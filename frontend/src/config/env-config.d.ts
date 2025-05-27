/**
 * 环境配置文件
 * 用于声明不同环境下的配置
 */
export interface EnvConfig {
    serverHost: string;
    serverPort: number;
    frontendPort: number;
    apiPrefix: string;
    env: 'development' | 'production';
}
export declare const config: EnvConfig;
export declare const configs: {
    development: EnvConfig;
    production: EnvConfig;
};
