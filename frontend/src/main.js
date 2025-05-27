import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import store from './store';
import './assets/main.css';
import { createPinia } from 'pinia';

// Element Plus
import 'element-plus/dist/index.css';
import ElementPlus from 'element-plus';
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
// Element Plus 图标
import * as ElementPlusIcons from '@element-plus/icons-vue';
import { loadServerConfig } from './utils/api';
import { registerAllModules } from './modules';

// 添加全局错误处理
const handleError = (error) => {
  console.error('应用错误:', error);
};

// 使用IIFE加载服务器配置，然后初始化应用
(async () => {
  try {
    // 首先尝试从后端加载配置
    await loadServerConfig();
    
    // 然后初始化Vue应用
    const app = createApp(App);
    
    // 配置Vue应用
    const pinia = createPinia();
    app.use(pinia);
    app.use(router);
    app.use(store);
    app.use(ElementPlus, {
      locale: zhCn,
    });
    
    // 注册所有Element Plus图标
    for (const [key, component] of Object.entries(ElementPlusIcons)) {
      app.component(key, component);
    }
    
    // 注册所有模块
    registerAllModules(app);
    
    // 添加错误处理
    app.config.errorHandler = handleError;
    
    // 挂载应用
    app.mount('#app');
    
    console.log('应用初始化完成，已加载服务器配置');
    console.log('应用已启动，运行在:', window.location.href);
  } catch (error) {
    console.error('应用初始化失败:', error);
    
    // 即使配置加载失败，也继续初始化应用
    const app = createApp(App);
    
    // 配置Vue应用
    const pinia = createPinia();
    app.use(pinia);
    app.use(router);
    app.use(store);
    app.use(ElementPlus, {
      locale: zhCn,
    });
    
    // 注册所有Element Plus图标
    for (const [key, component] of Object.entries(ElementPlusIcons)) {
      app.component(key, component);
    }
    
    // 注册所有模块
    registerAllModules(app);
    
    // 添加错误处理
    app.config.errorHandler = handleError;
    
    // 挂载应用
    app.mount('#app');
    
    console.log('应用已启动，运行在:', window.location.href);
  }
})();
