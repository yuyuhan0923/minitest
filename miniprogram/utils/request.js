// ─────────────────────────────────────────────
// utils/request.js  统一请求封装
// ─────────────────────────────────────────────
const { BASE_URL } = require('./config')

/**
 * 发起请求
 * @param {object} options
 * @param {string}  options.url     接口路径（不含域名）
 * @param {string}  [options.method='GET']
 * @param {object}  [options.data]
 * @param {boolean} [options.auth=true]  是否携带 token
 * @returns {Promise<any>}  resolve data 字段，reject 时含 code+message
 */
const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token') || ''
    const header = { 'Content-Type': 'application/json' }
    if (options.auth !== false && token) {
      header['Authorization'] = `Bearer ${token}`
    }

    wx.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header,
      success(res) {
        const body = res.data
        if (body && body.code === 200) {
          resolve(body.data)
        } else {
          // token 过期，跳回登录
          if (body && body.code === 401) {
            wx.removeStorageSync('token')
            wx.reLaunch({ url: '/pages/login/login' })
          }
          const msg = (body && body.message) || '请求失败'
          wx.showToast({ title: msg, icon: 'none', duration: 2000 })
          reject({ code: body && body.code, message: msg })
        }
      },
      fail(err) {
        wx.showToast({ title: '网络异常，请检查连接', icon: 'none', duration: 2500 })
        reject({ code: -1, message: '网络异常', detail: err })
      }
    })
  })
}

// 语法糖
const get  = (url, data, options = {}) => request({ ...options, url, method: 'GET',  data })
const post = (url, data, options = {}) => request({ ...options, url, method: 'POST', data })

module.exports = { request, get, post }
