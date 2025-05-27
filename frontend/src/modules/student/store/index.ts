/**
 * 学生模块状态管理
 */
import { defineStore } from 'pinia';
import * as studentApi from '../api';

// 课程类型定义
export interface Course {
  id: number;
  name: string;
  code: string;
  description: string;
  credit: number;
  instructor: string;
  schedule: string;
  location: string;
  capacity: number;
  enrolled: number;
  status: 'AVAILABLE' | 'FULL' | 'CLOSED';
  isEnrolled?: boolean;
  [key: string]: any;
}

// 作业类型定义
export interface Assignment {
  id: number;
  title: string;
  description: string;
  courseId: number;
  courseName: string;
  dueDate: string;
  status: 'PENDING' | 'SUBMITTED' | 'GRADED';
  grade?: number;
  feedback?: string;
  [key: string]: any;
}

// 成绩类型定义
export interface Grade {
  id: number;
  courseId: number;
  courseName: string;
  score: number;
  grade: string;
  feedback?: string;
  term: string;
  year: number;
  [key: string]: any;
}

export const useStudentStore = defineStore('student', {
  state: () => ({
    // 课程相关状态
    courses: [] as Course[],
    enrolledCourses: [] as Course[],
    currentCourse: null as Course | null,
    
    // 作业相关状态
    assignments: [] as Assignment[],
    currentAssignment: null as Assignment | null,
    
    // 成绩相关状态
    grades: [] as Grade[],
    
    // 通用状态
    loading: false,
    error: null as string | null,
    currentPage: 1,
    pageSize: 10
  }),
  
  getters: {
    availableCourses: (state) => {
      return state.courses.filter(course => course.status === 'AVAILABLE' && !course.isEnrolled);
    },
    pendingAssignments: (state) => {
      return state.assignments.filter(assignment => assignment.status === 'PENDING');
    },
    hasPendingAssignments: (state) => {
      return state.assignments.some(assignment => assignment.status === 'PENDING');
    },
    averageGrade: (state) => {
      if (state.grades.length === 0) return 0;
      const sum = state.grades.reduce((total, grade) => total + grade.score, 0);
      return sum / state.grades.length;
    }
  },
  
  actions: {
    // 课程相关操作
    async fetchCourses(params: any = {}) {
      this.loading = true;
      this.error = null;
      
      if (!params.page) {
        params.page = this.currentPage;
      }
      
      if (!params.size) {
        params.size = this.pageSize;
      }
      
      try {
        const response = await studentApi.getCourses(params);
        this.courses = response.data.content || response.data || [];
        return this.courses;
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取课程列表失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async fetchCourseById(id: number) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await studentApi.getCourseById(id);
        this.currentCourse = response.data;
        return response.data;
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取课程详情失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async enrollCourse(id: number) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await studentApi.enrollCourse(id);
        
        // 更新课程状态
        const courseIndex = this.courses.findIndex(course => course.id === id);
        if (courseIndex > -1) {
          this.courses[courseIndex].isEnrolled = true;
          this.enrolledCourses.push(this.courses[courseIndex]);
        }
        
        return response.data;
      } catch (error: any) {
        this.error = error.response?.data?.message || '选课失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async dropCourse(id: number) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await studentApi.dropCourse(id);
        
        // 更新课程状态
        const courseIndex = this.courses.findIndex(course => course.id === id);
        if (courseIndex > -1) {
          this.courses[courseIndex].isEnrolled = false;
        }
        
        // 从已选课程中移除
        this.enrolledCourses = this.enrolledCourses.filter(course => course.id !== id);
        
        return response.data;
      } catch (error: any) {
        this.error = error.response?.data?.message || '退课失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    // 作业相关操作
    async fetchAssignments(params: any = {}) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await studentApi.getAssignments(params);
        this.assignments = response.data.content || response.data || [];
        return this.assignments;
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取作业列表失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async fetchAssignmentById(id: number) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await studentApi.getAssignmentById(id);
        this.currentAssignment = response.data;
        return response.data;
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取作业详情失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async submitAssignment(id: number, data: any) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await studentApi.submitAssignment(id, data);
        
        // 更新作业状态
        const assignmentIndex = this.assignments.findIndex(assignment => assignment.id === id);
        if (assignmentIndex > -1) {
          this.assignments[assignmentIndex].status = 'SUBMITTED';
        }
        
        if (this.currentAssignment && this.currentAssignment.id === id) {
          this.currentAssignment.status = 'SUBMITTED';
        }
        
        return response.data;
      } catch (error: any) {
        this.error = error.response?.data?.message || '提交作业失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    // 成绩相关操作
    async fetchGrades(params: any = {}) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await studentApi.getGrades(params);
        this.grades = response.data.content || response.data || [];
        return this.grades;
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取成绩列表失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async fetchGradeByCourse(courseId: number) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await studentApi.getGradeByCourse(courseId);
        
        // 更新成绩列表
        const gradeIndex = this.grades.findIndex(grade => grade.courseId === courseId);
        if (gradeIndex > -1) {
          this.grades[gradeIndex] = response.data;
        } else {
          this.grades.push(response.data);
        }
        
        return response.data;
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取课程成绩失败';
        throw error;
      } finally {
        this.loading = false;
      }
    }
  }
}); 