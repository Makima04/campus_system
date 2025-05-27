/**
 * 管理员模块入口
 */

// 导出路由
export { default as routes } from './routes';

// 导出类型
export * from './types/course';

// 导出服务
export { default as courseService } from './api/course-service';

// 模块注册函数，用于向应用注册此模块
export const register = (app: any) => {
  // 注册模块特定的全局组件
  // app.component('AdminLayout', Layout);
  
  // 注册模块特定的指令
  // app.directive('admin-permission', permissionDirective);
  
  console.log('Admin module registered');
}; 