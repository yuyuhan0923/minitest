const cloud = require('wx-server-sdk')

cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })

const db = cloud.database()

exports.main = async (event) => {
  const { sessionId } = event
  if (!sessionId) return null

  try {
    const record = await db.collection('exam_records').doc(sessionId).get()
    return record.data
  } catch (_) {
    return null
  }
}
