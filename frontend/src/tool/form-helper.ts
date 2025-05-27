/**
 * 表单助手工具
 * 提供表单处理的实用方法
 */

import { ref } from '@vue/runtime-dom';
import type { FormInstance } from 'element-plus';
import { ElMessage } from 'element-plus';

/**
 * 重置表单并清除验证
 * @param formRef 表单引用
 * @param formData 表单数据
 * @param initialValues 表单初始值（可选）
 */
export const resetForm = (
  formRef: FormInstance | null, 
  formData: Record<string, any>, 
  initialValues?: Record<string, any>
): void => {
  if (!formRef) return;
  
  // 重置表单验证
  formRef.resetFields();
  
  // 如果提供了初始值，使用初始值重置表单
  if (initialValues) {
    Object.keys(formData).forEach(key => {
      if (key in initialValues) {
        formData[key] = initialValues[key];
      } else {
        // 清除其他字段值
        if (Array.isArray(formData[key])) {
          formData[key] = [];
        } else if (typeof formData[key] === 'object' && formData[key] !== null) {
          formData[key] = {};
        } else {
          formData[key] = null;
        }
      }
    });
  }
};

/**
 * 创建表单提交处理
 * @param formRef 表单引用
 * @param submitFn 提交函数
 * @param successMsg 成功消息
 * @returns 表单提交处理函数和加载状态
 */
export const useFormSubmit = (
  formRef: FormInstance | null, 
  submitFn: () => Promise<any>, 
  successMsg: string = '操作成功'
) => {
  const loading = ref(false);
  
  const handleSubmit = async () => {
    if (!formRef) return;
    
    try {
      // 验证表单
      await formRef.validate();
      
      // 设置加载状态
      loading.value = true;
      
      // 执行提交
      await submitFn();
      
      // 显示成功消息
      ElMessage.success(successMsg);
      
      return true;
    } catch (error) {
      // 处理错误
      if (error !== 'cancel' && error !== false) {
        console.error('表单提交失败:', error);
        ElMessage.error('表单验证失败或提交出错');
      }
      return false;
    } finally {
      loading.value = false;
    }
  };
  
  return {
    loading,
    handleSubmit
  };
};

/**
 * 从对象中提取表单需要的字段
 * @param source 源对象
 * @param fields 需要提取的字段数组
 * @returns 提取后的新对象
 */
export const extractFormFields = <T extends Record<string, any>>(
  source: T, 
  fields: (keyof T)[]
): Partial<T> => {
  return fields.reduce((result, field) => {
    if (field in source) {
      result[field] = source[field];
    }
    return result;
  }, {} as Partial<T>);
};

/**
 * 创建初始表单值
 * @param initialValues 初始值对象
 * @returns 一个用于表单数据的ref对象
 */
export const createFormData = <T extends Record<string, any>>(initialValues: T) => {
  return ref<T>({ ...initialValues });
};

// 需要在导入时补充 ElMessage 导入
// import { ElMessage } from 'element-plus'; 