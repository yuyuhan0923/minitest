// ─────────────────────────────────────────────
// utils/config.js  全局配置
// ─────────────────────────────────────────────

// 【联调配置】本地开发用 localhost，内网穿透时替换为 ngrok 地址
// 例：const BASE_URL = 'https://xxxx.ngrok-free.app'
const BASE_URL = 'http://localhost:8083'

// 接口路径常量，统一维护
const API = {
  LOGIN:                '/api/mini/login',
  QUESTIONS:            '/api/mini/questions',
  EXAM_START:           '/api/mini/exam/start',
  EXAM_SUBMIT:          '/api/mini/exam/submit',
  REPORT:               '/api/mini/report',
  ASSESSMENT_QUESTIONS: '/api/mini/assessment/questions',
  ASSESSMENT_SUBMIT:    '/api/mini/assessment/submit',
  RECORDS:              '/api/mini/records',
  LEAD:                 '/api/mini/lead',
  // 微信联系配置（纯 yml，不依赖数据库）
  WECHAT_CONFIG:        '/api/mini/wechat/config',
  // 课程配置（纯 yml，不依赖数据库）
  COURSE_CONFIG:        '/api/mini/course/config',
  // 转化接口
  MATERIAL_INFO:        '/api/mini/conversion/material/info',
  MATERIAL_CLAIM:       '/api/mini/conversion/material/claim',
  CONTACT_INFO:         '/api/mini/conversion/contact/info',
  ACTION_TRACK:         '/api/mini/conversion/action/track',
  COURSE_INFO:          '/api/mini/conversion/course/info',
  COURSE_CLICK:         '/api/mini/conversion/course/click',
  // 管理后台
  STATS_OVERVIEW:       '/api/admin/stats/overview',
  STATS_DAILY:          '/api/admin/stats/daily',
  ADMIN_USERS:          '/api/admin/users',
}

module.exports = { BASE_URL, API }
