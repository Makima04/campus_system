// 认证工具类
// 处理用户登录、注销、权限等认证相关操作

import { post } from './http';
import { authApi } from '../config/api-config';

// 存储令牌的键名
const TOKEN_KEY = 'token';
const USER_INFO_KEY = 'user_info';

/**
 * 登录并保存令牌
 * @param username 用户名
 * @param password 密码
 */
export const login = async (username: string, password: string): Promise<boolean> => {
  try {
    const response = await post(authApi.login, { username, password });
    if (response.token) {
      // 保存令牌和用户信息
      localStorage.setItem(TOKEN_KEY, response.token);
      localStorage.setItem(USER_INFO_KEY, JSON.stringify(response.user));
      return true;
    }
    return false;
  } catch (error) {
    console.error('登录失败:', error);
    return false;
  }
};

/**
 * 获取当前用户信息
 */
export const getCurrentUser = (): any | null => {
  const userInfo = localStorage.getItem(USER_INFO_KEY);
  return userInfo ? JSON.parse(userInfo) : null;
};

/**
 * 获取当前用户的角色
 */
export const getUserRole = (): string | null => {
  const user = getCurrentUser();
  return user ? user.role : null;
};

/**
 * 检查用户是否已登录
 */
export const isAuthenticated = (): boolean => {
  return !!localStorage.getItem(TOKEN_KEY);
};

/**
 * 检查用户是否具有指定角色
 * @param role 角色名称
 */
export const hasRole = (role: string): boolean => {
  const userRole = getUserRole();
  return userRole === role;
};

/**
 * 注销并清除令牌
 */
export const logout = (): void => {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_INFO_KEY);
  window.location.href = '/login';
}; 