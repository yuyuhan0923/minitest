const app = getApp()

Page({
  data: {
    isLoggedIn: false
  },

  onShow() {
    const token = wx.getStorageSync('token')
    this.setData({
      isLoggedIn: !!token
    })
  },

  startExam() {
    // 使用 app.requireLogin：已登录直接跳转，未登录先弹授权再跳转
    app.requireLogin(() => {
      wx.navigateTo({ url: '/pages/exam/exam' })
    })
  },

  goQuestions() {
    wx.switchTab({
      url: '/pages/questions/questions'
    })
  },

  goProfile() {
    wx.switchTab({
      url: '/pages/profile/profile'
    })
  },

  goLogin() {
    wx.navigateTo({
      url: '/pages/login/login'
    })
  },

  goLead() {
    wx.navigateTo({
      url: '/pages/lead/lead'
    })
  },

  goAdmin() {},

  checkLogin() {
    if (!wx.getStorageSync('token')) {
      wx.showModal({
        title: '提示',
        content: '请先登录',
        confirmText: '去登录',
        showCancel: false,
        success: () => {
          wx.navigateTo({
            url: '/pages/login/login'
          })
        }
      })
      return false
    }
    return true
  }
})