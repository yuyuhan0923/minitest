const cloud = require('wx-server-sdk')

cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })

const db = cloud.database()
const _ = db.command

async function checkAdmin(openid) {
  try {
    const { data } = await db.collection('admins').where({ openid }).limit(1).get()
    return data.length > 0
  } catch (e) {
    return false
  }
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
    category = '',   // 按维度筛选
    keyword = '',    // 按关键词搜索题干
    action = '',     // 'toggleEnabled' 时执行状态切换
    questionId = '', // toggleEnabled 时传入 _id
    enabled = true   // toggleEnabled 时传入目标状态
  } = event

  if (!skipAdminCheck && !(await checkAdmin(OPENID))) {
    return { code: 403, message: '无权限' }
  }

  // ── 状态切换 ──────────────────────────────────────────────
  if (action === 'toggleEnabled' && questionId) {
    try {
      await db.collection('questions').doc(questionId).update({
        data: { enabled: !!enabled }
      })
      return { code: 0, message: 'ok' }
    } catch (e) {
      return { code: -1, message: '更新失败: ' + e.message }
    }
  }

  // ── 查询列表 ──────────────────────────────────────────────
  // 构建 where 条件（云数据库不支持 like，关键词搜索在前端过滤）
  const whereClause = {}
  if (category) whereClause.category = category

  const query = db.collection('questions')
    .where(whereClause)
    .orderBy('sortOrder', 'asc')

  const skip = (page - 1) * pageSize

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
    return { code: -1, message: '查询失败: ' + e.message }
  }

  // 组装返回字段，管理端保留 correctAnswer
  const list = records.map(q => ({
    _id: q._id,
    title: q.title || '',
    options: safeArray(q.options),
    category: q.category || '',
    score: Number(q.score) || 3,
    sortOrder: Number(q.sortOrder) || 0,
    enabled: q.enabled !== false, // 字段不存在时默认启用
    correctAnswer: q.correctAnswer || ''
  }))

  // 收集所有 category 供前端筛选栏使用
  let categories = []
  try {
    const allRes = await db.collection('questions')
      .field({ category: true })
      .limit(200)
      .get()
    const catSet = new Set((allRes.data || []).map(q => q.category).filter(Boolean))
    categories = [...catSet].sort()
  } catch (e) {
    // 获取分类失败不影响主流程
  }

  return {
    code: 0,
    data: {
      list,
      total,
      page,
      pageSize,
      totalPages: Math.max(1, Math.ceil(total / pageSize)),
      categories
    }
  }
}
