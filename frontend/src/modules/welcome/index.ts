/**
 * 欢迎模块入口
 * 集中导出欢迎模块的组件
 */

// 导出组件
export { default as HelloWorld } from './components/HelloWorld.vue';
export { default as WelcomeItem } from './components/WelcomeItem.vue';
export { default as TheWelcome } from './components/TheWelcome.vue';

// 模块注册函数，用于向应用注册此模块
export function registerModule(app: any) {
  console.log('注册欢迎模块');
  // 可以在这里注册全局组件、指令等
} 