/**
 * 环境配置文件
 * 用于声明不同环境下的配置
 */
// 开发环境配置
var developmentConfig = {
    serverHost: 'localhost',
    serverPort: 80,
    frontendPort: 5173,
    apiPrefix: '/api',
    env: 'development'
};
// 生产环境配置
var productionConfig = {
    serverHost: '47.109.158.246',
    serverPort: 80,
    frontendPort: 5173,
    apiPrefix: '/api',
    env: 'production'
};
// 当前环境 (development 或 production)
var currentEnv = process.env.NODE_ENV || 'development';
// 根据当前环境导出对应的配置
export var config = currentEnv === 'production' ? productionConfig : developmentConfig;
console.log('当前环境:', config.env);
console.log('服务器地址:', "".concat(config.serverHost, ":").concat(config.serverPort));
// 为了兼容性也导出所有配置
export var configs = {
    development: developmentConfig,
    production: productionConfig
};
