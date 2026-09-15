const cloud = require('wx-server-sdk')
const crypto = require('crypto')

cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })

const db = cloud.database()

exports.main = async (event) => {
  const { username, password } = event

  if (!username || !password) {
    return { code: 400, message: '用户名和密码不能为空' }
  }

  let admin
  try {
    const { data } = await db.collection('admins')
      .where({ username })
      .limit(1)
      .get()
    admin = data[0]
  } catch (err) {
    return { code: 500, message: '查询失败: ' + err.message }
  }

  if (!admin) {
    return { code: 401, message: '用户名或密码错误' }
  }

  // 密码用 SHA256 存储：sha256(password + salt)
  const hash = crypto.createHash('sha256')
    .update(password + (admin.salt || ''))
    .digest('hex')

  if (hash !== admin.passwordHash) {
    return { code: 401, message: '用户名或密码错误' }
  }

  // 更新最后登录时间
  await db.collection('admins').doc(admin._id).update({
    data: { lastLoginAt: db.serverDate() }
  })

  return {
    code: 200,
    username: admin.username,
    role: 'admin'
  }
}
