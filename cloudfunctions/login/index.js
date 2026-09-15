const cloud = require('wx-server-sdk')

cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })

const db = cloud.database()

exports.main = async (event, context) => {
  const { OPENID, APPID } = cloud.getWXContext()

  if (!OPENID) {
    return { code: -1, message: '获取 openid 失败' }
  }

  // 接收前端传来的用户信息（可选）
  const { nickName = '微信用户', avatarUrl = '' } = event

  const now = db.serverDate()
  const usersCol = db.collection('users')

  try {
    const { data } = await usersCol.where({ openid: OPENID }).get()

    if (data.length === 0) {
      // 新用户，写入完整信息
      await usersCol.add({
        data: {
          openid: OPENID,
          appid: APPID,
          nickName,
          avatarUrl,
          createdAt: now,
          lastLoginAt: now,
          loginCount: 1
        }
      })
    } else {
      // 老用户，更新登录时间；有新昵称/头像时一并更新
      const updateData = {
        lastLoginAt: now,
        loginCount: db.command.inc(1)
      }
      if (nickName && nickName !== '微信用户') updateData.nickName = nickName
      if (avatarUrl) updateData.avatarUrl = avatarUrl

      await usersCol.doc(data[0]._id).update({ data: updateData })
    }

    return {
      code: 200,
      openid: OPENID,
      appid: APPID,
      isNew: data.length === 0
    }
  } catch (err) {
    return { code: -1, message: err.message || '数据库操作失败' }
  }
}
