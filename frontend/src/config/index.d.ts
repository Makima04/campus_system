interface ServerConfig {
    baseUrl: string;
    apiPrefix: string;
    port: number;
}
export declare const serverConfig: ServerConfig;
/**
 * 获取API基础URL
 * @returns API基础URL
 */
export declare function getApiBaseUrl(): string;
export declare const appConfig: {
    appName: string;
    version: string;
    frontendPort: number;
};
export {};
