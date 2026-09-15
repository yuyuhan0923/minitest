const app = getApp()

Page({
  data: {
    courseName: "AI算法就业实战班",
    coursePrice: "¥799",
    goodsName: "AI算法测评专业版",
    price: "799",
    desc: "解锁全部题库 + 无限次测评 + 专属报告",
    teacherWechatId: "sxy15205811430",
    showBuyPopup: false,
    showTeacherModal: false
  },

  onLoad(options) {},

  // 打开购买弹窗
  goBuy() {
    this.setData({ showBuyPopup: true })
  },

  // 关闭购买弹窗
  closeBuyPopup() {
    this.setData({ showBuyPopup: false })
  },

  stopClose() {},

  // 购买
  onBuy() {
    if (app.recordBuyClick) {
      app.recordBuyClick()
    }
    wx.showToast({
      title: "购买成功",
      icon: "success"
    })
    this.setData({ showBuyPopup: false })
  },

  // 立即咨询
  consultTeacher() {
    this.setData({ showTeacherModal: true })
  },

  // 复制微信号
  copyWechat() {
    wx.setClipboardData({
      data: this.data.teacherWechatId,
      success() {
        wx.showToast({ title: "已复制", icon: "success" })
      }
    })
  },

  // 关闭弹窗
  closePopup() {
    this.setData({ showTeacherModal: false })
  },

  noop() {},

  // 进群试听
  joinGroup() {
    wx.navigateTo({ url: "/pages/group/group" })
  },

  // 免费试听
  joinTrial() {
    wx.showToast({ title: "试听开发中", icon: "none" })
  },

  onShareAppMessage() {
    return {
      title: "AI算法测评",
      path: "/pages/index/index"
    }
  }
})