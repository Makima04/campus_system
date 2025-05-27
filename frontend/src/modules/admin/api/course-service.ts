/**
 * 课程管理服务
 */
import { get, post, put, del } from '@/utils/http';
import { getApiBaseUrl } from '@/config';

// 服务基础路径
const BASE_PATH = '/api/admin';

// API端点
const API_ENDPOINTS = {
  getCourses: `${BASE_PATH}/course/list`,
  getCourse: (id: number) => `${BASE_PATH}/course/${id}`,
  createCourse: `${BASE_PATH}/course`,
  updateCourse: (id: number) => `${BASE_PATH}/course/${id}`,
  deleteCourse: (id: number) => `${BASE_PATH}/course/${id}`,
  getDepartments: `${BASE_PATH}/department/list`,
  searchTeachers: `${BASE_PATH}/teacher/search`,
  exportTemplate: `${BASE_PATH}/course/export/template`,
  importCourses: `${BASE_PATH}/course/import`,
  exportCourses: `${BASE_PATH}/course/export`
};

// 调试日志API端点
console.log('Course API Endpoints:', API_ENDPOINTS);

/**
 * 获取课程列表
 * @param params 查询参数 (页码、每页数量、搜索关键词等)
 */
export const getCourses = (params?: any) => {
  console.log('调用课程列表API', API_ENDPOINTS.getCourses, params);
  return get(API_ENDPOINTS.getCourses, params);
};

/**
 * 获取课程详情
 * @param id 课程ID
 */
export const getCourseById = (id: number) => {
  return get(API_ENDPOINTS.getCourse(id));
};

/**
 * 创建课程
 * @param courseData 课程数据
 */
export const createCourse = (courseData: any) => {
  return post(API_ENDPOINTS.createCourse, courseData);
};

/**
 * 更新课程
 * @param id 课程ID
 * @param courseData 课程数据
 */
export const updateCourse = (id: number, courseData: any) => {
  return put(API_ENDPOINTS.updateCourse(id), courseData);
};

/**
 * 删除课程
 * @param id 课程ID
 */
export const deleteCourse = (id: number) => {
  return del(API_ENDPOINTS.deleteCourse(id));
};

/**
 * 获取院系列表
 */
export const getDepartments = () => {
  return get(API_ENDPOINTS.getDepartments);
};

/**
 * 搜索教师
 * @param query 查询关键词
 */
export const searchTeachers = (query: string) => {
  return get(API_ENDPOINTS.searchTeachers, { query });
};

/**
 * 获取导出模板URL
 */
export const getExportTemplateUrl = () => {
  return API_ENDPOINTS.exportTemplate;
};

/**
 * 导入课程数据
 * @param formData 包含Excel文件的FormData
 */
export const importCourses = (formData: FormData) => {
  return post(API_ENDPOINTS.importCourses, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
};

/**
 * 导出课程数据
 * @param params 导出参数，如筛选条件
 */
export const exportCourses = (params?: any) => {
  return `${API_ENDPOINTS.exportCourses}?${new URLSearchParams(params).toString()}`;
};

// 导出API服务对象
const courseService = {
  getCourses,
  getCourseById,
  createCourse,
  updateCourse,
  deleteCourse,
  getDepartments,
  searchTeachers,
  getExportTemplateUrl,
  importCourses,
  exportCourses
};

export default courseService; 