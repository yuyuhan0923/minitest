import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const username = ref(localStorage.getItem('admin_username') || '')
  const nickname = ref(localStorage.getItem('admin_nickname') || '')

  function setAuth(data) {
    token.value = data.token
    username.value = data.username
    nickname.value = data.nickname
    localStorage.setItem('admin_token', data.token)
    localStorage.setItem('admin_username', data.username)
    localStorage.setItem('admin_nickname', data.nickname)
  }

  function logout() {
    token.value = ''
    username.value = ''
    nickname.value = ''
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_username')
    localStorage.removeItem('admin_nickname')
  }

  return { token, username, nickname, setAuth, logout }
})
