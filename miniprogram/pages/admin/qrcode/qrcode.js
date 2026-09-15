Page({
  data: { contact: null, loading: true },

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
        contact: {
          teacherWechat: "sxy15205811430",
          // 老师微信二维码 → 对应本地图片 /images/teacher_wechat.png
          teacherQrcode: "/images/teacher_wechat.png",
          // 学习群二维码 → 对应本地图片 /images/study_group.png
          groupQrcode: "/images/study_group.png",
          // 企业微信二维码 → 复用老师微信图片（你项目里只有这张）
          workQrcode: "/images/teacher_wechat.png"
        },
        loading: false
      })
    }, 500)
  },

  copyWechat(e) {
    const wechat = e.currentTarget.dataset.wechat
    if (!wechat || wechat === '未配置') {
      wx.showToast({ title: '暂无微信号可复制', icon: 'none' })
      return
    }
    wx.setClipboardData({
      data: wechat,
      success: () => wx.showToast({ title: '微信号已复制', icon: 'success' })
    })
  },

  previewQrcode(e) {
    const url = e.currentTarget.dataset.url
    if (!url) {
      wx.showToast({ title: '暂无二维码', icon: 'none' })
      return
    }
    wx.previewImage({ current: url, urls: [url] })
  },

  onPullDownRefresh() {
    this.loadConfig()
    wx.stopPullDownRefresh()
  }
})