/**
 * 用户模块状态管理
 */
import { defineStore } from 'pinia';
import * as userApi from '../api';

// 用户接口定义
export interface User {
  id: number;
  username: string;
  email: string;
  role: string;
  avatar?: string;
  createdAt?: string;
  [key: string]: any;
}

export const useUserStore = defineStore('user', {
  state: () => ({
    currentUser: null as User | null,
    userList: [] as User[],
    loading: false,
    error: null as string | null,
    totalUsers: 0,
    currentPage: 1,
    pageSize: 10,
    detailsLoading: false
  }),
  
  getters: {
    isUserListEmpty: (state) => state.userList.length === 0,
    getUserById: (state) => (id: number) => {
      return state.userList.find(user => user.id === id) || null;
    }
  },
  
  actions: {
    async fetchUserProfile() {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await userApi.getProfile();
        this.currentUser = response.data;
        return response.data;
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取用户信息失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async updateUserProfile(profileData: any) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await userApi.updateProfile(profileData);
        this.currentUser = response.data;
        return response.data;
      } finally {
        this.loading = false;
      }
    },
    
    async fetchUsers(params: any = {}) {
      this.loading = true;
      this.error = null;
      
      if (!params.page) {
        params.page = this.currentPage;
      }
      
      if (!params.size) {
        params.size = this.pageSize;
      }
      
      try {
        const response = await userApi.getUsers(params);
        this.userList = response.data.content || response.data || [];
        this.totalUsers = response.data.totalElements || this.userList.length;
        this.currentPage = params.page;
        return this.userList;
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取用户列表失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async fetchUserById(id: number) {
      this.detailsLoading = true;
      this.error = null;
      
      try {
        const response = await userApi.getUserById(id);
        const userData = response.data as User;
        
        // 更新列表中的用户数据
        const index = this.userList.findIndex(user => user.id === id);
        if (index > -1) {
          this.userList[index] = userData;
        }
        return userData;
      } catch (error: any) {
        this.error = error.response?.data?.message || '获取用户详情失败';
        throw error;
      } finally {
        this.detailsLoading = false;
      }
    },
    
    async createUser(userData: any) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await userApi.createUser(userData);
        return response.data;
      } catch (error: any) {
        this.error = error.response?.data?.message || '创建用户失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async updateUser(id: number, userData: any) {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await userApi.updateUser(id, userData);
        const updatedUser = response.data as User;
        
        // 更新本地用户列表
        const index = this.userList.findIndex(user => user.id === id);
        if (index > -1) {
          this.userList[index] = updatedUser;
        }
        
        return updatedUser;
      } catch (error: any) {
        this.error = error.response?.data?.message || '更新用户失败';
        throw error;
      } finally {
        this.loading = false;
      }
    },
    
    async deleteUser(id: number) {
      this.loading = true;
      this.error = null;
      
      try {
        await userApi.deleteUser(id);
        
        // 从列表中移除用户
        this.userList = this.userList.filter(user => user.id !== id);
        
        return true;
      } catch (error: any) {
        this.error = error.response?.data?.message || '删除用户失败';
        throw error;
      } finally {
        this.loading = false;
      }
    }
  }
}); 