// HTTP工具类
// 提供统一的请求方法和错误处理

import axios from 'axios';
// 使用自定义类型别名解决类型导入问题
type AxiosRequestConfig = any;
type AxiosResponse = any;
import { getApiBaseUrl } from '../config';
import { ElMessage } from 'element-plus';

// 创建axios实例
const http = axios.create({
  baseURL: "",  // 使用相对路径，让开发服务器的代理配置生效
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
http.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    // 从localStorage获取token和role
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');
    
    console.log('发送请求:', {
      url: config.url,
      method: config.method,
      headers: config.headers,
      token: token ? '存在' : '不存在',
      role: role
    });
    
    // 如果有token则添加到请求头
    if (token && config.headers) {
      // 确保token格式正确
      if (!token.startsWith('Bearer ')) {
        config.headers['Authorization'] = `Bearer ${token}`;
      } else {
        config.headers['Authorization'] = token;
      }
      
      // 添加角色信息到请求头
      if (role) {
        // 确保角色格式正确 - 保持大写格式
        const normalizedRole = role.toUpperCase();
        config.headers['X-User-Role'] = normalizedRole;
        config.headers['ROLE'] = normalizedRole;
      }
      
      // 尝试解析JWT令牌中的角色信息
      try {
        const parts = token.replace('Bearer ', '').split('.');
        if (parts.length === 3) {
          const payload = JSON.parse(atob(parts[1]));
          console.log('JWT令牌内容:', {
            subject: payload.sub,
            roles: payload.roles || '无角色信息',
            exp: payload.exp ? new Date(payload.exp * 1000).toLocaleString() : '无过期时间'
          });
          
          // 从JWT中添加角色信息
          if (payload.roles) {
            const rolesStr = Array.isArray(payload.roles) 
              ? payload.roles.join(',') 
              : String(payload.roles);
              
            // 确保角色格式正确
            const normalizedRoles = rolesStr.split(',')
              .map((role: string) => role.trim())
              .map((role: string) => role.startsWith('ROLE_') ? role : `ROLE_${role}`)
              .join(',');
              
            config.headers['ROLE'] = normalizedRoles;
            console.log('标准化后的角色权限:', normalizedRoles);
            
            // 提取主要角色给前端使用
            const mainRole = extractMainRole(rolesStr);
            if (mainRole && mainRole !== role) {
              console.log(`更新本地存储的角色：${role} -> ${mainRole}`);
              localStorage.setItem('role', mainRole);
            }
          }
        }
      } catch (e) {
        console.warn('解析JWT令牌失败:', e);
      }
      
      console.log('设置认证头:', {
        Authorization: config.headers['Authorization'],
        'X-User-Role': config.headers['X-User-Role'],
        'ROLE': config.headers['ROLE']
      });
    } else {
      console.warn('未找到token，请求将不包含认证信息');
    }
    
    return config;
  },
  (error) => {
    console.error('请求拦截器错误:', error);
    return Promise.reject(error);
  }
);

// 提取主要角色的辅助函数
function extractMainRole(rolesStr: string): string | null {
  if (!rolesStr) return null;
  
  const roles = rolesStr.split(',').map(r => r.trim());
  
  // 按优先级查找角色
  const priorityRoles = ['ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT'];
  
  for (const priorityRole of priorityRoles) {
    const found = roles.find(r => r.toUpperCase() === priorityRole);
    if (found) {
      // 保持原始格式，不进行转换
      return found;
    }
  }
  
  // 如果没有找到优先角色，返回第一个角色
  if (roles.length > 0) {
    return roles[0];
  }
  
  return null;
}

// 响应拦截器
http.interceptors.response.use(
  (response: AxiosResponse) => {
    // 返回完整响应
    return response;
  },
  async (error) => {
    console.log('响应错误:', error.response); 
    
    if (error.response) {
      // 记录错误详细信息
      console.log('响应错误:', {
        status: error.response.status,
        url: error.config.url,
        method: error.config.method,
        headers: error.response.headers,
        data: error.response.data
      });

      // 处理401未授权错误 - 清除token并重定向到登录页
      if (error.response.status === 401) {
        console.log('401未授权');
        localStorage.removeItem('token');
        localStorage.removeItem('role');
        window.location.href = '/login';
        return Promise.reject('会话已过期，请重新登录');
      }
      
      // 处理403禁止访问错误 - 可能是权限不足
      if (error.response.status === 403) {
        const token = localStorage.getItem('token');
        const role = localStorage.getItem('role');
        
        console.log('权限验证失败:', {
          role: role,
          token: token ? '存在' : '不存在',
          url: error.config.url,
          method: error.config.method
        });
        
        // 检查JWT是否包含角色信息
        if (token) {
          try {
            const parts = token.replace('Bearer ', '').split('.');
            if (parts.length === 3) {
              const payload = JSON.parse(atob(parts[1]));
              
              // 如果JWT中没有角色信息，提示用户重新登录
              if (!payload.roles || payload.roles === "无角色信息") {
                console.log('JWT中没有角色信息，需要重新登录');
                // 显示重新登录提示
                const userConfirmed = confirm('您的登录信息不完整，请重新登录以获取正确的权限');
                if (userConfirmed) {
                  localStorage.removeItem('token');
                  localStorage.removeItem('role');
                  window.location.href = '/login';
                  return Promise.reject('请重新登录以获取正确的权限');
                }
              }
            }
          } catch (e) {
            console.warn('解析JWT令牌失败:', e);
          }
        }
        
        // 通用403处理
        return Promise.reject('您没有权限访问此资源');
      }
    }
    
    return Promise.reject(error);
  }
);

// 封装GET请求
export const get = async <T = any>(url: string, params?: any, config?: AxiosRequestConfig): Promise<any> => {
  const response = await http.get<T>(url, { params, ...config });
  return response;
};

// 封装POST请求
export const post = async <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<any> => {
  const response = await http.post<T>(url, data, config);
  return response;
};

// 封装PUT请求
export const put = async <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<any> => {
  const response = await http.put<T>(url, data, config);
  return response;
};

// 封装DELETE请求
export const del = async <T = any>(url: string, config?: AxiosRequestConfig): Promise<any> => {
  const response = await http.delete<T>(url, config);
  return response;
};

// 导出http实例
export default http; 