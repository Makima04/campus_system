/**
 * 学生模块路由配置
 */

const studentRoutes = [
  {
    path: '/student/dashboard',
    name: 'StudentDashboard',
    component: () => import('../views/StudentDashboard.vue'),
    meta: {
      requiresAuth: true,
      roles: ['ROLE_STUDENT']
    }
  },
  {
    path: '/student/profile',
    name: 'StudentProfile',
    component: () => import('../views/StudentProfile.vue'),
    meta: {
      requiresAuth: true,
      roles: ['ROLE_STUDENT']
    }
  },
  {
    path: '/student/grades',
    name: 'StudentGrades',
    component: () => import('../views/StudentGrades.vue'),
    meta: {
      requiresAuth: true,
      roles: ['ROLE_STUDENT']
    }
  },
  {
    path: '/student/schedule',
    name: 'StudentSchedule',
    component: () => import('../views/StudentSchedule.vue'),
    meta: {
      requiresAuth: true,
      roles: ['ROLE_STUDENT']
    }
  },
  {
    path: '/student/course-selection',
    name: 'CourseSelection',
    component: () => import('../views/CourseSelection.vue'),
    meta: {
      requiresAuth: true,
      roles: ['ROLE_STUDENT']
    }
  }
]

export default studentRoutes 