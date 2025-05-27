<template>
  <div class="main-layout">
    <el-container>
      <el-aside width="220px">
        <div class="logo-container">
          <img src="../assets/logo.png" alt="校园系统" class="logo">
          <h2>校园管理系统</h2>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          class="side-menu"
          background-color="#001529"
          text-color="#fff"
          active-text-color="#ffd04b"
          router
        >
          <!-- 通用菜单 -->
          <el-menu-item index="/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          
          <!-- 根据角色显示不同菜单 -->
          <template v-if="userRole === 'ROLE_ADMIN' || userRole === 'ROLE_NOTICE_ADMIN'">
            <!-- 通知管理菜单 -->
            <el-sub-menu index="/notification">
              <template #title>
                <el-icon><Bell /></el-icon>
                <span>通知管理</span>
              </template>
              <el-menu-item index="/notification/list">通知列表</el-menu-item>
              <el-menu-item index="/notification/publish">发布通知</el-menu-item>
            </el-sub-menu>
          </template>
          
          <!-- 超级管理员特有菜单 -->
          <template v-if="userRole === 'ROLE_ADMIN'">
            <!-- 用户管理菜单 -->
            <el-sub-menu index="/user">
              <template #title>
                <el-icon><User /></el-icon>
                <span>用户管理</span>
              </template>
              <el-menu-item index="/user/list">用户列表</el-menu-item>
              <el-menu-item index="/user/roles">角色管理</el-menu-item>
              <el-menu-item index="/admin/notification-managers">通知管理员</el-menu-item>
            </el-sub-menu>
            
            <el-sub-menu index="/course">
              <template #title>
                <el-icon><Reading /></el-icon>
                <span>课程管理</span>
              </template>
              <el-menu-item index="/course/list">课程列表</el-menu-item>
              <el-menu-item index="/course/schedule">课表安排</el-menu-item>
            </el-sub-menu>
          </template>
          
          <!-- 教师菜单 -->
          <template v-if="userRole === 'ROLE_TEACHER'">
            <!-- 教师菜单项 -->
          </template>
          
          <!-- 学生菜单 -->
          <template v-if="userRole === 'ROLE_STUDENT'">
            <!-- 学生菜单项 -->
          </template>
          
          <!-- 用户设置 -->
          <el-menu-item index="/profile">
            <el-icon><Setting /></el-icon>
            <span>个人设置</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-container>
        <el-header height="60px">
          <div class="header-left">
            <el-icon class="collapse-btn" @click="toggleCollapse"><Fold /></el-icon>
            <breadcrumb />
          </div>
          <div class="header-right">
            <el-dropdown trigger="click" @command="handleCommand">
              <span class="dropdown-link">
                <el-avatar :size="32" :src="userAvatar"></el-avatar>
                <span>{{ userName }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="changePassword">修改密码</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <el-main>
          <router-view v-slot="{ Component }">
            <keep-alive>
              <component :is="Component" />
            </keep-alive>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
    
    <!-- 通知抽屉 -->
    <el-drawer
      v-model="notificationDrawer"
      title="通知列表"
      direction="rtl"
      size="350px"
    >
      <template v-if="notifications.length">
        <div class="notification-list">
          <div 
            v-for="(notification, index) in notifications" 
            :key="index"
            class="notification-item"
            :class="{ 'notification-unread': !notification.read }"
            @click="viewNotification(notification)"
          >
            <div class="notification-title">
              <el-tag v-if="notification.type === '紧急'" type="danger" size="small">紧急</el-tag>
              <el-tag v-else-if="notification.type === '重要'" type="warning" size="small">重要</el-tag>
              <span>{{ notification.title }}</span>
            </div>
            <div class="notification-time">{{ formatDate(notification.publishTime) }}</div>
          </div>
        </div>
      </template>
      <template v-else>
        <el-empty description="暂无通知" />
      </template>
    </el-drawer>
    
    <!-- 通知详情弹窗 -->
    <el-dialog
      v-model="notificationDialog"
      :title="currentNotification.title"
      width="600px"
    >
      <div class="notification-meta">
        <span><strong>发布人:</strong> {{ currentNotification.publisher }}</span>
        <span><strong>时间:</strong> {{ formatDate(currentNotification.publishTime) }}</span>
        <span><strong>类型:</strong> {{ currentNotification.type }}</span>
      </div>
      <div class="notification-content" v-html="currentNotification.content"></div>
      <div v-if="currentNotification.attachmentUrl" class="notification-attachment">
        <el-button type="primary" link :icon="Download" @click="downloadAttachment">
          下载附件
        </el-button>
      </div>
    </el-dialog>
    
    <!-- 修改密码弹窗 -->
    <el-dialog
      v-model="passwordDialog"
      title="修改密码"
      width="400px"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input 
            v-model="passwordForm.oldPassword" 
            type="password" 
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password" 
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password" 
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span>
          <el-button @click="passwordDialog = false">取消</el-button>
          <el-button type="primary" @click="handleChangePassword">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import {
  HomeFilled,
  User,
  Reading,
  Bell,
  Setting,
  Download,
  Fold,
  ArrowDown
} from '@element-plus/icons-vue'
import axios from 'axios'
import Breadcrumb from '@/components/Breadcrumb.vue'
import { encryptPassword } from '@/utils/crypto'

const router = useRouter()
const route = useRoute()
const store = useStore()

// 侧边栏状态
const isCollapse = ref(false)
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 菜单激活状态
const activeMenu = computed(() => route.path)

// 用户信息
const userName = ref('管理员')
const userAvatar = ref('https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png')
const userRole = ref('ROLE_ADMIN') // 默认角色

// 通知相关
const notificationCount = ref(0)
interface Notification {
  id: number;
  title: string;
  content: string;
  type: string;
  publishTime: string;
  publisher?: string;
  read: boolean;
  attachmentUrl?: string;
}
const notifications = ref<Notification[]>([])
const notificationDrawer = ref(false)
const notificationDialog = ref(false)
const currentNotification = ref<Notification>({} as Notification)

const fetchNotifications = async () => {
  try {
    // TODO: 替换为实际的API调用
    const response = await axios.get<{data: Notification[]}>('/api/notification/user')
    notifications.value = response.data.data as Notification[]
    notificationCount.value = notifications.value.filter(n => !n.read).length
  } catch (error) {
    console.error('获取通知失败:', error)
    ElMessage.error('获取通知失败')
  }
}

const viewNotification = (notification: Notification) => {
  currentNotification.value = notification
  notificationDialog.value = true
  
  // 标记为已读
  if (!notification.read) {
    markAsRead(notification.id)
  }
}

const markAsRead = async (id: number) => {
  try {
    // TODO: 替换为实际的API调用
    await axios.post(`/api/notification/${id}/read`)
    
    // 更新通知状态
    const index = notifications.value.findIndex(n => n.id === id)
    if (index !== -1) {
      notifications.value[index].read = true
      notificationCount.value = notifications.value.filter(n => !n.read).length
    }
  } catch (error) {
    console.error('标记通知已读失败:', error)
  }
}

const downloadAttachment = () => {
  if (currentNotification.value.attachmentUrl) {
    window.open(currentNotification.value.attachmentUrl, '_blank')
  }
}

// 用户操作
const handleCommand = (command: string) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'changePassword') {
    passwordDialog.value = true
  } else if (command === 'logout') {
    ElMessageBox.confirm(
      '确定要退出登录吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    ).then(() => {
      store.dispatch('user/logout')
      router.push('/login')
    }).catch(() => {})
  }
}

