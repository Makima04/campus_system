/**
 * 公共模块入口
 * 集中导出公共组件和工具
 */

// 导出图标组件
export { default as IconDocumentation } from './components/icons/IconDocumentation.vue';
export { default as IconTooling } from './components/icons/IconTooling.vue';
export { default as IconEcosystem } from './components/icons/IconEcosystem.vue';
export { default as IconCommunity } from './components/icons/IconCommunity.vue';
export { default as IconSupport } from './components/icons/IconSupport.vue';

// 模块注册函数，用于向应用注册此模块
export function registerModule(app: any) {
  console.log('注册公共模块');
  // 可以在这里注册全局组件、指令等
} 