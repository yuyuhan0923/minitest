const app = getApp()
const db = wx.cloud.database()

Page({
  data: {
    list: [],
    loading: true
  },

  onLoad() {
    this.getMyRecords()
  },

  getMyRecords() {
    const openId = wx.getStorageSync('openId')
    if (!openId) {
      this.setData({ loading: false })
      return
    }

    // ✅ 最终修复：查询字段统一为 openid，大小写完全匹配
    db.collection('exam_records')
      .where({
        openid: openId
      })
      .orderBy('createdAt', 'desc')
      .get()
      .then(res => {
        console.log("读取到记录：", res.data)
        this.setData({
          list: res.data,
          loading: false
        })
      })
      .catch(err => {
        console.error('查询失败', err)
        this.setData({ loading: false })
      })
  },

  goToResult(e) {
    const recordId = e.currentTarget.dataset.id
    const record = this.data.list.find(item => item._id === recordId)
    if (!record) return

    const url = `/pages/result/result?totalScore=${record.totalScore}&maxScore=${record.maxScore}&level=${record.level}&evalText=${encodeURIComponent(record.evalText || '')}&suggest=${encodeURIComponent(JSON.stringify(record.suggestions || []))}&categoryScores=${encodeURIComponent(JSON.stringify(record.categoryScores || {}))}`

    wx.navigateTo({ url })
  },

  goBack() {
    wx.navigateBack()
  }
})