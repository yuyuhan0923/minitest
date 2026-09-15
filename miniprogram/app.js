// app.js
App({
  onLaunch() {
    if (!wx.cloud) {
      console.error('请使用 2.2.3 或以上的基础库以使用云能力');
    } else {
      wx.cloud.init({
        // 👇 这里填你真实的环境 ID
        env: 'cloud1-d0gniq41w706a6006',
        traceUser: true
      });
    }
    // 启动时恢复登录状态
    this._restoreLogin()
  },

  globalData: {
    isLoggedIn: false,
    userInfo: null,   // { openid, nickName, avatarUrl }
  },

  // ─── 登录状态管理（新增，不影响原有逻辑）─────────────────────

  // 启动时从 Storage 恢复登录状态
  _restoreLogin() {
    const token = wx.getStorageSync('token')
    const userInfo = wx.getStorageSync('userInfo')
    if (token && userInfo) {
      this.globalData.isLoggedIn = true
      this.globalData.userInfo = userInfo
    }
  },

  // 判断是否已登录
  isLoggedIn() {
    return !!wx.getStorageSync('token')
  },

  // 执行登录：调用 login 云函数获取 openid，写入 users 集合
  // nickName/avatarUrl 可选，由登录页的头像昵称组件填写后传入
  // 成功回调 onSuccess(userInfo)，失败回调 onFail()
  doLogin(onSuccess, onFail, profileData) {
    wx.showLoading({ title: '登录中...' })

    const { nickName = '微信用户', avatarUrl = '' } = profileData || {}

    wx.cloud.callFunction({
      name: 'login',
      data: { nickName, avatarUrl }
    }).then(res => {
      wx.hideLoading()
      const result = res.result || {}
      if (result.code !== 200) {
        wx.showToast({ title: '登录失败，请重试', icon: 'none' })
        onFail && onFail()
        return
      }
      const userInfo = {
        openid: result.openid,
        nickName,
        avatarUrl
      }
      // 持久化
      wx.setStorageSync('token', 'login_success')
      wx.setStorageSync('openId', result.openid)
      wx.setStorageSync('userInfo', userInfo)
      // 更新全局状态
      this.globalData.isLoggedIn = true
      this.globalData.userInfo = userInfo

      onSuccess && onSuccess(userInfo)
    }).catch(() => {
      wx.hideLoading()
      wx.showToast({ title: '网络错误，请重试', icon: 'none' })
      onFail && onFail()
    })
  },

  // 需要登录时的统一入口：已登录直接执行 action，未登录跳转登录页
  requireLogin(action) {
    if (this.isLoggedIn()) {
      action && action()
      return
    }
    // 跳转登录页，登录成功后由登录页自行处理跳转
    wx.navigateTo({ url: '/pages/login/login' })
  }
});