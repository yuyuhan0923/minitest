const cloud = require('wx-server-sdk')

cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })

const db = cloud.database()

function calcLevel(score, maxScore) {
  // 容错：防止除0
  if (maxScore === 0) return { level: '入门级', levelDesc: '暂无有效题目，无法评估。' }
  const pct = score / maxScore
  if (pct >= 0.9) return { level: '专家级', levelDesc: '你对AI算法有深入理解，具备独立解决复杂问题的能力。' }
  if (pct >= 0.75) return { level: '实战级', levelDesc: '你已掌握AI算法核心技能，能够应对大多数工程场景。' }
  if (pct >= 0.6)  return { level: '进阶级', levelDesc: '你有一定基础，继续深入可以快速提升到实战水平。' }
  if (pct >= 0.4)  return { level: '基础级', levelDesc: '你已入门AI算法，系统学习后可以大幅提升。' }
  return { level: '入门级', levelDesc: '你刚开始接触AI算法，建议从基础开始系统学习。' }
}

function optionScore(selected) {
  return { A: 0, B: 1, C: 2, D: 3 }[selected] ?? 0
}

function calcCategoryScores(questions, answers) {
  const map = {}
  questions.forEach(q => {
    // 容错：category 为空时默认“综合”
    const cat = q.category?.trim() || '综合'
    if (!map[cat]) map[cat] = { category: cat, score: 0, maxScore: 0 }
    // 每题满分默认3
    const qMax = q.score || 3
    map[cat].maxScore += qMax
    // 累加本题得分
    map[cat].score += optionScore(answers[q._id])
  })
  return Object.values(map)
}

function genSuggestions(categoryScores) {
  const tips = {
    '编程基础': '建议系统学习 Python，掌握 NumPy/Pandas，多做数据处理练习。',
    '数学基础': '建议补充线性代数、概率统计和微积分基础，这是理解算法的核心。',
    '机器学习基础': '建议系统学习监督/无监督学习，用 sklearn 完成完整项目实战。',
    '深度学习基础': '建议从神经网络基础入手，理解 CNN/Transformer 核心原理。',
    '框架工具能力': '建议熟练掌握 PyTorch，尝试 GPU 训练和 Hugging Face 模型调用。',
    '项目实战能力': '建议动手完成 1-2 个完整 AI 项目，整理到 GitHub 形成作品集。'
  }
  return categoryScores
    .filter(c => {
      // 防止除0
      if (c.maxScore === 0) return false
      return c.score / c.maxScore < 0.6
    })
    .map(c => tips[c.category] || `建议加强 ${c.category} 方向的学习。`)
    .slice(0, 3)
}

exports.main = async (event, context) => {
  const { OPENID } = cloud.getWXContext()
  const { answers = {} } = event

  // 安全获取题目
  let questions = []
  try {
    const { data } = await db.collection('questions')
      .orderBy('sortOrder', 'asc')
      .limit(20)
      .get()
    questions = data || []
  } catch (err) {
    return { code: -1, message: '获取题库失败', error: err.message }
  }

  if (questions.length === 0) {
    return { code: -1, message: '题库为空，请先添加题目' }
  }

  // 计算总分
  const maxScore = questions.reduce((s, q) => s + (q.score || 3), 0)
  const totalScore = questions.reduce((s, q) => s + optionScore(answers[q._id]), 0)

  // 生成结果
  const { level, levelDesc } = calcLevel(totalScore, maxScore)
  const categoryScores = calcCategoryScores(questions, answers)
  const suggestions = genSuggestions(categoryScores)

  const result = {
    totalScore,
    maxScore,
    level,
    levelDesc,
    categoryScores,
    suggestions,
    hasLead: false
  }

  // 保存记录
  let res = null
  if (OPENID) {
    try {
      res = await db.collection('exam_records').add({
        data: {
          openid: OPENID,
          answers,
          totalScore,
          maxScore,
          level,
          categoryScores,
          suggestions,
          createdAt: db.serverDate()
        }
      })
    } catch (err) {
      console.error('保存记录失败', err)
    }
  }

  result.success = true
  if (res?._id) result._id = res._id

  return result
}