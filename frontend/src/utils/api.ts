// API配置工具类
// 负责从后端加载配置，并提供给其他组件使用

import axios from 'axios';
import { config } from '../config/env-config';

// 默认API基础URL
let apiBaseUrl = `http://${config.serverHost}:${config.serverPort}/api`;
let frontendUrl = `http://${config.serverHost}:${config.frontendPort}`;

// 保存后端返回的配置
let serverConfig: any = null;

/**
 * 从后端加载配置信息
 * 在应用启动时调用
 */
export const loadServerConfig = async (): Promise<void> => {
  try {
    // 使用当前的API基础URL尝试加载配置
    const response = await axios.get(`${apiBaseUrl}/config/public`);
    if (response.data) {
      serverConfig = response.data;
      
      // 使用后端配置更新API基础URL
      if (serverConfig.apiBaseUrl) {
        apiBaseUrl = `${serverConfig.apiBaseUrl}/api`;
      }
      
      // 使用后端配置更新前端URL
      if (serverConfig.frontendUrl) {
        frontendUrl = serverConfig.frontendUrl;
      }
      
      console.log('服务器配置已加载:', serverConfig);
    }
  } catch (error) {
    console.error('加载服务器配置失败，使用默认配置:', error);
  }
};

/**
 * 获取API基础URL
 * @returns API基础URL
 */
export function getApiBaseUrl() {
  return `/api`;
}

/**
 * 获取前端URL
 */
export const getFrontendUrl = (): string => {
  return frontendUrl;
};

/**
 * 获取当前环境
 */
export const getEnvironment = (): string => {
  return serverConfig?.env || process.env.NODE_ENV || 'development';
};

/**
 * 获取CORS允许的源列表
 */
export const getCorsAllowedOrigins = (): string[] => {
  return serverConfig?.corsAllowedOrigins || [];
};

/**
 * 获取完整的服务器配置
 */
export const getServerConfig = (): any => {
  return serverConfig || {};
}; 