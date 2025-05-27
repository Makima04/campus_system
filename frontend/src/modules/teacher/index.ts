/**
 * 教师模块入口
 * 集中导出教师模块的API、组件、路由和状态
 */

// 导出路由
export { default as routes } from './routes';

// 模块注册函数，用于向应用注册此模块
export function registerModule(app: any) {
  console.log('注册教师模块');
  // 可以在这里注册全局组件、指令等
} 