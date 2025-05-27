/**
 * 管理员模块API
 */
import { get, post, put, del } from '../../../utils/http';
import { getApiBaseUrl } from '../../../config';

// API端点
const API_ENDPOINTS = {
  // 通知管理API
  getNotifications: `${getApiBaseUrl()}/notification`,
  getNotificationById: (id: number) => `${getApiBaseUrl()}/notification/${id}`,
  createNotification: `${getApiBaseUrl()}/notification`,
  updateNotification: (id: number) => `${getApiBaseUrl()}/notification/${id}`,
  deleteNotification: (id: number) => `${getApiBaseUrl()}/notification/${id}`,
  publishNotification: (id: number) => `${getApiBaseUrl()}/notification/${id}/publish`,
  
  // 管理员仪表板API
  getDashboardStats: `${getApiBaseUrl()}/admin/dashboard`,
  
  // 系统设置API
  getSystemSettings: `${getApiBaseUrl()}/admin/settings`,
  updateSystemSettings: `${getApiBaseUrl()}/admin/settings`,
  
  // 用户管理API
  getUsers: `${getApiBaseUrl()}/user`,
  getUserById: (id: number) => `${getApiBaseUrl()}/user/${id}`,
  createUser: `${getApiBaseUrl()}/user`,
  updateUser: (id: number) => `${getApiBaseUrl()}/user/${id}`,
  deleteUser: (id: number) => `${getApiBaseUrl()}/user/${id}`
};

/**
 * 获取通知列表
 * @param params 查询参数
 */
export const getNotifications = (params?: any) => {
  return get(API_ENDPOINTS.getNotifications, { params });
};

/**
 * 获取通知详情
 * @param id 通知ID
 */
export const getNotificationById = (id: number) => {
  return get(API_ENDPOINTS.getNotificationById(id));
};

/**
 * 创建通知
 * @param notificationData 通知数据
 */
export const createNotification = (notificationData: any) => {
  return post(API_ENDPOINTS.createNotification, notificationData);
};

/**
 * 更新通知
 * @param id 通知ID
 * @param notificationData 通知数据
 */
export const updateNotification = (id: number, notificationData: any) => {
  return put(API_ENDPOINTS.updateNotification(id), notificationData);
};

/**
 * 删除通知
 * @param id 通知ID
 */
export const deleteNotification = (id: number) => {
  return del(API_ENDPOINTS.deleteNotification(id));
};

/**
 * 发布通知
 * @param id 通知ID
 */
export const publishNotification = (id: number) => {
  return post(API_ENDPOINTS.publishNotification(id));
};

/**
 * 获取仪表板统计数据
 */
export const getDashboardStats = () => {
  return get(API_ENDPOINTS.getDashboardStats);
};

/**
 * 获取系统设置
 */
export const getSystemSettings = () => {
  return get(API_ENDPOINTS.getSystemSettings);
};

/**
 * 更新系统设置
 * @param settingsData 设置数据
 */
export const updateSystemSettings = (settingsData: any) => {
  return put(API_ENDPOINTS.updateSystemSettings, settingsData);
};

// 导出API接口对象，用于支持默认导入
const api = {
  get: get,
  post: post,
  put: put,
  delete: del,
  getNotifications,
  getNotificationById,
  createNotification,
  updateNotification,
  deleteNotification,
  publishNotification,
  getDashboardStats,
  getSystemSettings,
  updateSystemSettings
};

export default api; 