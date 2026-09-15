import http from './http'

export const getUsers = () => http.get('/admin/users')

export const getUserDetail = (openid) => http.get(`/admin/users/${encodeURIComponent(openid)}`)
