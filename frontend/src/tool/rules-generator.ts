/**
 * Element Plus 表单验证规则生成器
 * 提供常用表单验证规则的预配置
 */

import { createRules, required, length, numberRange, email, phone, idCard, courseCode, credits } from './validation';

// 课程管理表单规则
export const courseRules = createRules({
  courseCode: [required, (value: string) => courseCode(value)],
  courseName: [required, (value: string) => length(value, 2, 50)],
  departmentId: [required],
  teacherId: [required],
  credits: [required, (value: number) => credits(value)],
  courseType: [required],
  semester: [required],
  capacity: [required, (value: number) => numberRange(value, 1, 500)],
  status: [required],
  description: [(value: string) => length(value, 0, 500)]
});

// 用户表单规则
export const userRules = createRules({
  username: [required, (value: string) => length(value, 4, 20)],
  password: [required, (value: string) => length(value, 6, 20)],
  email: [required, (value: string) => email(value)],
  phone: [(value: string) => phone(value)],
  realName: [required, (value: string) => length(value, 2, 20)],
  idCard: [(value: string) => idCard(value)]
});

// 学生表单规则
export const studentRules = createRules({
  studentNumber: [required, (value: string) => length(value, 6, 20)],
  realName: [required, (value: string) => length(value, 2, 20)],
  idCard: [(value: string) => idCard(value)],
  phone: [(value: string) => phone(value)],
  email: [(value: string) => email(value)],
  grade: [required],
  classId: [required],
  dormitory: [(value: string) => length(value, 0, 50)]
});

// 教师表单规则
export const teacherRules = createRules({
  teacherNumber: [required, (value: string) => length(value, 6, 20)],
  realName: [required, (value: string) => length(value, 2, 20)],
  departmentId: [required],
  title: [required],
  phone: [(value: string) => phone(value)],
  email: [(value: string) => email(value)]
});

// 导出所有预设规则
export const formRules = {
  course: courseRules,
  user: userRules,
  student: studentRules,
  teacher: teacherRules
};

/**
 * 使用示例:
 * 
 * import { formRules } from '@/tool/rules-generator';
 * 
 * // 在组件中
 * const rules = formRules.course;
 * 
 * // 或者
 * import { courseRules } from '@/tool/rules-generator';
 * 
 * const rules = courseRules;
 */ 