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

exports.main = async (event, context) => {
  const { OPENID } = cloud.getWXContext()
  const { skipAdminCheck = false, action = 'get', config = {} } = event

  // ── 写入/更新配置（仅管理员）────────────────────────────────
  if (action === 'set') {
    if (!skipAdminCheck && !(await checkAdmin(OPENID))) {
      return { code: 403, message: '无权限' }
    }
    try {
      const col = db.collection('course_config')
      const { data } = await col.limit(1).get()
      if (data.length === 0) {
        await col.add({ data: { ...config, updatedAt: db.serverDate() } })
      } else {
        await col.doc(data[0]._id).update({ data: { ...config, updatedAt: db.serverDate() } })
      }
      return { code: 0, message: 'ok' }
    } catch (e) {
      return { code: -1, message: e.message }
    }
  }

  // ── 读取配置（所有人可访问）──────────────────────────────────
  try {
    const { data } = await db.collection('course_config').limit(1).get()
    if (data.length === 0) {
      // 集合为空时返回默认配置，前端不白屏
      return {
        code: 0,
        data: {
          courseName: 'AI算法就业实战班',
          coursePrice: '¥799',
          courseDesc: '从Python基础到模型部署，30天掌握AI算法核心技能',
          trialUrl: '',
          buyUrl: '',
          discountText: '限时优惠，立省¥500',
          highlights: [
            '零基础可学，从Python到模型部署',
            '30天高强度实战，掌握AI算法核心',
            '实战项目 + 作业批改 + 1v1答疑',
            '简历指导 + 内推机会'
          ]
        }
      }
    }
    const cfg = data[0]
    return {
      code: 0,
      data: {
        courseName:    cfg.courseName    || 'AI算法就业实战班',
        coursePrice:   cfg.coursePrice   || '¥799',
        courseDesc:    cfg.courseDesc    || '',
        trialUrl:      cfg.trialUrl      || '',
        buyUrl:        cfg.buyUrl        || '',
        discountText:  cfg.discountText  || '',
        highlights:    Array.isArray(cfg.highlights) ? cfg.highlights : []
      }
    }
  } catch (e) {
    return { code: -1, message: e.message }
  }
}
