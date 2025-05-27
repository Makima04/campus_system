/**
 * 安全工具函数
 * 提供防XSS、防SQL注入等安全功能
 */

/**
 * 防XSS攻击 - 转义HTML特殊字符
 * @param str 需要转义的字符串
 * @returns 转义后的安全字符串
 */
export const escapeHtml = (str: string): string => {
  if (!str) return '';
  
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
};

/**
 * 防XSS攻击 - 清理HTML内容，移除危险标签和属性
 * @param html HTML字符串
 * @returns 清理后的安全HTML
 */
export const sanitizeHtml = (html: string): string => {
  if (!html) return '';
  
  // 移除脚本标签
  let safeHtml = html.replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '');
  
  // 移除事件处理属性
  safeHtml = safeHtml.replace(/ on\w+="[^"]*"/g, '');
  safeHtml = safeHtml.replace(/ on\w+='[^']*'/g, '');
  
  // 移除javascript:协议
  safeHtml = safeHtml.replace(/javascript:/gi, '');
  
  // 移除data:协议
  safeHtml = safeHtml.replace(/data:/gi, '');
  
  return safeHtml;
};

/**
 * 防SQL注入 - 转义SQL特殊字符
 * @param str 需要转义的字符串
 * @returns 转义后的安全字符串
 */
export const escapeSql = (str: string): string => {
  if (!str) return '';
  
  return str
    .replace(/'/g, "''")
    .replace(/--/g, '')
    .replace(/;/g, '');
};

/**
 * 防SQL注入 - 参数化查询辅助函数
 * @param query SQL查询模板
 * @param params 参数对象
 * @returns 安全的参数化查询
 */
export const createParameterizedQuery = (query: string, params: Record<string, any>): string => {
  let safeQuery = query;
  
  // 替换所有参数占位符
  Object.keys(params).forEach(key => {
    const value = params[key];
    const placeholder = new RegExp(`:${key}`, 'g');
    
    if (typeof value === 'string') {
      // 字符串类型，添加引号并转义
      safeQuery = safeQuery.replace(placeholder, `'${escapeSql(value)}'`);
    } else if (value === null) {
      // null值
      safeQuery = safeQuery.replace(placeholder, 'NULL');
    } else if (typeof value === 'number') {
      // 数字类型，直接使用
      safeQuery = safeQuery.replace(placeholder, value.toString());
    } else if (typeof value === 'boolean') {
      // 布尔类型
      safeQuery = safeQuery.replace(placeholder, value ? '1' : '0');
    } else {
      // 其他类型，转为字符串并转义
      safeQuery = safeQuery.replace(placeholder, `'${escapeSql(String(value))}'`);
    }
  });
  
  return safeQuery;
};

/**
 * 生成安全的随机令牌
 * @param length 令牌长度
 * @returns 随机令牌
 */
export const generateSecureToken = (length: number = 32): string => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  const randomValues = new Uint8Array(length);
  window.crypto.getRandomValues(randomValues);
  
  let result = '';
  for (let i = 0; i < length; i++) {
    result += chars.charAt(randomValues[i] % chars.length);
  }
  
  return result;
};

/**
 * 安全的字符串比较（防止时序攻击）
 * @param a 第一个字符串
 * @param b 第二个字符串
 * @returns 是否相等
 */
export const secureCompare = (a: string, b: string): boolean => {
  if (a.length !== b.length) {
    return false;
  }
  
  let result = 0;
  for (let i = 0; i < a.length; i++) {
    result |= a.charCodeAt(i) ^ b.charCodeAt(i);
  }
  
  return result === 0;
};

/**
 * 安全的密码哈希函数（实际项目中应使用bcrypt等库）
 * @param password 原始密码
 * @returns 哈希后的密码
 */
export const hashPassword = async (password: string): Promise<string> => {
  // 注意：这只是示例，实际项目中应使用专业的密码哈希库
  const encoder = new TextEncoder();
  const data = encoder.encode(password);
  const hashBuffer = await window.crypto.subtle.digest('SHA-256', data);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
  return hashHex;
};

/**
 * 验证密码强度
 * @param password 密码
 * @returns 密码强度评估结果
 */
export const validatePasswordStrength = (password: string): {
  isValid: boolean;
  score: number; // 0-4，0最弱，4最强
  message: string;
} => {
  let score = 0;
  let message = '';
  
  // 长度检查
  if (password.length < 8) {
    return { isValid: false, score: 0, message: '密码长度至少为8个字符' };
  }
  
  if (password.length >= 12) score++;
  
  // 包含数字
  if (/\d/.test(password)) score++;
  
  // 包含小写字母
  if (/[a-z]/.test(password)) score++;
  
  // 包含大写字母
  if (/[A-Z]/.test(password)) score++;
  
  // 包含特殊字符
  if (/[^A-Za-z0-9]/.test(password)) score++;
  
  // 评估结果
  if (score < 2) {
    message = '密码强度弱，建议使用更复杂的密码';
  } else if (score < 3) {
    message = '密码强度中等';
  } else if (score < 4) {
    message = '密码强度良好';
  } else {
    message = '密码强度强';
  }
  
  return {
    isValid: score >= 2,
    score: Math.min(score, 4),
    message
  };
};

/**
 * 防抖函数封装
 * @param fn 需要防抖的函数
 * @param delay 延迟时间(毫秒)
 * @returns 防抖处理后的函数
 */
export const debounce = <T extends (...args: any[]) => any>(
  fn: T,
  delay: number
): ((...args: Parameters<T>) => void) => {
  let timer: number | null = null;
  
  return (...args: Parameters<T>) => {
    if (timer) {
      clearTimeout(timer);
    }
    
    timer = window.setTimeout(() => {
      fn(...args);
      timer = null;
    }, delay);
  };
}; 