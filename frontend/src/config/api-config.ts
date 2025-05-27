// API配置文件
// 定义所有API端点路径

import { getApiBaseUrl } from './index';

/**
 * 认证相关API
 */
export const authApi = {
  login: `${getApiBaseUrl()}/auth/login`,
  register: `${getApiBaseUrl()}/auth/register`,
  logout: `${getApiBaseUrl()}/auth/logout`,
  refreshToken: `${getApiBaseUrl()}/auth/refresh-token`,
};

/**
 * 用户相关API
 */
export const userApi = {
  profile: `${getApiBaseUrl()}/users/profile`,
  update: `${getApiBaseUrl()}/users/update`,
  changePassword: `${getApiBaseUrl()}/users/change-password`,
  list: `${getApiBaseUrl()}/users`,
  getById: (id: number) => `${getApiBaseUrl()}/users/${id}`,
};

/**
 * 图书相关API
 */
export const bookApi = {
  list: `${getApiBaseUrl()}/books`,
  getById: (id: number) => `${getApiBaseUrl()}/books/${id}`,
  create: `${getApiBaseUrl()}/books`,
  update: (id: number) => `${getApiBaseUrl()}/books/${id}`,
  delete: (id: number) => `${getApiBaseUrl()}/books/${id}`,
  borrow: (id: number) => `${getApiBaseUrl()}/books/${id}/borrow`,
  return: (id: number) => `${getApiBaseUrl()}/books/${id}/return`,
};

/**
 * 课程相关API
 */
export const courseApi = {
  list: `${getApiBaseUrl()}/courses`,
  getById: (id: number) => `${getApiBaseUrl()}/courses/${id}`,
  create: `${getApiBaseUrl()}/courses`,
  update: (id: number) => `${getApiBaseUrl()}/courses/${id}`,
  delete: (id: number) => `${getApiBaseUrl()}/courses/${id}`,
  enroll: (id: number) => `${getApiBaseUrl()}/courses/${id}/enroll`,
  withdraw: (id: number) => `${getApiBaseUrl()}/courses/${id}/withdraw`,
};

/**
 * 文件上传相关API
 */
export const uploadApi = {
  uploadFile: `${getApiBaseUrl()}/upload/file`,
  uploadImage: `${getApiBaseUrl()}/upload/image`,
  uploadAvatar: `${getApiBaseUrl()}/upload/avatar`,
};

/**
 * OSS相关API
 */
export const ossApi = {
  getPolicy: `${getApiBaseUrl()}/oss/policy`,
  getCallback: `${getApiBaseUrl()}/oss/callback`,
};

/**
 * 配置相关API
 */
export const configApi = {
  public: `${getApiBaseUrl()}/config/public`,
  server: `${getApiBaseUrl()}/config/server`,
}; 