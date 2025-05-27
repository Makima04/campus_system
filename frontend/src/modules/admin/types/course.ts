/**
 * 课程相关类型定义
 */

/**
 * 课程信息
 */
export interface Course {
  id: number;
  courseCode: string;  // 课程编号
  courseName: string;  // 课程名称
  departmentId: number;  // 开课院系ID
  departmentName: string;  // 开课院系名称
  teacherId: number;  // 教师ID
  teacherName: string;  // 教师名称
  credits: number;  // 学分
  courseType: string;  // 课程类型: REQUIRED(必修), ELECTIVE(选修), PUBLIC(公共课)
  semester: string;  // 学期，例如: 2023-2024-1
  capacity: number;  // 容量
  selectedCount: number;  // 已选人数
  status: string;  // 课程状态: NOT_STARTED(未开始), IN_PROGRESS(进行中), ENDED(已结束)
  description?: string;  // 课程描述
  classTime: string;  // 上课时间
  classroom: string;  // 上课地点
  weeks: string;  // 上课周次
  sections: string;  // 上课节次
}

/**
 * 课程表单数据
 */
export interface CourseForm {
  id?: number;  // 课程ID，新增时不需要
  courseCode: string;  // 课程编号
  courseName: string;  // 课程名称
  departmentId: number;  // 开课院系ID
  teacherId: number;  // 教师ID
  credits: number;  // 学分
  courseType: string;  // 课程类型
  semester: string;  // 学期
  capacity: number;  // 容量
  status: string;  // 课程状态
  description?: string;  // 课程描述
  classTime: string;  // 上课时间
  classroom: string;  // 上课地点
  weeks: string;  // 上课周次
  sections: string;  // 上课节次
}

/**
 * 院系信息
 */
export interface Department {
  id: number;  // 院系ID
  name: string;  // 院系名称
  code: string;  // 院系编码
}

/**
 * 教师信息
 */
export interface Teacher {
  id: number;  // 教师ID
  realName: string;  // 教师姓名
  department: string;  // 所属院系
}

/**
 * 课程类型枚举
 */
export enum CourseType {
  REQUIRED = 'REQUIRED',  // 必修课
  ELECTIVE = 'ELECTIVE',  // 选修课
  PUBLIC = 'PUBLIC'  // 公共课
}

/**
 * 课程状态枚举
 */
export enum CourseStatus {
  NOT_STARTED = 'NOT_STARTED',  // 未开始
  IN_PROGRESS = 'IN_PROGRESS',  // 进行中
  ENDED = 'ENDED'  // 已结束
}

/**
 * 课程类型标签映射
 */
export const courseTypeTagMap: Record<string, string> = {
  [CourseType.REQUIRED]: 'danger',
  [CourseType.ELECTIVE]: 'success',
  [CourseType.PUBLIC]: 'warning'
};

/**
 * 课程类型名称映射
 */
export const courseTypeLabelMap: Record<string, string> = {
  [CourseType.REQUIRED]: '必修课',
  [CourseType.ELECTIVE]: '选修课',
  [CourseType.PUBLIC]: '公共课'
};

/**
 * 课程状态标签映射
 */
export const courseStatusTagMap: Record<string, string> = {
  [CourseStatus.NOT_STARTED]: 'info',
  [CourseStatus.IN_PROGRESS]: 'success',
  [CourseStatus.ENDED]: 'warning'
};

/**
 * 课程状态名称映射
 */
export const courseStatusLabelMap: Record<string, string> = {
  [CourseStatus.NOT_STARTED]: '未开始',
  [CourseStatus.IN_PROGRESS]: '进行中',
  [CourseStatus.ENDED]: '已结束'
}; 