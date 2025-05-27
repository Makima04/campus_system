/**
 * 文件模块路由配置
 */

import { RouteRecordRaw } from 'vue-router'

const fileRoutes: RouteRecordRaw[] = [
  {
    path: '/file/demo',
    redirect: '/admin/file'
  }
]

export default fileRoutes 