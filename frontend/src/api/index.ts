/**
 * API模块入口
 * 提供全局通用的API调用方法
 */
import axios from 'axios'

// 基础URL，根据环境配置
const getBaseUrl = () => {
  const env = process.env.NODE_ENV
  return env === 'production' 
    ? '/api' // 生产环境使用相对路径，由部署环境决定实际访问地址
    : '/api'
}

// 创建API实例
const apiInstance = axios.create({
  baseURL: getBaseUrl(),
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
apiInstance.interceptors.request.use(
  config => {
    // 从本地存储获取token
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
apiInstance.interceptors.response.use(
  response => {
    // 返回原始响应对象，不修改结构
    return response
  },
  error => {
    return Promise.reject(error)
  }
)

// 类型别名用于API响应
export interface ApiResponse<T = unknown> {
  data: T;
  code?: number;
  message?: string;
}

// 定义通用的请求体类型
export interface LoginRequest {
  username: string;
  password: string;
}

export interface UserProfileData {
  name?: string;
  email?: string;
  phone?: string;
  avatar?: string;
}

export interface SearchParams {
  keyword?: string;
  page?: number;
  size?: number;
  [key: string]: unknown;
}

// 通用API对象
export const api = {
  // 用户相关
  user: {
    login: (data: LoginRequest) => apiInstance.post<ApiResponse<{token: string}>>('/auth/login', data),
    getInfo: () => apiInstance.get<ApiResponse<UserProfileData>>('/user/info'),
    updateProfile: (data: UserProfileData) => apiInstance.put<ApiResponse<boolean>>('/user/profile', data)
  },
  
  // 通知相关
  notification: {
    getList: (params?: SearchParams) => apiInstance.get<ApiResponse<unknown[]>>('/notification/list', { params }),
    getById: (id: number) => apiInstance.get<ApiResponse<unknown>>(`/notification/${id}`),
    create: (data: Record<string, unknown>) => apiInstance.post<ApiResponse<unknown>>('/notification', data),
    update: (id: number, data: Record<string, unknown>) => apiInstance.put<ApiResponse<unknown>>(`/notification/${id}`, data),
    delete: (id: number) => apiInstance.delete<ApiResponse<boolean>>(`/notification/${id}`),
    publish: (id: number) => apiInstance.post<ApiResponse<boolean>>(`/notification/${id}/publish`),
    withdraw: (id: number) => apiInstance.post<ApiResponse<boolean>>(`/notification/${id}/withdraw`)
  },
  
  // 课程相关
  course: {
    getList: (params?: SearchParams) => apiInstance.get<ApiResponse<unknown[]>>('/course/list', { params }),
    getById: (id: number) => apiInstance.get<ApiResponse<unknown>>(`/course/${id}`),
    create: (data: Record<string, unknown>) => apiInstance.post<ApiResponse<unknown>>('/course', data),
    update: (id: number, data: Record<string, unknown>) => apiInstance.put<ApiResponse<unknown>>(`/course/${id}`, data),
    delete: (id: number) => apiInstance.delete<ApiResponse<boolean>>(`/course/${id}`)
  }
}

// 默认导出
export default apiInstance 