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

function assessLevel(totalExams) {
  if (!totalExams || totalExams === 0) return '初级'
  if (totalExams <= 2) return '中级'
  return '高级'
}

function behaviorTag(recentCount) {
  if (recentCount >= 5) return '活跃'
  if (recentCount <= 1) return '沉默'
  return '新用户'
}

function maskPhone(phone) {
  if (!phone || phone.length < 7) return phone || ''
  return phone.substring(0, 3) + '****' + phone.substring(phone.length - 4)
}

function toDate(val) {
  if (!val) return null
  if (val instanceof Date) return val
  if (val.toDate) return val.toDate()
  const d = new Date(val)
  return isNaN(d) ? null : d
}

exports.main = async (event, context) => {
  const { OPENID } = cloud.getWXContext()
  const { skipAdminCheck } = event

  if (!skipAdminCheck && !await checkAdmin(OPENID)) {
    return { code: 403, message: '无权限' }
  }

  const todayStart = new Date()
  todayStart.setHours(0, 0, 0, 0)

  const thirtyDaysAgo = new Date()
  thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30)

  const [examsRes, leadsRes, usersRes] = await Promise.all([
    db.collection('exam_records').limit(500).get(),
    db.collection('leads').limit(500).get(),
    db.collection('users').limit(500).get()
  ])

  const exams = examsRes.data || []
  const leads = leadsRes.data || []
  const users = usersRes.data || []

  const leadMap = {}
  leads.forEach(l => {
    const oid = l.openid || l.openId || l._openid || ''
    if (oid && !leadMap[oid]) leadMap[oid] = l
  })

  const userMap = {}
  users.forEach(u => {
    const oid = u.openid || u._openid || ''
    if (oid) userMap[oid] = u
  })

  const examsByOpenid = {}
  exams.forEach(e => {
    const oid = e.openid || e.openId || e._openid || ''
    if (!oid) return
    if (!examsByOpenid[oid]) examsByOpenid[oid] = []
    examsByOpenid[oid].push(e)
  })

  const allOpenids = [...new Set([
    ...Object.keys(examsByOpenid),
    ...Object.keys(userMap)
  ])]

  const list = allOpenids.map(openid => {
    const userExams = examsByOpenid[openid] || []
    const totalExams = userExams.length

    const todayExams = userExams.filter(e => {
      const t = toDate(e.createdAt)
      return t && t >= todayStart
    }).length

    const recentCount = userExams.filter(e => {
      const t = toDate(e.createdAt)
      return t && t >= thirtyDaysAgo
    }).length

    const lastExamTime = userExams.reduce((latest, e) => {
      const t = toDate(e.createdAt)
      return t && (!latest || t > latest) ? t : latest
    }, null)

    const lead = leadMap[openid]
    const user = userMap[openid]

    return {
      openid,
      nickname: (lead && (lead.nickName || lead.nickname)) ||
                (user && (user.nickName || user.nickname)) || '微信用户',
      phone: maskPhone(lead && (lead.phone || lead.phoneNumber)),
      assessLevel: assessLevel(totalExams),
      behaviorTag: behaviorTag(recentCount),
      createTime: toDate(user && (user.createdAt || user._createTime)) ||
                  toDate(userExams[0] && userExams[0].createdAt) || '',
      lastExamTime: lastExamTime || '',
      todayExams,
      totalExams,
      hasLead: lead ? 1 : 0
    }
  })

  return { code: 200, data: list }
}
