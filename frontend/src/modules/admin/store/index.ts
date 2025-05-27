/**
 * 管理员模块状态管理
 */
import { defineStore } from 'pinia';
import * as adminApi from '../api';

// 通知类型定义
export interface Notification {
  id: number;
  title: string;
  content: string;
  createdBy: string;
  createdAt: string;
  publishedAt?: string;
  status: 'DRAFT' | 'PUBLISHED';
  targetGroups: string[];
  [key: string]: any;
}

// 仪表板统计数据类型
export interface DashboardStats {
  userCount: number;
  studentCount: number;
  teacherCount: number;
  courseCount: number;
  activeUsers: number;
  [key: string]: any;
}

export const useAdminStore = defineStore('admin', {
  state: () => ({
    // 通知相关状态
    notifications: [] as Notification[],
    notificationTotalCount: 0,
    currentNotification: null as Notification | null,
    
    // 仪表板相关状态
    dashboardStats: null as DashboardStats | null,
    systemSettings: null as any,
    
    // 通用状态
    loading: false,
    error: null as string | null,
    currentPage: 1,
    pageSize: 10
  }),
  
  getters: {
    hasNotifications: (state) => state.notifications.length > 0,
    getNotificationById: (state) => (id: number) => {
      return state.notifications.find((notification: Notification) => notification.id === id) || null;
    }
  },
  
  actions: {
    // 通知相关操作
    async fetchNotifications(params: any = {}) {
      this.loading = true;
      this.error = null;
      
      if (!params.page) {
        params.page = this.currentPage;
      }
      
      if (!params.size) {
        params.size = this.pageSize;
      }
      
      try {
        const response = await adminApi.getNotifications(params);
        this.notifications = response.data.content || response.data || [];
        this.notificationTotalCount = response.data.totalElements || this.notifications.length;
        this.currentPage = params.page;
        return this.notifications;
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取通知列表失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async fetchNotificationById(id: number) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await adminApi.getNotificationById(id);
        this.currentNotification = response.data;
        return response.data;
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取通知详情失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async createNotification(notificationData: any) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await adminApi.createNotification(notificationData);
        return response.data;
      } catch (error: any) {
        this.error = error.response?.data?.message || '创建通知失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async updateNotification(id: number, notificationData: any) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await adminApi.updateNotification(id, notificationData);
        
        // 更新列表中的通知
        const index = this.notifications.findIndex((notification: Notification) => notification.id === id);
        if (index > -1) {
          this.notifications[index] = response.data;
        }
        
        this.currentNotification = response.data;
        return response.data;
      } catch (error: any) {
        this.error = error.response?.data?.message || '更新通知失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async deleteNotification(id: number) {
      this.loading = true;
      this.error = null;
      
      try {
        await adminApi.deleteNotification(id);
        
        // 从列表中删除通知
        this.notifications = this.notifications.filter((notification: Notification) => notification.id !== id);
        
        if (this.currentNotification && this.currentNotification.id === id) {
          this.currentNotification = null;
        }
        
        return true;
      } catch (error: any) {
        this.error = error.response?.data?.message || '删除通知失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async publishNotification(id: number) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await adminApi.publishNotification(id);
        
        // 更新列表中的通知
        const index = this.notifications.findIndex((notification: Notification) => notification.id === id);
        if (index > -1) {
          this.notifications[index] = response.data;
        }
        
        if (this.currentNotification && this.currentNotification.id === id) {
          this.currentNotification = response.data;
        }
        
        return response.data;
      } catch (error: any) {
        this.error = error.response?.data?.message || '发布通知失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    // 仪表板相关操作
    async fetchDashboardStats() {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await adminApi.getDashboardStats();
        this.dashboardStats = response.data;
        return response.data;
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取仪表板数据失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    // 系统设置相关操作
    async fetchSystemSettings() {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await adminApi.getSystemSettings();
        this.systemSettings = response.data;
        return response.data;
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取系统设置失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async updateSystemSettings(settingsData: any) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await adminApi.updateSystemSettings(settingsData);
        this.systemSettings = response.data;
        return response.data;
      } catch (error: any) {
        this.error = error.response?.data?.message || '更新系统设置失败';
        throw error;
      } finally {
        this.loading = false;
      }
    }
  }
}); 