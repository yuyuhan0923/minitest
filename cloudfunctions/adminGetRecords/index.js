const cloud = require('wx-server-sdk')

cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })

const db = cloud.database()

async function checkAdmin(openid) {
  try {
    const { data } = await db.collection('admins').where({ openid }).limit(1).get()
    return data.length > 0
  } catch (e) {
    return false
  }
}

function toDate(val) {
  if (!val) return null
  if (val instanceof Date) return val
  if (typeof val.toDate === 'function') return val.toDate()
  const d = new Date(val)
  return isNaN(d.getTime()) ? null : d
}

function formatDate(val) {
  const d = toDate(val)
  if (!d) return ''
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function safeArray(val) {
  return Array.isArray(val) ? val : []
}

exports.main = async (event, context) => {
  const { OPENID } = cloud.getWXContext()
  const {
    skipAdminCheck = false,
    page = 1,
    pageSize = 20,
    level = '',
    filterOpenid = ''
  } = event

  if (!skipAdminCheck && !(await checkAdmin(OPENID))) {
    return { code: 403, message: '无权限' }
  }

  // 构建筛选条件
  const whereClause = {}
  if (level) whereClause.level = level
  if (filterOpenid) whereClause.openid = filterOpenid

  const query = db.collection('exam_records').where(whereClause).orderBy('createdAt', 'desc')
  const skip = (page - 1) * pageSize

  // 并发获取总数和当前页数据
  let total = 0
  let records = []
  try {
    const [countRes, listRes] = await Promise.all([
      query.count(),
      query.skip(skip).limit(pageSize).get()
    ])
    total = countRes.total || 0
    records = listRes.data || []
  } catch (e) {
    return { code: -1, message: '查询记录失败: ' + e.message }
  }

  if (records.length === 0) {
    return {
      code: 0,
      data: { list: [], total, page, pageSize, totalPages: Math.max(1, Math.ceil(total / pageSize)) }
    }
  }

  // 收集当前页所有 openid（兼容大小写）
  const openids = [...new Set(
    records.map(r => r.openid || r.openId || r._openid || '').filter(Boolean)
  )]

  // 并发查 users + leads，limit 与当前页条数对齐
  const batchLimit = Math.min(openids.length, pageSize)
  const [usersRes, leadsRes] = await Promise.all([
    db.collection('users')
      .where({ openid: db.command.in(openids) })
      .limit(batchLimit)
      .get()
      .catch(() => ({ data: [] })),
    db.collection('leads')
      .where({ openid: db.command.in(openids) })
      .limit(batchLimit)
      .get()
      .catch(() => ({ data: [] }))
  ])

  // 构建 openid → user/lead 映射
  const userMap = {}
  safeArray(usersRes.data).forEach(u => {
    const oid = u.openid || u._openid || ''
    if (oid) userMap[oid] = { ...u }
  })
  safeArray(leadsRes.data).forEach(l => {
    const oid = l.openid || l.openId || l._openid || ''
    if (!oid) return
    if (!userMap[oid]) userMap[oid] = {}
    userMap[oid]._lead = l
  })

  // 组装返回数据
  const list = records.map(r => {
    const oid = r.openid || r.openId || r._openid || ''
    const u = userMap[oid] || {}
    const lead = u._lead || {}

    // 严格防护：所有数组字段用 safeArray
    const categoryScores = safeArray(r.categoryScores).map(c => ({
      category: (c && c.category) || '',
      score: Number((c && c.score) || 0),
      maxScore: Number((c && c.maxScore) || 0)
    }))

    const suggestions = safeArray(r.suggestions).filter(s => typeof s === 'string')

    // answers 是对象 { questionId: selectedOption }
    const answers = (r.answers && typeof r.answers === 'object' && !Array.isArray(r.answers))
      ? r.answers
      : {}

    return {
      _id: r._id,
      openid: oid,
      totalScore: Number(r.totalScore) || 0,
      maxScore: Number(r.maxScore) || 0,
      level: r.level || '',
      levelDesc: r.levelDesc || '',
      categoryScores,
      suggestions,
      answers,
      createdAt: formatDate(r.createdAt),
      userInfo: {
        nickname: u.nickName || u.nickname || lead.nickName || lead.nickname || '微信用户',
        phone: lead.phone || lead.phoneNumber || u.phone || ''
      }
    }
  })

  return {
    code: 0,
    data: {
      list,
      total,
      page,
      pageSize,
      totalPages: Math.max(1, Math.ceil(total / pageSize))
    }
  }
}
