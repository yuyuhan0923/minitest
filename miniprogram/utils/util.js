// ─────────────────────────────────────────────
// utils/util.js  通用工具函数
// ─────────────────────────────────────────────

/** 格式化日期  2026-05-27T10:00:00 → 2026-05-27 10:00 */
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').slice(0, 16)
}

/** 手机号脱敏  13812345678 → 138****5678 */
const maskPhone = (phone) => {
  if (!phone || phone.length < 7) return phone
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}

/** 等级 code → 中文 */
const levelLabel = (code) => {
  const map = { beginner: '入门级', intermediate: '进阶级', expert: '专家级' }
  return map[code] || code
}

/** 等级 code → 颜色 */
const levelColor = (code) => {
  const map = { beginner: '#6366f1', intermediate: '#f59e0b', expert: '#10b981' }
  return map[code] || '#999'
}

/** 防抖 */
const debounce = (fn, delay = 500) => {
  let timer = null
  return function (...args) {
    clearTimeout(timer)
    timer = setTimeout(() => fn.apply(this, args), delay)
  }
}

/** 深拷贝（简版） */
const deepClone = (obj) => JSON.parse(JSON.stringify(obj))

module.exports = { formatDate, maskPhone, levelLabel, levelColor, debounce, deepClone }
