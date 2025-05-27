/**
 * 用户模块API
 */
import { get, post, put, del } from '../../../utils/http';
import { getApiBaseUrl } from '../../../config';

// API端点
const API_ENDPOINTS = {
  getProfile: `${getApiBaseUrl()}/user/profile`,
  updateProfile: `${getApiBaseUrl()}/user/profile`,
  getUsers: `${getApiBaseUrl()}/user`,
  getUserById: (id: number) => `${getApiBaseUrl()}/user/${id}`,
  createUser: `${getApiBaseUrl()}/user`,
  updateUser: (id: number) => `${getApiBaseUrl()}/user/${id}`,
  deleteUser: (id: number) => `${getApiBaseUrl()}/user/${id}`,
  changePassword: `${getApiBaseUrl()}/user/password`
};

/**
 * 获取当前用户信息
 */
export const getProfile = () => {
  return get(API_ENDPOINTS.getProfile);
};

/**
 * 更新当前用户信息
 * @param profileData 用户信息数据
 */
export const updateProfile = (profileData: any) => {
  return put(API_ENDPOINTS.updateProfile, profileData);
};

/**
 * 获取所有用户列表
 * @param params 查询参数
 */
export const getUsers = (params?: any) => {
  return get(API_ENDPOINTS.getUsers, { params });
};

/**
 * 根据ID获取用户
 * @param id 用户ID
 */
export const getUserById = (id: number) => {
  return get(API_ENDPOINTS.getUserById(id));
};

/**
 * 创建新用户
 * @param userData 用户数据
 */
export const createUser = (userData: any) => {
  return post(API_ENDPOINTS.createUser, userData);
};

/**
 * 更新用户信息
 * @param id 用户ID
 * @param userData 用户数据
 */
export const updateUser = (id: number, userData: any) => {
  return put(API_ENDPOINTS.updateUser(id), userData);
};

/**
 * 删除用户
 * @param id 用户ID
 */
export const deleteUser = (id: number) => {
  return del(API_ENDPOINTS.deleteUser(id));
};

/**
 * 修改密码
 * @param passwordData 密码数据
 */
export const changePassword = (passwordData: any) => {
  return post(API_ENDPOINTS.changePassword, passwordData);
}; 