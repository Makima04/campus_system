import { fileURLToPath, URL } from 'node:url';
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import vueJsx from '@vitejs/plugin-vue-jsx';
import vueDevTools from 'vite-plugin-vue-devtools';
// 导入环境配置
import { config } from './src/config/env-config';
// https://vitejs.dev/config/
export default defineConfig({
    plugins: [
        vue(),
        vueJsx(),
        vueDevTools(),
    ],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url))
        },
    },
    server: {
        port: config.frontendPort,
        host: true, // 允许通过IP访问
        proxy: {
            '/api': {
                target: `http://${config.serverHost}:8080`,
                changeOrigin: true,
                rewrite: function (path) { return path; }
            }
        }
    }
});
