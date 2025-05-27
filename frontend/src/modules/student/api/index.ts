/**
 * 学生模块API
 */
import { get, post, put } from '../../../utils/http';
import { getApiBaseUrl } from '../../../config';
import axios from 'axios'

// API端点
const API_ENDPOINTS = {
  // 课程相关API
  getCourses: `${getApiBaseUrl()}/student/courses`,
  getCourseById: (id: number) => `${getApiBaseUrl()}/student/courses/${id}`,
  enrollCourse: (id: number) => `${getApiBaseUrl()}/student/courses/${id}/enroll`,
  dropCourse: (id: number) => `${getApiBaseUrl()}/student/courses/${id}/drop`,
  
  // 作业相关API
  getAssignments: `${getApiBaseUrl()}/student/assignments`,
  getAssignmentById: (id: number) => `${getApiBaseUrl()}/student/assignments/${id}`,
  submitAssignment: (id: number) => `${getApiBaseUrl()}/student/assignments/${id}/submit`,
  
  // 成绩相关API
  getGrades: `${getApiBaseUrl()}/student/grades`,
  getGradeByCourse: (courseId: number) => `${getApiBaseUrl()}/student/grades/course/${courseId}`
};

/**
 * 获取学生可选课程列表
 * @param params 查询参数
 */
export const getCourses = (params?: any) => {
  return get(API_ENDPOINTS.getCourses, { params });
};

/**
 * 获取课程详情
 * @param id 课程ID
 */
export const getCourseById = (id: number) => {
  return get(API_ENDPOINTS.getCourseById(id));
};

/**
 * 学生选课
 * @param id 课程ID
 */
export const enrollCourse = (id: number) => {
  return post(API_ENDPOINTS.enrollCourse(id));
};

/**
 * 学生退课
 * @param id 课程ID
 */
export const dropCourse = (id: number) => {
  return post(API_ENDPOINTS.dropCourse(id));
};

/**
 * 获取学生作业列表
 * @param params 查询参数
 */
export const getAssignments = (params?: any) => {
  return get(API_ENDPOINTS.getAssignments, { params });
};

/**
 * 获取作业详情
 * @param id 作业ID
 */
export const getAssignmentById = (id: number) => {
  return get(API_ENDPOINTS.getAssignmentById(id));
};

/**
 * 提交作业
 * @param id 作业ID
 * @param data 提交的数据
 */
export const submitAssignment = (id: number, data: any) => {
  return post(API_ENDPOINTS.submitAssignment(id), data);
};

/**
 * 获取学生成绩列表
 * @param params 查询参数
 */
export const getGrades = (params?: any) => {
  return get(API_ENDPOINTS.getGrades, { params });
};

/**
 * 获取特定课程的成绩
 * @param courseId 课程ID
 */
export const getGradeByCourse = (courseId: number) => {
  return get(API_ENDPOINTS.getGradeByCourse(courseId));
};

const baseURL = '/api/student'

export const api = {
  // 获取课表
  getSchedule: async (studentId: number, semester: string) => {
    const response = await axios.get(`${baseURL}/courses/schedule`, {
      params: { studentId, semester }
    })
    return response.data
  },

  // 获取可选课程
  getAvailableCourses: async (studentId: number, semester: string, page: number, size: number) => {
    const response = await axios.get(`${baseURL}/courses/available`, {
      params: { studentId, semester, page, size }
    })
    return response.data
  },

  // 获取已选课程
  getSelectedCourses: async (studentId: number, semester: string) => {
    const response = await axios.get(`${baseURL}/courses/selected`, {
      params: { studentId, semester }
    })
    return response.data
  },

  // 选课
  enrollCourse: async (studentId: number, courseId: number) => {
    const response = await axios.post(`${baseURL}/courses/${courseId}/enroll`, null, {
      params: { studentId }
    })
    return response.data
  },

  // 退课
  dropCourse: async (studentId: number, courseId: number) => {
    const response = await axios.delete(`${baseURL}/courses/${courseId}/drop`, {
      params: { studentId }
    })
    return response.data
  },

  // 获取课程详情
  getCourseDetail: async (studentId: number, courseId: number) => {
    const response = await axios.get(`${baseURL}/courses/${courseId}`, {
      params: { studentId }
    })
    return response.data
  }
} 