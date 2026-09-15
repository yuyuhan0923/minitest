const app = getApp()

Page({
  data: {
    loading: true,
    error: '',
    config: null
    // config 结构：{ courseName, coursePrice, courseDesc, trialUrl, buyUrl, discountText, highlights[] }
  },

  onLoad() {
    this.loadConfig()
  },

  loadConfig() {
    this.setData({ loading: true, error: '' })
    wx.cloud.callFunction({ name: 'adminGetCourseConfig' })
      .then(res => {
        const result = res.result || {}
        if (result.code !== 0 || !result.data) {
          this.setData({ loading: false, error: result.message || '配置加载失败' })
          return
        }
        this.setData({ loading: false, config: result.data })
      })
      .catch(err => {
        this.setData({ loading: false, error: (err && err.message) || '网络错误' })
      })
  },

  goTrial() {
    app.requireLogin(() => {
      wx.setClipboardData({
        data: 'https://study.xiaoe-tech.com/#/wx',
        success() {
          wx.showModal({
            title: '链接已复制',
            content: '请在浏览器中打开：https://study.xiaoe-tech.com/#/wx',
            showCancel: false,
            confirmText: '知道了'
          })
        }
      })
    })
  },

  goBuy() {
    app.requireLogin(() => {
      wx.setClipboardData({
        data: 'https://study.xiaoe-tech.com/#/wx',
        success() {
          wx.showModal({
            title: '链接已复制',
            content: '请在浏览器中打开：https://study.xiaoe-tech.com/#/wx',
            showCancel: false,
            confirmText: '知道了'
          })
        }
      })
    })
  },

  retry() {
    this.loadConfig()
  },

  onPullDownRefresh() {
    this.loadConfig()
    wx.stopPullDownRefresh()
  }
})
