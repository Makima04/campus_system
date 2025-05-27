/**
 * 认证模块API
 */
import { post } from '../../../utils/http';
import { getApiBaseUrl } from '../../../config';

// API端点
const API_ENDPOINTS = {
  login: `${getApiBaseUrl()}/auth/login`,
  register: `${getApiBaseUrl()}/auth/register`,
  logout: `${getApiBaseUrl()}/auth/logout`,
  refreshToken: `${getApiBaseUrl()}/auth/refresh-token`
};

/**
 * 用户登录
 * @param username 用户名
 * @param password 密码
 */
export const login = (username: string, password: string) => {
  return post(API_ENDPOINTS.login, { username, password });
};

/**
 * 用户注册
 * @param userData 用户数据
 */
export const register = (userData: any) => {
  return post(API_ENDPOINTS.register, userData);
};

/**
 * 用户登出
 */
export const logout = () => {
  return post(API_ENDPOINTS.logout);
};

/**
 * 刷新令牌
 * @param refreshToken 刷新令牌
 */
export const refreshToken = (refreshToken: string) => {
  return post(API_ENDPOINTS.refreshToken, { refreshToken });
}; 