const cloud = require('wx-server-sdk')

cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })

const db = cloud.database()

exports.main = async (event, context) => {
  const { OPENID } = cloud.getWXContext()
  const { phone, wechat, nickname } = event

  await db.collection('leads').add({
    data: {
      openid: OPENID || '',
      phone:    phone    || '',
      wechat:   wechat   || '',
      nickname: nickname || '',
      createdAt: db.serverDate()
    }
  })

  return { code: 200 }
}
