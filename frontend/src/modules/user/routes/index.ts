/**
 * 用户模块路由配置
 */

const userRoutes = [
  {
    path: '/user/profile',
    name: 'UserProfile',
    component: () => import('../views/UserProfileView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/user/settings',
    name: 'UserSettings',
    component: () => import('../views/UserSettingsView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/user/management',
    name: 'UserManagement',
    component: () => import('../views/UserManagement.vue'),
    meta: {
      requiresAuth: true,
      roles: ['ROLE_ADMIN']
    }
  }
]

export default userRoutes 