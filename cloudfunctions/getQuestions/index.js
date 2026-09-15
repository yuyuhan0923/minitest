const cloud = require('wx-server-sdk')

cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })

const db = cloud.database()

exports.main = async () => {
  const { data } = await db.collection('questions')
    .orderBy('sortOrder', 'asc')
    .limit(20)
    .get()

  // 去掉 correctAnswer，不暴露给前端
  return data.map(q => ({
    id: q._id,
    title: q.title,
    options: q.options,
    category: q.category,
    score: q.score || 3,
    sortOrder: q.sortOrder || 0
  }))
}
