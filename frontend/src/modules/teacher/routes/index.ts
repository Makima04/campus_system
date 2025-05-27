/**
 * 教师相关路由配置
 */

const teacherRoutes = [
  {
    path: '/teacher/dashboard',
    name: 'TeacherDashboard',
    component: () => import('../views/TeacherDashboard.vue'),
    meta: { requiresAuth: true, roles: ['teacher'] }
  },
  {
    path: '/teacher/course',
    name: 'TeacherCourse',
    component: () => import('../views/TeacherCourse.vue'),
    meta: { requiresAuth: true, roles: ['teacher'] }
  },
  {
    path: '/teacher/assignments',
    name: 'TeacherAssignments',
    component: () => import('../views/TeacherAssignments.vue'),
    meta: { requiresAuth: true, roles: ['teacher'] }
  },
  {
    path: '/teacher/grades',
    name: 'TeacherGrades',
    component: () => import('../views/TeacherGrades.vue'),
    meta: { requiresAuth: true, roles: ['teacher'] }
  }
]

export default teacherRoutes 