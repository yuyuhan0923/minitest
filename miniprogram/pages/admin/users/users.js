const { get } = require('../../../utils/request')
const { API } = require('../../../utils/config')

Page({
  data: {
    list: [],
    loading: false
  },

  onLoad() {
    const isAdmin = wx.getStorageSync('isAdmin')
    if (!isAdmin) {
      wx.redirectTo({ url: '/pages/admin/login/login' })
      return
    }
    wx.setNavigationBarTitle({ title: '用户管理' })
    this.loadUsers()
  },

  loadUsers() {
    if (this.data.loading) return
    this.setData({ loading: true })
    get(API.ADMIN_USERS)
      .then(data => {
        this.setData({ list: Array.isArray(data) ? data : [], loading: false })
      })
      .catch(() => {
        this.setData({ loading: false })
      })
  },

  onPullDownRefresh() {
    this.loadUsers()
    wx.stopPullDownRefresh()
  }
})
