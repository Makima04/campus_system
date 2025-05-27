/**
 * 路由配置
 */
import { createRouter, createWebHistory, RouteLocationNormalized, NavigationGuardNext, RouteRecordRaw } from 'vue-router'
import homeRoutes from '@/modules/home/routes'
import authRoutes from '@/modules/auth/routes'
import { routes as userRoutes } from '@/modules/user'
import adminRoutes from '@/modules/admin/routes'
import teacherRoutes from '@/modules/teacher/routes'
import { routes as fileRoutes } from '@/modules/file'
import { routes as studentRoutes } from '@/modules/student'
import { routes as permissionRoutes } from '@/modules/permission'
import dataRoutes from '@/modules/data/routes'
import { NotFound } from '@/modules/common/components'
// @ts-ignore
import store from '../store'

// 合并所有模块的路由
const routes: RouteRecordRaw[] = [
  ...homeRoutes,
  ...authRoutes,
  ...userRoutes,
  ...adminRoutes,
  ...teacherRoutes,
  ...fileRoutes,
  ...studentRoutes,
  ...permissionRoutes,
  ...dataRoutes,
  {
    path: '/system/:pathMatch(.*)*',
    redirect: '/admin/redis'
  },
  {
    path: '/dashboard',
    redirect: (to) => {
      const userRole = localStorage.getItem('role');
      // 标准化角色格式
      const normalizedRole = userRole?.toUpperCase();
      if (normalizedRole === 'ROLE_ADMIN') {
        return '/admin';
      } else if (normalizedRole === 'ROLE_TEACHER') {
        return '/teacher/dashboard';
      } else if (normalizedRole === 'ROLE_STUDENT') {
        return '/student/dashboard';
      } else {
        return '/';
      }
    }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: NotFound
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound
  }
]

console.log('已加载路由配置:', routes)

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const isLoggedIn = !!localStorage.getItem('token')
  const userRole = localStorage.getItem('role')

  console.log('路由导航:', to.path, '是否登录:', isLoggedIn, '用户角色:', userRole)

  // 需要认证的页面
  if (to.meta.requiresAuth && !isLoggedIn) {
    console.log('需要登录，重定向到登录页')
    next('/login')
    return
  }

  // 角色限制
  if (to.meta.roles && userRole && Array.isArray(to.meta.roles)) {
    // 标准化用户角色
    const normalizedUserRole = userRole.toUpperCase();
    // 检查是否匹配任何允许的角色
    const hasPermission = to.meta.roles.some(role => {
      const normalizedRole = role.toUpperCase();
      return normalizedUserRole === normalizedRole;
    });

    if (!hasPermission) {
      console.log('没有访问权限，重定向到403页面')
      next('/403')
      return
    }
  }

  // 登录后不能访问登录页
  if (to.path === '/login' && isLoggedIn) {
    console.log('已登录，不能访问登录页，重定向到首页')
    next('/')
    return
  }

  console.log('导航到:', to.path)
  next()
})

export default router