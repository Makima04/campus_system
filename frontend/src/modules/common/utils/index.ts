/**
 * 公共工具函数导出文件
 */

// 日期格式化函数
export const formatDate = (date: Date, format: string = 'YYYY-MM-DD'): string => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  
  let result = format;
  result = result.replace('YYYY', String(year));
  result = result.replace('MM', month);
  result = result.replace('DD', day);
  
  return result;
};

// 防抖函数
export const debounce = <T extends (...args: any[]) => any>(fn: T, delay: number = 300) => {
  let timer: number | null = null;
  
  return function(this: any, ...args: Parameters<T>) {
    if (timer) clearTimeout(timer);
    
    timer = window.setTimeout(() => {
      fn.apply(this, args);
    }, delay);
  };
};

// 节流函数
export const throttle = <T extends (...args: any[]) => any>(fn: T, delay: number = 300) => {
  let lastTime = 0;
  
  return function(this: any, ...args: Parameters<T>) {
    const now = Date.now();
    
    if (now - lastTime >= delay) {
      fn.apply(this, args);
      lastTime = now;
    }
  };
}; 