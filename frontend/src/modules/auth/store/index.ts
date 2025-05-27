/**
 * 认证模块状态管理
 */
import { defineStore } from 'pinia';
import * as authApi from '../api';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    refreshToken: localStorage.getItem('refreshToken') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    role: localStorage.getItem('role') || '',
    loginLoading: false,
    loginError: null
  }),
  
  getters: {
    isAuthenticated: (state) => !!state.token,
    currentUser: (state) => state.user,
    userRole: (state) => state.role
  },
  
  actions: {
    async login(username: string, password: string) {
      this.loginLoading = true;
      this.loginError = null;
      
      try {
        const response = await authApi.login(username, password);
        this.setAuthData(response.data);
        return response;
      } catch (error: any) {
        this.loginError = error.response?.data?.message || '登录失败';
        throw error;
      } finally {
        this.loginLoading = false;
      }
    },
    
    async logout() {
      try {
        await authApi.logout();
      } catch (error) {
        console.error('登出失败', error);
      } finally {
        this.clearAuthData();
      }
    },
    
    setAuthData(data: any) {
      this.token = data.token;
      this.refreshToken = data.refreshToken;
      this.user = data.user;
      this.role = data.user?.role;
      
      localStorage.setItem('token', data.token);
      localStorage.setItem('refreshToken', data.refreshToken);
      localStorage.setItem('user', JSON.stringify(data.user));
      localStorage.setItem('role', data.user?.role || '');
    },
    
    clearAuthData() {
      this.token = '';
      this.refreshToken = '';
      this.user = null;
      this.role = '';
      
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      localStorage.removeItem('role');
    }
  }
}); 