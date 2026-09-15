import http from './http'

export const adminApi = {
  login: (data) => http.post('/admin/login', data),
  getOverview: () => http.get('/admin/stats/overview'),
  getDailyStats: (days = 7) => http.get('/admin/stats/daily', { params: { days } }),

  // 题库
  getQuestions: (params) => http.get('/admin/questions', { params }),
  getQuestion: (id) => http.get(`/admin/questions/${id}`),
  createQuestion: (data) => http.post('/admin/questions', data),
  updateQuestion: (id, data) => http.put(`/admin/questions/${id}`, data),
  deleteQuestion: (id) => http.delete(`/admin/questions/${id}`),

  // 线索
  getLeads: (params) => http.get('/admin/leads', { params }),
  updateFollowStatus: (id, data) => http.put(`/admin/leads/${id}/follow`, data),

  // 报告
  getReports: (params) => http.get('/admin/reports', { params }),

  // 用户列表
  getUsers: () => http.get('/admin/users'),
  getUserDetail: (openid) => http.get(`/admin/users/${encodeURIComponent(openid)}`),

  // 测评记录
  getRecords: (params) => http.get('/admin/records', { params }),

  // 课程配置
  getCourseConfig: () => http.get('/admin/course'),

  // 用户行为
  getActions: (params) => http.get('/admin/actions', { params })
}
