/**
 * 前端校验工具箱
 * 提供常用的表单验证方法
 */

// 验证方法接口，返回布尔值或错误消息
interface ValidationResult {
  valid: boolean;
  message?: string;
}

// 必填验证
export const required = (value: any, message = '此字段为必填项'): ValidationResult => {
  // 检查各种空值的情况
  const isValid = !(
    value === undefined || 
    value === null || 
    value === '' || 
    (Array.isArray(value) && value.length === 0) ||
    (typeof value === 'object' && Object.keys(value).length === 0)
  );
  
  return {
    valid: isValid,
    message: isValid ? undefined : message
  };
};

// 字符串长度验证
export const length = (
  value: string, 
  min: number, 
  max: number, 
  message?: string
): ValidationResult => {
  if (value === null || value === undefined || value === '') {
    return { valid: true }; // 空值不由此验证器处理
  }
  
  const strValue = String(value);
  const isValid = strValue.length >= min && strValue.length <= max;
  
  return {
    valid: isValid,
    message: isValid ? undefined : message || `长度应在${min}到${max}个字符之间`
  };
};

// 数字范围验证
export const numberRange = (
  value: number, 
  min: number, 
  max: number, 
  message?: string
): ValidationResult => {
  if (value === null || value === undefined) {
    return { valid: true }; // 空值不由此验证器处理
  }
  
  const numValue = Number(value);
  
  if (isNaN(numValue)) {
    return {
      valid: false,
      message: '请输入有效的数字'
    };
  }
  
  const isValid = numValue >= min && numValue <= max;
  
  return {
    valid: isValid,
    message: isValid ? undefined : message || `数值应在${min}到${max}之间`
  };
};

// 电子邮件验证
export const email = (value: string, message = '请输入有效的电子邮件地址'): ValidationResult => {
  if (value === null || value === undefined || value === '') {
    return { valid: true }; // 空值不由此验证器处理
  }
  
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
  const isValid = emailRegex.test(value);
  
  return {
    valid: isValid,
    message: isValid ? undefined : message
  };
};

// 手机号码验证（中国大陆）
export const phone = (value: string, message = '请输入有效的手机号码'): ValidationResult => {
  if (value === null || value === undefined || value === '') {
    return { valid: true }; // 空值不由此验证器处理
  }
  
  const phoneRegex = /^1[3-9]\d{9}$/;
  const isValid = phoneRegex.test(value);
  
  return {
    valid: isValid,
    message: isValid ? undefined : message
  };
};

// 身份证号验证
export const idCard = (value: string, message = '请输入有效的身份证号'): ValidationResult => {
  if (value === null || value === undefined || value === '') {
    return { valid: true }; // 空值不由此验证器处理
  }
  
  const idCardRegex = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
  const isValid = idCardRegex.test(value);
  
  return {
    valid: isValid,
    message: isValid ? undefined : message
  };
};

// 自定义正则验证
export const pattern = (
  value: string, 
  regex: RegExp, 
  message = '格式不正确'
): ValidationResult => {
  if (value === null || value === undefined || value === '') {
    return { valid: true }; // 空值不由此验证器处理
  }
  
  const isValid = regex.test(value);
  
  return {
    valid: isValid,
    message: isValid ? undefined : message
  };
};

// 课程编号验证（根据系统规则定制）
export const courseCode = (
  value: string, 
  message = '课程编号应为6-20位字母、数字或短横线的组合'
): ValidationResult => {
  if (value === null || value === undefined || value === '') {
    return { valid: true }; // 空值不由此验证器处理
  }
  
  const codeRegex = /^[A-Za-z0-9-]{6,20}$/;
  const isValid = codeRegex.test(value);
  
  return {
    valid: isValid,
    message: isValid ? undefined : message
  };
};

// 学分验证
export const credits = (
  value: number, 
  message = '学分必须是0.5的倍数，且在0.5-10之间'
): ValidationResult => {
  if (value === null || value === undefined) {
    return { valid: true }; // 空值不由此验证器处理
  }
  
  const numValue = Number(value);
  
  if (isNaN(numValue)) {
    return {
      valid: false,
      message: '请输入有效的数字'
    };
  }
  
  const isValid = numValue >= 0.5 && numValue <= 10 && (numValue * 2) % 1 === 0;
  
  return {
    valid: isValid,
    message: isValid ? undefined : message
  };
};

// Element Plus 表单验证规则生成器
export const createRules = (validations: Record<string, Function[]>) => {
  const rules: Record<string, any[]> = {};
  
  for (const field in validations) {
    rules[field] = validations[field].map(validation => ({
      validator: (_rule: any, value: any, callback: Function) => {
        const result = validation(value);
        if (result.valid) {
          callback();
        } else {
          callback(new Error(result.message));
        }
      },
      trigger: ['blur', 'change']
    }));
  }
  
  return rules;
};

// 示例用法
/*
import { createRules, required, length, numberRange, credits } from '@/tool/validation';

const formRules = createRules({
  courseCode: [required, value => courseCode(value)],
  courseName: [required, value => length(value, 2, 50)],
  credits: [required, value => credits(value)],
  capacity: [required, value => numberRange(value, 1, 500)]
});
*/ 