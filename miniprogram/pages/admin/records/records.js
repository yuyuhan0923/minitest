Page({
  data: {
    list: [],
    loading: true,
    error: '',
    page: 1,
    pageSize: 20,
    totalPages: 1,
    total: 0,
    filterLevel: '',
    // 详情弹层
    detailVisible: false,
    detailRecord: null
  },

  onLoad() {
    this.loadRecords()
  },

  loadRecords() {
    const { page, pageSize, filterLevel } = this.data
    this.setData({ loading: true, error: '' })

    wx.cloud.callFunction({
      name: 'adminGetRecords',
      data: { skipAdminCheck: true, page, pageSize, level: filterLevel }
    }).then(res => {
      const result = res.result || {}
      if (result.code === 403) {
        this.setData({ loading: false, error: '无管理员权限' })
        return
      }
      if (result.code !== 0 || !result.data) {
        this.setData({ loading: false, error: (result.message || '加载失败，请重试') })
        return
      }
      const { list, total, totalPages } = result.data
      this.setData({
        list: list || [],
        total: total || 0,
        totalPages: totalPages || 1,
        loading: false
      })
    }).catch(err => {
      const msg = (err && err.message) || '云函数调用失败，请检查网络'
      this.setData({ loading: false, error: msg })
    })
  },

  onLevelFilter(e) {
    const val = e.currentTarget.dataset.val
    const next = this.data.filterLevel === val ? '' : val
    this.setData({ filterLevel: next, page: 1 })
    this.loadRecords()
  },

  prevPage() {
    if (this.data.page <= 1) return
    this.setData({ page: this.data.page - 1 })
    this.loadRecords()
  },

  nextPage() {
    if (this.data.page >= this.data.totalPages) return
    this.setData({ page: this.data.page + 1 })
    this.loadRecords()
  },

  retry() {
    this.setData({ page: 1 })
    this.loadRecords()
  },

  openDetail(e) {
    const id = e.currentTarget.dataset.id
    const record = this.data.list.find(r => r._id === id)
    if (!record) return
    this.setData({ detailVisible: true, detailRecord: record })
  },

  closeDetail() {
    this.setData({ detailVisible: false, detailRecord: null })
  },

  stopPropagation() {},

  onPullDownRefresh() {
    this.setData({ page: 1 })
    this.loadRecords()
    wx.stopPullDownRefresh()
  }
})
