/**
 * 权限模块路由配置
 */

const permissionRoutes = [
  {
    path: '/permission',
    component: () => import('../views/PermissionLayout.vue'),
    meta: {
      requiresAuth: true,
      roles: ['ROLE_ADMIN']
    },
    children: [
      {
        path: 'roles',
        name: 'RoleList',
        component: () => import('../views/RoleList.vue'),
        meta: {
          requiresAuth: true,
          roles: ['ROLE_ADMIN']
        }
      },
      {
        path: 'list',
        name: 'PermissionList',
        component: () => import('../views/PermissionList.vue'),
        meta: {
          requiresAuth: true,
          roles: ['ROLE_ADMIN']
        }
      }
    ]
  }
]

export default permissionRoutes 