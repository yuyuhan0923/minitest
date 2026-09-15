const app = getApp()

Page({
  data: {
    totalScore: 0,
    maxScore: 54,
    level: "基础级",
    evalText: "",
    categoryScores: [], // ✅ 动态接收分类得分
    suggestions: [],
    showTeacher: false,
    showGroup: false
  },

  onLoad(options) {
    // ✅ 接收所有参数，包括分类得分
    const categoryScores = JSON.parse(decodeURIComponent(options.categoryScores || "{}"))
    // 转换为数组格式，方便页面渲染
    const categoryList = Object.keys(categoryScores).map(key => ({
      name: key,
      score: categoryScores[key].score,
      max: categoryScores[key].max,
      percent: (categoryScores[key].score / categoryScores[key].max) * 100
    }))

    this.setData({
      totalScore: Number(options.totalScore) || 0,
      maxScore: Number(options.maxScore) || 54,
      level: options.level || "基础级",
      evalText: decodeURIComponent(options.evalText || ""),
      suggestions: options.suggest ? JSON.parse(decodeURIComponent(options.suggest)) : [],
      categoryScores: categoryList // ✅ 替换为真实分类得分
    })
  },

  goBack() {
    wx.navigateBack()
  },
  goToMaterial() {
    // 领取学习资料需要登录
    app.requireLogin(() => {
      wx.navigateTo({ url: "/pages/lead/lead" })
    })
  },
  goToCourse() {
    // 领取完整报告/购买课程需要登录
    app.requireLogin(() => {
      wx.navigateTo({ url: "/pages/buy/buy" })
    })
  },
  restartExam() {
    wx.redirectTo({ url: "/pages/exam/exam" })
  },

  showTeacherQr() {
    this.setData({ showTeacher: true })
  },
  closeTeacherModal() {
    this.setData({ showTeacher: false })
  },

  showGroupQr() {
    this.setData({ showGroup: true })
  },
  closeGroupModal() {
    this.setData({ showGroup: false })
  },

  copyWechat() {
    wx.setClipboardData({
      data: "sxy15205811430",
      success: () => {
        wx.showToast({ title: "微信号已复制", icon: "success" })
      }
    })
  },

  saveTeacherQrcode() {
    wx.showToast({ title: "长按图片可保存", icon: "none" })
  },
  saveGroupQrcode() {
    wx.showToast({ title: "长按图片可保存", icon: "none" })
  },

  stopClose() {}
})