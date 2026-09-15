// 选项字母前缀
const OPTION_LABELS = ['A', 'B', 'C', 'D', 'E', 'F']

Page({
  data: {
    list: [],           // 当前页（经关键词过滤后）的题目
    rawList: [],        // 当前页原始数据（未过滤）
    loading: true,
    error: '',
    page: 1,
    pageSize: 20,
    totalPages: 1,
    total: 0,
    categories: [],     // 所有维度列表
    filterCategory: '', // 当前选中维度
    keyword: '',        // 搜索关键词
    // 详情弹层
    detailVisible: false,
    detailQuestion: null,
    // 切换状态 loading
    togglingId: ''
  },

  onLoad() {
    const isAdmin = wx.getStorageSync('isAdmin')
    if (!isAdmin) {
      wx.showToast({ title: '无权限', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1500)
      return
    }
    this.loadQuestions()
  },

  loadQuestions() {
    const { page, pageSize, filterCategory } = this.data
    this.setData({ loading: true, error: '' })

    wx.cloud.callFunction({
      name: 'adminGetQuestions',
      data: { skipAdminCheck: true, page, pageSize, category: filterCategory }
    }).then(res => {
      const result = res.result || {}
      if (result.code === 403) {
        this.setData({ loading: false, error: '无管理员权限' })
        return
      }
      if (result.code !== 0 || !result.data) {
        this.setData({ loading: false, error: result.message || '加载失败，请重试' })
        return
      }
      const { list, total, totalPages, categories } = result.data
      // 给每道题的选项加上 A/B/C/D 前缀，方便展示
      const formatted = (list || []).map(q => ({
        ...q,
        optionsFormatted: (q.options || []).map((opt, i) => `${OPTION_LABELS[i] || i + 1}. ${opt}`)
      }))
      const cats = categories && categories.length > 0
        ? categories
        : this.data.categories  // 保留上次拉到的分类
      this.setData({
        rawList: formatted,
        total: total || 0,
        totalPages: totalPages || 1,
        categories: cats,
        loading: false
      })
      this.applyKeyword()
    }).catch(err => {
      const msg = (err && err.message) || '云函数调用失败'
      this.setData({ loading: false, error: msg })
    })
  },

  // 关键词过滤（本地，不重新请求）
  applyKeyword() {
    const { rawList, keyword } = this.data
    const kw = (keyword || '').trim().toLowerCase()
    const list = kw
      ? rawList.filter(q => (q.title || '').toLowerCase().includes(kw))
      : rawList
    this.setData({ list })
  },

  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value })
    this.applyKeyword()
  },

  onCategoryFilter(e) {
    const val = e.currentTarget.dataset.val
    const next = this.data.filterCategory === val ? '' : val
    this.setData({ filterCategory: next, page: 1, keyword: '' })
    this.loadQuestions()
  },

  prevPage() {
    if (this.data.page <= 1) return
    this.setData({ page: this.data.page - 1 })
    this.loadQuestions()
  },

  nextPage() {
    if (this.data.page >= this.data.totalPages) return
    this.setData({ page: this.data.page + 1 })
    this.loadQuestions()
  },

  retry() {
    this.setData({ page: 1 })
    this.loadQuestions()
  },

  // 切换启用/禁用
  toggleEnabled(e) {
    const { id, enabled } = e.currentTarget.dataset
    if (this.data.togglingId === id) return
    const nextEnabled = !enabled
    this.setData({ togglingId: id })

    wx.cloud.callFunction({
      name: 'adminGetQuestions',
      data: { skipAdminCheck: true, action: 'toggleEnabled', questionId: id, enabled: nextEnabled }
    }).then(res => {
      this.setData({ togglingId: '' })
      if (res.result && res.result.code === 0) {
        // 本地同步状态，不重新请求
        const rawList = this.data.rawList.map(q =>
          q._id === id ? { ...q, enabled: nextEnabled } : q
        )
        this.setData({ rawList })
        this.applyKeyword()
      } else {
        wx.showToast({ title: '操作失败', icon: 'none' })
      }
    }).catch(() => {
      this.setData({ togglingId: '' })
      wx.showToast({ title: '网络错误', icon: 'none' })
    })
  },

  openDetail(e) {
    const id = e.currentTarget.dataset.id
    const q = this.data.rawList.find(q => q._id === id)
    if (!q) return
    const optionsWithCorrect = (q.optionsFormatted || []).map((text, i) => ({
      text,
      isCorrect: q.correctAnswer === OPTION_LABELS[i]
    }))
    this.setData({ detailVisible: true, detailQuestion: { ...q, optionsWithCorrect } })
  },

  closeDetail() {
    this.setData({ detailVisible: false, detailQuestion: null })
  },

  stopPropagation() {},

  onPullDownRefresh() {
    this.setData({ page: 1, keyword: '' })
    this.loadQuestions()
    wx.stopPullDownRefresh()
  }
})
