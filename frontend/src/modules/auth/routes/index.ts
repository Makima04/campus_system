/**
 * 认证模块路由配置
 */

const authRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  }
]

export default authRoutes 