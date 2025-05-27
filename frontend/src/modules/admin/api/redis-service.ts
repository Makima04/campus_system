/**
 * Redis缓存管理API服务
 */
import { get, post, del } from '@/utils/http';
import { getApiBaseUrl } from '@/config';

// API端点
const API_ENDPOINTS = {
  getAllKeys: `${getApiBaseUrl()}/redis/keys`,
  getValue: `${getApiBaseUrl()}/redis/get`,
  setValue: `${getApiBaseUrl()}/redis/set`,
  deleteKey: `${getApiBaseUrl()}/redis/delete`,
  getTTL: `${getApiBaseUrl()}/redis/ttl`,
  flushAll: `${getApiBaseUrl()}/redis/flush`
};

/**
 * 获取所有Redis键
 * @returns 所有Redis键的信息
 */
export const getAllKeys = () => {
  return get(API_ENDPOINTS.getAllKeys);
};

/**
 * 获取指定键的值
 * @param key 键名
 * @returns 键对应的值
 */
export const getValue = (key: string) => {
  return get(API_ENDPOINTS.getValue, { params: { key } });
};

/**
 * 设置键值对
 * @param key 键名
 * @param value 键值
 * @param ttl 过期时间（秒），0表示永不过期
 * @returns 设置结果
 */
export const setValue = (key: string, value: string, ttl: number = 0) => {
  const params = new URLSearchParams();
  params.append('key', key);
  params.append('value', value);
  params.append('ttl', ttl.toString());
  
  return post(API_ENDPOINTS.setValue, params, {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  });
};

/**
 * 删除指定键
 * @param key 键名
 * @returns 删除结果
 */
export const deleteKey = (key: string) => {
  return del(`${API_ENDPOINTS.deleteKey}?key=${encodeURIComponent(key)}`);
};

/**
 * 获取键的剩余过期时间
 * @param key 键名
 * @returns 剩余过期时间（秒）
 */
export const getTTL = (key: string) => {
  return get(API_ENDPOINTS.getTTL, { params: { key } });
};

/**
 * 清空所有缓存
 * @returns 清空结果
 */
export const flushAll = () => {
  return del(API_ENDPOINTS.flushAll);
};

// 导出API接口对象，用于支持默认导入
const redisService = {
  getAllKeys,
  getValue,
  setValue,
  deleteKey,
  getTTL,
  flushAll
};

export default redisService; 