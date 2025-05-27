/**
 * 全局组件类型声明
 * 为了支持模块化组件的类型检查
 */

// 当使用全局组件时，确保TypeScript能够识别它们
declare module 'vue' {
  export interface GlobalComponents {
    // 通用图标组件
    IconDocumentation: typeof import('./modules/common/components/icons/IconDocumentation.vue')['default']
    IconTooling: typeof import('./modules/common/components/icons/IconTooling.vue')['default']
    IconEcosystem: typeof import('./modules/common/components/icons/IconEcosystem.vue')['default']
    IconCommunity: typeof import('./modules/common/components/icons/IconCommunity.vue')['default']
    IconSupport: typeof import('./modules/common/components/icons/IconSupport.vue')['default']
    
    // 欢迎组件
    HelloWorld: typeof import('./modules/welcome/components/HelloWorld.vue')['default']
    WelcomeItem: typeof import('./modules/welcome/components/WelcomeItem.vue')['default']
    TheWelcome: typeof import('./modules/welcome/components/TheWelcome.vue')['default']
    
    // 文件上传组件
    ChunkFileUploader: typeof import('./modules/file/components/ChunkFileUploader.vue')['default']
  }
} 