/**
 * 环境配置文件
 * 用于声明不同环境下的配置
 */

// 默认环境配置
export interface EnvConfig {
  serverHost: string;
  serverPort: number;
  frontendPort: number;
  apiPrefix: string;
  env: 'development' | 'production';
}

// 开发环境配置
const developmentConfig: EnvConfig = {
  serverHost: 'localhost',
  serverPort: 8080,
  frontendPort: 5173,
  apiPrefix: '/api',
  env: 'development'
};

// 生产环境配置
const productionConfig: EnvConfig = {
  serverHost: '47.109.158.246',
  serverPort: 80,
  frontendPort: 5173,
  apiPrefix: '/api',
  env: 'production'
};

// 当前环境 (development 或 production)
const currentEnv = process.env.NODE_ENV || 'development';

// 根据当前环境导出对应的配置
export const config: EnvConfig =
  currentEnv === 'production' ? productionConfig : developmentConfig;

console.log('当前环境:', config.env);
console.log('服务器地址:', `${config.serverHost}:${config.serverPort}`);

// 为了兼容性也导出所有配置
export const configs = {
  development: developmentConfig,
  production: productionConfig
}; 