const cloud = require('wx-server-sdk')

cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })

const db = cloud.database()

// 返回联系方式、课程信息、资料列表
// 先从云数据库 config 集合读取，读不到就返回默认值
exports.main = async () => {
  try {
    const { data } = await db.collection('config').limit(1).get()
    if (data.length > 0) return data[0]
  } catch (_) {}

  // 默认兜底配置
  return {
    contact: {
      teacherWechatId:  'sxy15205811430',
      teacherQrcodeUrl: '/images/teacher_wechat.png',
      groupQrcodeUrl:   '/images/study_group.png'
    },
    course: {
      courseName:     'AI算法工程师系统课',
      coursePrice:    '限时优惠价 ¥799',
      courseDiscount: '从Python基础到模型部署，30天掌握AI算法核心技能',
      courseUrl:      null,
      buyUrl:         null
    },
    materialList: [
      'AI算法路线图PDF', 'Python数据分析模板',
      '机器学习速查表', 'PyTorch模板',
      '面试题100道', '实战案例集'
    ]
  }
}
