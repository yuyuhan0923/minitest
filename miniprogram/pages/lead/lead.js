Page({
  data: {
    phone: '',
    wechat: '',
    nickname: '',
    submitting: false,
    materialList: [
      'AI算法路线图PDF', 'Python数据分析模板',
      '机器学习速查表', 'PyTorch模板',
      '面试题100道', '实战案例集'
    ],
    teacherWechatId: 'sxy15205811430',
    teacherQrcodeUrl: '/images/teacher_wechat.png',
    step: 'form'
  },

  onLoad() {
    // 从云数据库读取配置，读不到就用 data 里的默认值
    wx.cloud.callFunction({ name: 'getConfig' })
      .then(res => {
        const cfg = (res.result && res.result.contact) || {}
        this.setData({
          teacherWechatId:  cfg.teacherWechatId  || this.data.teacherWechatId,
          teacherQrcodeUrl: cfg.teacherQrcodeUrl || this.data.teacherQrcodeUrl,
          materialList:     (res.result && res.result.materialList) || this.data.materialList
        })
      })
      .catch(() => {})
  },

  onPhoneInput(e)    { this.setData({ phone: e.detail.value }) },
  onWechatInput(e)   { this.setData({ wechat: e.detail.value }) },
  onNicknameInput(e) { this.setData({ nickname: e.detail.value }) },

  submitLead() {
    const { phone, wechat, nickname } = this.data
    if (!phone && !wechat) {
      wx.showToast({ title: '请填写手机号或微信号', icon: 'none' })
      return
    }
    if (phone && !/^1[3-9]\d{9}$/.test(phone)) {
      wx.showToast({ title: '手机号格式不正确', icon: 'none' })
      return
    }

    this.setData({ submitting: true })
    wx.cloud.callFunction({
      name: 'submitLead',
      data: { phone, wechat, nickname }
    }).finally(() => {
      // 无论成功失败都进入成功页，不阻断用户体验
      this.setData({ step: 'success', submitting: false })
    })
  },

  copyWechat() {
    const id = this.data.teacherWechatId
    if (!id) return
    wx.setClipboardData({
      data: id,
      success: () => wx.showToast({ title: '微信号已复制', icon: 'success' })
    })
  },

  saveQrcode() {
    const path = this.data.teacherQrcodeUrl
    if (!path) { wx.showToast({ title: '二维码未配置', icon: 'none' }); return }
    wx.previewImage({ current: path, urls: [path],
      fail: () => wx.showToast({ title: '请长按图片保存', icon: 'none' })
    })
  },

  goBack() { wx.navigateBack() }
})
