Page({
  data: { config: null, loading: true },

  onLoad() {
    const isAdmin = wx.getStorageSync('isAdmin')
    if (!isAdmin) {
      wx.redirectTo({ url: '/pages/admin/login/login' })
      return
    }
    this.loadConfig()
  },

  loadConfig() {
    this.setData({ loading: true })
    // 模拟配置数据（可替换为云数据库调用）
    setTimeout(() => {
      this.setData({
        config: {
          name: "AI算法实战训练营",
          price: "¥799",
          desc: "从0到1掌握AI算法实战技能",
          trialUrl: "https://example.com/trial",
          buyUrl: "https://example.com/buy" // 小鹅通购买链接
        },
        loading: false
      })
    }, 500)
  },

  // 复制链接功能
  copyLink(e) {
    const link = e.currentTarget.dataset.link
    if (!link || link === '未配置') {
      wx.showToast({ title: '暂无链接可复制', icon: 'none' })
      return
    }
    wx.setClipboardData({
      data: link,
      success: () => wx.showToast({ title: '链接已复制', icon: 'success' })
    })
  },

  onPullDownRefresh() {
    this.loadConfig()
    wx.stopPullDownRefresh()
  }
})