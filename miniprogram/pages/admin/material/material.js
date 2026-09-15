Page({
  data: { list: [], loading: true },

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
    setTimeout(() => {
      this.setData({
        list: [
          {
            id: 1,
            title: "AI学习资料包",
            desc: "包含：AI算法路线图PDF、Python数据分析模板、机器学习速查表、PyTorch模板、面试题100道、实战案例集",
            downloadUrl: "https://example.com/material1.zip"
          }
        ],
        loading: false
      })
    }, 500)
  },

  copyLink(e) {
    const link = e.currentTarget.dataset.link
    if (!link) {
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