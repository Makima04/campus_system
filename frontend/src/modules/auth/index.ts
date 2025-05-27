/**
 * 认证模块入口
 * 集中导出认证模块的路由和组件
 */

// 导出路由
export { default as routes } from './routes';

// 模块注册函数，用于向应用注册此模块
export function registerModule(app: any) {
  console.log('注册认证模块');
  // 可以在这里注册全局组件、指令等
} 