/**
 * 用户模块类型定义
 */

/**
 * 用户角色枚举
 */
export enum UserRole {
  ROLE_ADMIN = 'ROLE_ADMIN',
  ROLE_STUDENT = 'ROLE_STUDENT',
  ROLE_TEACHER = 'ROLE_TEACHER',
  ROLE_LIBRARY_ADMIN = 'ROLE_LIBRARY_ADMIN',
  ROLE_NOTICE_ADMIN = 'ROLE_NOTICE_ADMIN'
}

/**
 * 用户基本信息接口
 */
export interface User {
  id?: number | string;
  username: string;
  password?: string;
  name?: string;
  email?: string;
  phone?: string;
  role?: UserRole | string;
  avatar?: string;
  studentId?: string;
  employeeId?: string;
  address?: string;
  bio?: string;
  createTime?: string;
  updateTime?: string;
  status?: UserStatus;
  lastLoginTime?: string;
  lastLoginIp?: string;
  [key: string]: any; // 允许添加其他属性
}

/**
 * 用户状态枚举
 */
export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  LOCKED = 'LOCKED',
  DELETED = 'DELETED'
}

/**
 * 登录请求参数
 */
export interface LoginRequest {
  username: string;
  password: string;
  rememberMe?: boolean;
  captcha?: string;
}

/**
 * 登录响应数据
 */
export interface LoginResponse {
  token: string;
  refreshToken?: string;
  tokenExpires?: number;
  user: User;
}

/**
 * 注册请求参数
 */
export interface RegisterRequest {
  username: string;
  password: string;
  confirmPassword?: string;
  email: string;
  role?: UserRole | string;
  captcha?: string;
}

/**
 * 修改密码请求参数
 */
export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
  confirmPassword?: string;
}

/**
 * 用户查询参数
 */
export interface UserQueryParams {
  keyword?: string;
  role?: UserRole | string;
  status?: UserStatus;
  pageNum?: number;
  pageSize?: number;
  startTime?: string;
  endTime?: string;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

/**
 * 分页响应数据
 */
export interface PaginationResponse<T> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
  totalPages: number;
}

/**
 * 用户分页响应数据
 */
export type UserPaginationResponse = PaginationResponse<User>; 