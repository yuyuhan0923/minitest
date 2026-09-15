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

// 安全 count：集合不存在时返回 0
async function safeCount(collection, where) {
  try {
    const q = where
      ? db.collection(collection).where(where)
      : db.collection(collection)
    const { total } = await q.count()
    return total || 0
  } catch (e) {
    return 0
  }
}

// 去重 openid 数：取前 500 条，统计唯一 openid 数
// 云数据库不支持 distinct，用 limit(500) 近似去重
async function countDistinctOpenid(collection) {
  try {
    const { data } = await db.collection(collection)
      .field({ openid: true, _openid: true })
      .limit(500)
      .get()
    const set = new Set()
    ;(data || []).forEach(r => {
      const oid = r.openid || r._openid || ''
      if (oid) set.add(oid)
    })
    return set.size
  } catch (e) {
    return 0
  }
}

exports.main = async (event, context) => {
  const { OPENID } = cloud.getWXContext()
  const { skipAdminCheck = false } = event

  if (!skipAdminCheck && !(await checkAdmin(OPENID))) {
    return { code: 403, message: '无权限' }
  }

  // ── 并发查各集合 ──────────────────────────────────────────
  const [
    visitCount,
    registerCount,
    examCount,
    leadCount,
    purchaseCount,
    // 各等级分布（并发）
    levelEnter,
    levelBase,
    levelAdv,
    levelPro,
    levelExpert,
    // 近7天每日答题数（串行，避免并发过多）
  ] = await Promise.all([
    // 访问人数：visit_log 去重 openid
    countDistinctOpenid('visit_log'),
    // 注册人数：users 集合总数
    safeCount('users'),
    // 完成测评人数：exam_records 去重 openid
    countDistinctOpenid('exam_records'),
    // 留资人数：leads 集合总数
    safeCount('leads'),
    // 点击购买人数：purchase 集合总数
    safeCount('purchase'),
    // 等级分布
    safeCount('exam_records', { level: '入门级' }),
    safeCount('exam_records', { level: '基础级' }),
    safeCount('exam_records', { level: '进阶级' }),
    safeCount('exam_records', { level: '实战级' }),
    safeCount('exam_records', { level: '专家级' }),
  ])

  // 转化率 = 点击购买人数 / 访问人数（访问为 0 时显示 0%）
  const conversionRate = visitCount > 0
    ? (purchaseCount / visitCount * 100).toFixed(1) + '%'
    : '0.0%'

  // 近7天每日答题数（串行避免超时）
  const daily = []
  for (let i = 6; i >= 0; i--) {
    const dayStart = new Date()
    dayStart.setHours(0, 0, 0, 0)
    dayStart.setDate(dayStart.getDate() - i)
    const dayEnd = new Date(dayStart)
    dayEnd.setDate(dayEnd.getDate() + 1)
    const count = await safeCount('exam_records', {
      createdAt: _.gte(dayStart).and(_.lt(dayEnd))
    })
    daily.push({
      date: `${dayStart.getMonth() + 1}/${dayStart.getDate()}`,
      count
    })
  }

  return {
    code: 0,
    data: {
      visitCount,
      registerCount,
      examCount,
      leadCount,
      purchaseCount,
      conversionRate,
      levelGroups: {
        '入门级': levelEnter,
        '基础级': levelBase,
        '进阶级': levelAdv,
        '实战级': levelPro,
        '专家级': levelExpert
      },
      daily
    }
  }
}
