const db = wx.cloud.database()

function normalizeQuestion(q) {
  return {
    ...q,
    score: q.score ?? 3,
    // 把云数据库里的 options 数组转成 {key, text} 格式，和 WXML 匹配
    optionsJson: (q.options || []).map((text, idx) => ({
      key: ['A', 'B', 'C', 'D'][idx],
      text: text
    }))
  }
}

Page({
  data: {
    questions: [],
    loading: true,
    filterCategory: '',
    categories: ['全部', '编程基础', '数学基础', '机器学习基础', '深度学习基础', '框架工具能力', '项目实战能力']
  },

  onLoad() {
    this.loadQuestions()
  },

  onPullDownRefresh() {
    this.loadQuestions().finally(() => wx.stopPullDownRefresh())
  },

  loadQuestions() {
    this.setData({ loading: true })
    return db.collection('questions')
      .orderBy('sortOrder', 'asc')
      .get()
      .then(res => {
        const questions = res.data.map(normalizeQuestion)
        this.setData({ questions, loading: false })
      })
      .catch(err => {
        console.error('加载题库失败', err)
        this.setData({ loading: false })
      })
  },

  switchCategory(e) {
    const cat = e.currentTarget.dataset.cat
    this.setData({ filterCategory: cat === '全部' ? '' : cat })
  },

  toggleQuestion(e) {
    const idx = e.currentTarget.dataset.idx
    const key = `questions[${idx}]._expanded`
    this.setData({ [key]: !this.data.questions[idx]._expanded })
  },

  goExam() {
    wx.navigateTo({ url: '/pages/exam/exam' })
  }
})