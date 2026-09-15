const app = getApp()
const db = wx.cloud.database()

Page({
  data: {
    questions: [],
    currentIndex: 0,
    currentQuestion: null,
    selectedAnswer: '',
    answers: {},
  },

  onLoad() {
    // 登录校验：未登录则弹授权，授权后继续加载题目；取消则返回上页
    if (!app.isLoggedIn()) {
      app.doLogin(
        () => { this.loadAllQuestions() },
        () => { wx.navigateBack() }
      )
    } else {
      this.loadAllQuestions()
    }
  },

  loadAllQuestions() {
    wx.showLoading({ title: '加载中...' })
    db.collection('questions')
      .orderBy('sortOrder', 'asc')
      .get()
      .then(res => {
        wx.hideLoading()
        let qs = res.data.map(item => {
          let opts = item.options.map((text, i) => {
            return [['A', 'B', 'C', 'D'][i], text]
          })
          return { ...item, options: opts }
        })
        this.setData({
          questions: qs,
          currentQuestion: qs[0]
        })
      }).catch(err => {
        wx.hideLoading()
        console.error(err)
      })
  },

  selectOption(e) {
    let val = e.currentTarget.dataset.value
    this.setData({ selectedAnswer: val })
    let id = this.data.questions[this.data.currentIndex]._id
    this.setData({ [`answers.${id}`]: val })
  },

  prevQuestion() {
    let i = this.data.currentIndex - 1
    if (i < 0) return
    let q = this.data.questions[i]
    this.setData({
      currentIndex: i,
      currentQuestion: q,
      selectedAnswer: this.data.answers[q._id] || ''
    })
  },

  nextQuestion() {
    let i = this.data.currentIndex + 1
    if (i >= this.data.questions.length) {
      this.submitExam()
      return
    }
    let q = this.data.questions[i]
    this.setData({
      currentIndex: i,
      currentQuestion: q,
      selectedAnswer: this.data.answers[q._id] || ''
    })
  },

  submitExam() {
    const { questions, answers } = this.data
    let totalScore = 0
    let maxScore = questions.length * 3

    // ✅ 按分类统计得分（和你图里的6个方向完全对应）
    let categoryScores = {
      '编程基础': { score: 0, max: 0 },
      '数学基础': { score: 0, max: 0 },
      '机器学习基础': { score: 0, max: 0 },
      '深度学习基础': { score: 0, max: 0 },
      '框架工具能力': { score: 0, max: 0 },
      '项目实战能力': { score: 0, max: 0 }
    }

    questions.forEach(q => {
      const ans = answers[q._id] || 'A'
      let s = 0
      if (ans === 'B') s = 1
      else if (ans === 'C') s = 2
      else if (ans === 'D') s = 3
      totalScore += s

      // ✅ 按题目分类累加得分（假设题目有 category 字段）
      const cat = q.category || '编程基础' // 兜底默认分类
      if (categoryScores[cat]) {
        categoryScores[cat].score += s
        categoryScores[cat].max += 3 // 每题满分3分
      }
    })

    let level, evalText, suggest
    if (totalScore <= 20) {
      level = "入门级"
      evalText = "你刚接触AI算法，需要从基础开始系统学习。"
      suggest = ["补充数学基础、Python基础、简单案例入门"]
    } else if (totalScore <= 40) {
      level = "基础级"
      evalText = "你已入门AI算法，系统学习后可以大幅提升。"
      suggest = ["学习机器学习基础、数据预处理、模型评估"]
    } else if (totalScore <= 60) {
      level = "进阶级"
      evalText = "你已掌握AI基础，具备进阶实战能力。"
      suggest = ["学习经典算法、项目实战、框架使用"]
    } else if (totalScore <= 80) {
      level = "实战级"
      evalText = "你具备较强的AI实战能力。"
      suggest = ["模型优化、项目部署、竞赛实战"]
    } else {
      level = "专家级"
      evalText = "你已达到AI专家水平。"
      suggest = ["前沿研究、项目主导、技术分享"]
    }

    wx.showLoading({ title: '提交中...' })
    const openid = wx.getStorageSync('openId') || (app.globalData.userInfo && app.globalData.userInfo.openid) || 'unknown'
    db.collection('exam_records').add({
      data: {
        openid,
        totalScore,
        maxScore,
        level,
        evalText,
        suggestions: suggest,
        categoryScores, // ✅ 把分类得分存入数据库
        createdAt: db.serverDate()
      }
    }).then(() => {
      wx.hideLoading()
      wx.redirectTo({
        url: `/pages/result/result?totalScore=${totalScore}&maxScore=${maxScore}&level=${level}&evalText=${encodeURIComponent(evalText)}&suggest=${encodeURIComponent(JSON.stringify(suggest))}&categoryScores=${encodeURIComponent(JSON.stringify(categoryScores))}`
      })
    }).catch(err => {
      wx.hideLoading()
      console.error(err)
      wx.redirectTo({
        url: `/pages/result/result?totalScore=${totalScore}&maxScore=${maxScore}&level=${level}&evalText=${encodeURIComponent(evalText)}&suggest=${encodeURIComponent(JSON.stringify(suggest))}&categoryScores=${encodeURIComponent(JSON.stringify(categoryScores))}`
      })
    })
  }
})