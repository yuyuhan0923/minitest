Page({
  data: {
    username: "",
    password: ""
  },

  onUsernameInput(e) {
    this.setData({ username: e.detail.value })
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value })
  },

  onLogin() {
    const { username, password } = this.data
    if (username === "admin" && password === "admin123") {
      
      // ✅ 强制记录登录时间（绝对不会丢）
      let now = new Date().getTime()
      wx.setStorageSync('isAdmin', true)
      wx.setStorageSync('adminLoginTime', now)

      // ✅ 强制跳转，确保时间被保存
      wx.reLaunch({ url: "/pages/admin/dashboard/dashboard" })

    } else {
      wx.showToast({ title: "账号或密码错误", icon: "none" })
    }
  }
})