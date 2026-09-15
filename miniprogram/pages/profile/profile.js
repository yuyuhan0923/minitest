const app = getApp()

Page({
  data: {
    userInfo: {},
    isAdmin: true
  },

  onLoad() {
    this._loadUser()
  },

  onShow() {
    // tab 切换时也刷新，确保登录后立即显示最新头像昵称
    this._loadUser()
  },

  _loadUser() {
    const user = wx.getStorageSync('userInfo')
    this.setData({ userInfo: user || {} })
  },

  // 开始测评
  goToStartExam() {
    wx.navigateTo({ url: '/pages/exam/exam' })
  },

  // 浏览题库（TAB页面 → 用 switchTab）
  goToQuestions() {
    wx.switchTab({
      url: '/pages/questions/questions'
    })
  },

  // 领取学习资料
  goToMaterial() {
    wx.navigateTo({ url: '/pages/lead/lead' })
  },

  // 测评记录
  goToRecord() {
    wx.navigateTo({ url: '/pages/record/record' })
  },

  // 试听 / 购买课程
  goToCourse() {
    wx.navigateTo({ url: '/pages/course/course' })
  },

  // 管理后台
  goToAdmin() {
    wx.navigateTo({ url: '/pages/admin/login/login' })
  },

  // 清除缓存
  clearCache() {
    wx.showModal({
      title: '提示',
      content: '确定清除缓存？',
      success: res => {
        if (res.confirm) {
          wx.clearStorageSync()
          wx.showToast({ title: '缓存已清除', icon: 'success' })
        }
      }
    })
  },

  // 退出登录
  logout() {
    wx.showModal({
      title: '提示',
      content: '确定退出登录？',
      success: res => {
        if (res.confirm) {
          wx.removeStorageSync('userInfo')
          wx.removeStorageSync('token')
          wx.reLaunch({ url: '/pages/login/login' })
        }
      }
    })
  }
})