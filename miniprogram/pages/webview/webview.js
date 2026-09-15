Page({
  data: {
    url: ''
  },
  onLoad(options) {
    const url = decodeURIComponent(options.url || '')
    const title = decodeURIComponent(options.title || '课程详情')
    this.setData({ url })
    wx.setNavigationBarTitle({ title })
  }
})
