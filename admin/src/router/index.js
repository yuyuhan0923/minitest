import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { title: '数据看板', icon: 'DataAnalysis' }
      },
      {
        path: 'questions',
        name: 'Questions',
        component: () => import('@/views/questions/Questions.vue'),
        meta: { title: '题库管理', icon: 'Document' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/users/Users.vue'),
        meta: { title: '线索管理', icon: 'User' }
      },
      {
        path: 'reports',
        name: 'Reports',
        component: () => import('@/views/reports/Reports.vue'),
        meta: { title: '测评报告', icon: 'TrendCharts' }
      },
      {
        path: 'all-users',
        name: 'AllUsers',
        component: () => import('@/views/all-users/AllUsers.vue'),
        meta: { title: '用户管理', icon: 'Avatar' }
      },
      {
        path: 'records',
        name: 'Records',
        component: () => import('@/views/records/Records.vue'),
        meta: { title: '测评记录', icon: 'List' }
      },
      {
        path: 'course',
        name: 'Course',
        component: () => import('@/views/course/Course.vue'),
        meta: { title: '课程配置', icon: 'Reading' }
      },
      {
        path: 'actions',
        name: 'Actions',
        component: () => import('@/views/actions/Actions.vue'),
        meta: { title: '行为记录', icon: 'Pointer' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (!to.meta.public && !auth.token) {
    return '/login'
  }
})

export default router
