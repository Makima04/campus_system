// 系统配置文件
// 集中管理所有API地址和其他配置信息
import { config } from './env-config';

interface ServerConfig {
  baseUrl: string;
  apiPrefix: string;
  port: number;
}

// 服务器配置
export const serverConfig: ServerConfig = {
  baseUrl: `http://${config.serverHost}`,
  apiPrefix: '/api',
  port: config.serverPort
};

/**
 * 获取API基础URL
 * @returns API基础URL
 */
export function getApiBaseUrl() {
  // 使用相对路径连接后端API
  // 这样在开发环境中可以通过代理访问后端
  // 在生产环境中可以通过同源请求访问
  console.log('使用相对路径API');
  return ``;
}

// 其他前端配置
export const appConfig = {
  appName: '校园管理系统',
  version: '1.0.0',
  frontendPort: config.frontendPort
}; 