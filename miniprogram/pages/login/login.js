const app = getApp()

Page({
  data: {
    loading: false,
    // 登录成功后进入「完善信息」步骤
    step: 'login',   // 'login' | 'profile'
    // 用户填写的头像昵称（微信官方组件回填）
    avatarUrl: '',
    nickName: '',
    features: [
      { icon: '📝', text: '20道精选AI算法题，覆盖4大方向' },
      { icon: '📊', text: '完成后获得专属能力分析报告' },
      { icon: '🎯', text: '针对薄弱项生成个性化提升建议' },
      { icon: '🚀', text: '免费领取AI算法学习路径规划' }
    ]
  },

  // ── 第一步：点击「微信一键登录」，调用云函数获取 openid ──────
  handleLogin() {
    if (this.data.loading) return
    this.setData({ loading: true })

    app.doLogin(
      (userInfo) => {
        this.setData({
          loading: false,
          step: 'profile',
          // 如果已有昵称（老用户），直接回填
          nickName: userInfo.nickName !== '微信用户' ? userInfo.nickName : '',
          avatarUrl: userInfo.avatarUrl || ''
        })
      },
      () => {
        this.setData({ loading: false })
      }
    )
  },

  // ── 第二步：用户选择头像（open-type="chooseAvatar"）──────────
  onChooseAvatar(e) {
    const { avatarUrl } = e.detail
    this.setData({ avatarUrl })
  },

  // ── 第二步：用户输入昵称（type="nickname" 失焦时触发）────────
  onNicknameInput(e) {
    this.setData({ nickName: e.detail.value })
  },

  // ── 第二步：点击「完成」，上传头像到云存储后保存 ──────────────
  handleSaveProfile() {
    const { nickName, avatarUrl } = this.data
    const displayName = nickName.trim() || '微信用户'

    wx.showLoading({ title: '保存中...' })

    // 如果有头像且是临时路径，先上传到云存储获取永久 URL
    const uploadAndSave = (finalAvatarUrl) => {
      wx.cloud.callFunction({
        name: 'login',
        data: { nickName: displayName, avatarUrl: finalAvatarUrl }
      }).then(() => {
        wx.hideLoading()
        const stored = wx.getStorageSync('userInfo') || {}
        const updated = { ...stored, nickName: displayName, avatarUrl: finalAvatarUrl }
        wx.setStorageSync('userInfo', updated)
        if (app.globalData.userInfo) {
          app.globalData.userInfo.nickName = displayName
          app.globalData.userInfo.avatarUrl = finalAvatarUrl
        }
        wx.showToast({ title: '登录成功', icon: 'success' })
        setTimeout(() => {
          wx.switchTab({ url: '/pages/index/index' })
        }, 1200)
      }).catch(() => {
        wx.hideLoading()
        wx.switchTab({ url: '/pages/index/index' })
      })
    }

    // 临时路径（http://tmp/ 开头）需要先上传到云存储
    if (avatarUrl && avatarUrl.startsWith('http://tmp')) {
      const openid = wx.getStorageSync('openId') || 'unknown'
      const ext = avatarUrl.split('.').pop().split('?')[0] || 'jpg'
      const cloudPath = `avatars/${openid}_${Date.now()}.${ext}`

      wx.cloud.uploadFile({
        cloudPath,
        filePath: avatarUrl,
        success: (uploadRes) => {
          uploadAndSave(uploadRes.fileID)
        },
        fail: () => {
          // 上传失败，用临时路径降级保存（本次会话内可显示）
          uploadAndSave(avatarUrl)
        }
      })
    } else {
      // 已是云存储 fileID 或空，直接保存
      uploadAndSave(avatarUrl || '')
    }
  },

  // ── 跳过完善信息，直接进首页 ─────────────────────────────────
  handleSkip() {
    wx.switchTab({ url: '/pages/index/index' })
  }
})
