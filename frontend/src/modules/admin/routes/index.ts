/**
 * 管理员模块路由配置
 */

import { RouteRecordRaw } from 'vue-router'

// 管理员模块路由
const adminRoutes: RouteRecordRaw[] = [
  {
    path: '/admin',
    name: 'AdminLayout',
    component: () => import('../layout/AdminLayout.vue'),
    meta: {
      requiresAuth: true,
      roles: ['ROLE_ADMIN']
    },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('../views/Admin.vue'),
        meta: {
          title: '管理员面板',
          requiresAuth: true,
          roles: ['ROLE_ADMIN']
        }
      },
      {
        path: 'user',
        name: 'UserManagement',
        component: () => import('../views/UserManagement.vue'),
        meta: {
          title: '用户管理',
          requiresAuth: true,
          roles: ['ROLE_ADMIN']
        }
      },
      {
        path: 'student',
        name: 'StudentManagement',
        component: () => import('../views/StudentManagement.vue'),
        meta: {
          title: '学生管理',
          requiresAuth: true,
          roles: ['ROLE_ADMIN']
        }
      },
      {
        path: 'redis',
        name: 'RedisManagement',
        component: () => import('../views/RedisManagement.vue'),
        meta: {
          title: 'Redis缓存管理',
          requiresAuth: true,
          roles: ['ROLE_ADMIN']
        }
      },
      {
        path: 'data-management',
        name: 'DataManagement',
        component: () => import('../views/DataManagement.vue'),
        meta: {
          title: '数据管理',
          requiresAuth: true,
          roles: ['ROLE_ADMIN']
        }
      },
      {
        path: 'course-management',
        name: 'CourseManagement',
        component: () => import('../views/CourseManagement.vue'),
        meta: {
          title: '课程管理',
          requiresAuth: true,
          roles: ['ROLE_ADMIN']
        }
      },
      {
        path: 'file',
        name: 'FileManagement',
        component: () => import('../../file/views/UploadDemo.vue'),
        meta: {
          title: '文件上传',
          requiresAuth: true,
          roles: ['ROLE_ADMIN']
        }
      },
      {
        path: 'logs',
        name: 'LogManagement',
        component: () => import('../views/LogManagement.vue'),
        meta: {
          title: '系统日志',
          requiresAuth: true,
          roles: ['ROLE_ADMIN']
        }
      },
      {
        path: 'archived-logs',
        name: 'ArchivedLogManagement',
        component: () => import('../views/ArchivedLogManagement.vue'),
        meta: {
          title: '归档日志',
          requiresAuth: true,
          roles: ['ROLE_ADMIN']
        }
      }
    ]
  },
  {
    path: '/admin/dashboard',
    redirect: '/admin',
    meta: {
      requiresAuth: true,
      roles: ['ROLE_ADMIN']
    }
  },
  {
    path: '/admin/course',
    redirect: '/admin/course-management',
    meta: {
      requiresAuth: true,
      roles: ['ROLE_ADMIN']
    }
  },
  {
    path: '/admin/course-selection-management',
    name: 'CourseSelectionManagement',
    component: () => import('../views/CourseSelectionManagement.vue'),
    meta: {
      requiresAuth: true,
      roles: ['ROLE_ADMIN']
    }
  },
  {
    path: '/admin/meal-card-management',
    name: 'MealCardManagement',
    component: () => import('../views/MealCardManagement.vue'),
    meta: {
      requiresAuth: true,
      roles: ['ROLE_ADMIN']
    }
  },
  {
    path: '/admin/meal-card-transaction-management',
    name: 'MealCardTransactionManagement',
    component: () => import('../views/MealCardTransactionManagement.vue'),
    meta: {
      requiresAuth: true,
      roles: ['ROLE_ADMIN']
    }
  }
]

export default adminRoutes 