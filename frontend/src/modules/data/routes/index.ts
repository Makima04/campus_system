/**
 * 数据管理模块路由配置
 */

import { RouteRecordRaw } from 'vue-router'

// 数据管理模块路由
const dataRoutes: RouteRecordRaw[] = [
  {
    path: '/admin/course-selection-management',
    redirect: '/admin/data-management'
  },
  {
    path: '/admin/meal-card-management',
    redirect: '/admin/data-management'
  }
]

export default dataRoutes 