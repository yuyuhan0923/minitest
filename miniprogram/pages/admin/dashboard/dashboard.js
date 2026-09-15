const { get } = require('../../../utils/request')
const { API } = require('../../../utils/config')

function levelClass(v) {
  if (v === '高级') return 'level-gao'
  if (v === '中级') return 'level-zhong'
  return 'level-chu'
}

function tagClass(v) {
  if (v === '活跃') return 'tag-huoyue'
  if (v === '新用户') return 'tag-xinyonghu'
  return 'tag-chenmo'
}

Page({
  data: {
    loading: true,
    stats: null,
    loginTime: "",
    panelVisible: false,
    userList: [],
    filteredList: [],
    pagedList: [],
    userLoading: false,
    userError: '',
    userKeyword: '',
    filterLevel: '',
    filterTag: '',
    page: 1,
    pageSize: 10,
    totalPages: 1,
    detailVisible: false,
    detailUser: null,
    detailLoading: false
  },

  onLoad() {
    const isAdmin = wx.getStorageSync('isAdmin')
    if (!isAdmin) {
      wx.showToast({ title: '无权限', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1500)
      return
    }
    try {
      const ts = wx.getStorageSync('adminLoginTime')
      this.setData({ loginTime: ts ? new Date(ts).toLocaleString() : new Date().toLocaleString() })
    } catch (e) {
      this.setData({ loginTime: new Date().toLocaleString() })
    }
    this.loadStats()
  },

  loadStats() {
    this.setData({ loading: true })
    wx.cloud.callFunction({
      name: 'adminGetStats',
      data: { skipAdminCheck: true }
    }).then(res => {
      const result = res.result || {}
      if (result.code === 403) {
        this.setData({ loading: false })
        wx.showToast({ title: '无管理员权限', icon: 'none' })
        return
      }
      if (result.code !== 0 || !result.data) {
        this.setData({ loading: false })
        wx.showToast({ title: result.message || '统计加载失败', icon: 'none' })
        return
      }
      const d = result.data
      this.setData({
        stats: {
          visitCount:    d.visitCount    || 0,
          totalUsers:    d.registerCount || 0,
          totalExam:     d.examCount     || 0,
          totalLead:     d.leadCount     || 0,
          buyClickCount: d.purchaseCount || 0,
          conversionRate: d.conversionRate || '0.0%'
        },
        loading: false
      })
    }).catch(err => {
      this.setData({ loading: false })
      const msg = (err && err.message) || '云函数调用失败'
      wx.showToast({ title: msg, icon: 'none' })
    })
  },

  openUserPanel() {
    this.setData({ panelVisible: true, userKeyword: '', filterLevel: '', filterTag: '', page: 1 })
    this.loadUsers()
  },

  closeUserPanel() {
    this.setData({ panelVisible: false })
  },

  loadUsers() {
    this.setData({ userLoading: true, userError: '' })
    wx.cloud.callFunction({ name: 'adminGetUsers', data: { skipAdminCheck: true } })
      .then(res => {
        const result = res.result || {}
        if (result.code === 403) {
          this.setData({ userLoading: false, userError: '无管理员权限' })
          return
        }
        const list = Array.isArray(result.data) ? result.data : []
        this.setData({ userList: list, userLoading: false, userError: '' })
        this.applyFilter()
      })
      .catch(err => {
        const msg = (err && err.message) || '云函数调用失败，请检查网络或云环境配置'
        this.setData({ userLoading: false, userError: msg, pagedList: [], filteredList: [] })
      })
  },

  onKeywordInput(e) {
    this.setData({ userKeyword: e.detail.value, page: 1 })
    this.applyFilter()
  },

  onLevelFilter(e) {
    const val = e.currentTarget.dataset.val
    this.setData({ filterLevel: this.data.filterLevel === val ? '' : val, page: 1 })
    this.applyFilter()
  },

  onTagFilter(e) {
    const val = e.currentTarget.dataset.val
    this.setData({ filterTag: this.data.filterTag === val ? '' : val, page: 1 })
    this.applyFilter()
  },

  applyFilter() {
    const { userList, userKeyword, filterLevel, filterTag, page, pageSize } = this.data
    const kw = (userKeyword || '').trim().toLowerCase()
    let filtered = userList

    if (kw) {
      filtered = filtered.filter(u =>
        (u.nickname || '').toLowerCase().includes(kw) ||
        (u.phone || '').includes(kw) ||
        (u.openid || '').toLowerCase().includes(kw)
      )
    }
    if (filterLevel) {
      filtered = filtered.filter(u => u.assessLevel === filterLevel)
    }
    if (filterTag) {
      filtered = filtered.filter(u => u.behaviorTag === filterTag)
    }

    const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize))
    const start = (page - 1) * pageSize
    const pagedList = filtered.slice(start, start + pageSize).map(u => ({
      ...u,
      levelClass: levelClass(u.assessLevel),
      tagClass: tagClass(u.behaviorTag)
    }))

    this.setData({ filteredList: filtered, pagedList, totalPages })
  },

  prevPage() {
    if (this.data.page <= 1) return
    this.setData({ page: this.data.page - 1 })
    this.applyFilter()
  },

  nextPage() {
    if (this.data.page >= this.data.totalPages) return
    this.setData({ page: this.data.page + 1 })
    this.applyFilter()
  },

  openDetail(e) {
    const openid = e.currentTarget.dataset.openid
    const user = this.data.userList.find(u => u.openid === openid)
    if (!user) return
    this.setData({
      detailVisible: true,
      detailLoading: false,
      detailUser: {
        ...user,
        levelClass: levelClass(user.assessLevel),
        tagClass: tagClass(user.behaviorTag)
      }
    })
  },

  closeDetail() {
    this.setData({ detailVisible: false })
  },

  stopPropagation() {},

  go(e) {
    const url = e.currentTarget.dataset.url
    url ? wx.navigateTo({ url }) : wx.showToast({ title: '页面未配置', icon: 'none' })
  },

  onPullDownRefresh() {
    this.loadStats()
    wx.stopPullDownRefresh()
  },

  logout() {
    wx.removeStorageSync('isAdmin')
    wx.removeStorageSync('adminLoginTime')
    wx.reLaunch({ url: '/pages/admin/login/login' })
  }
})