// 修改密码
const passwordDialog = ref(false)
const passwordFormRef = ref<FormInstance | null>(null)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        const userId = store.state.user.userInfo.id
        
        // 对密码进行加密
        const encryptedOldPassword = encryptPassword(passwordForm.value.oldPassword)
        const encryptedNewPassword = encryptPassword(passwordForm.value.newPassword)
        
        await axios.post(`/api/user/${userId}/change-password`, {
          oldPassword: encryptedOldPassword,
          newPassword: encryptedNewPassword
        })
        
        ElMessage.success('密码修改成功，请重新登录')
        passwordDialog.value = false
        
        // 退出登录
        store.dispatch('user/logout')
        router.push('/login')
      } catch (error: Error | unknown) {
        console.error('修改密码失败:', error)
        const typedError = error as { response?: { data?: { error?: string } } }
        ElMessage.error(typedError.response?.data?.error || '修改密码失败')
      }
    }
  })
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 初始化
onMounted(() => {
  fetchNotifications()
})
</script>

<style scoped>
.main-layout {
  height: 100vh;
  overflow: hidden;
}

.el-container {
  height: 100%;
}

.el-aside {
  background-color: #001529;
  transition: width 0.3s;
  overflow-x: hidden;
}

.logo-container {
  height: 60px;
  padding: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #001529;
  color: white;
}

.logo {
  height: 40px;
  margin-right: 10px;
}

.logo-container h2 {
  margin: 0;
  font-size: 18px;
}

.side-menu {
  height: calc(100vh - 60px);
  border-right: none;
}

.el-header {
  background-color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-btn {
  font-size: 20px;
  margin-right: 20px;
  cursor: pointer;
}

.header-right {
  display: flex;
  align-items: center;
}

.dropdown-link {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.el-main {
  padding: 20px;
  overflow-y: auto;
  background-color: #f5f7f9;
}

.notification-list {
  max-height: calc(100vh - 150px);
  overflow-y: auto;
}

.notification-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
}

.notification-item:hover {
  background-color: #f5f7fa;
}

.notification-unread {
  background-color: #e6f7ff;
}

.notification-title {
  font-weight: 500;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.notification-time {
  font-size: 12px;
  color: #999;
}

.notification-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 16px;
  color: #666;
  font-size: 14px;
}

.notification-content {
  line-height: 1.6;
  margin-bottom: 16px;
}

.notification-attachment {
  margin-top: 16px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style> 