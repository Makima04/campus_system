<!-- 管理员布局组件 - 提供统一的导航栏和布局结构 -->
<template>
  <div class="admin-layout">
    <el-container class="admin-container">
      <!-- 侧边导航栏 -->
      <el-aside width="220px">
        <div class="logo">校园管理系统</div>
        <el-menu
          class="sidebar-menu"
          :router="true"
          :default-active="activeMenu"
          background-color="#001529"
          text-color="#fff"
          active-text-color="#1890ff"
        >
          <el-menu-item index="/admin">
            <el-icon><HomeFilled /></el-icon>
            <span>控制台</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/user">
            <el-icon><UserFilled /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/student">
            <el-icon><User /></el-icon>
            <span>学生管理</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/course">
            <el-icon><Reading /></el-icon>
            <span>课程管理</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/data-management">
            <el-icon><DataLine /></el-icon>
            <span>数据管理</span>
          </el-menu-item>
          
          <el-menu-item index="/file/demo">
            <el-icon><Upload /></el-icon>
            <span>文件上传</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/redis">
            <el-icon><Monitor /></el-icon>
            <span>Redis缓存</span>
          </el-menu-item>
          
          <el-sub-menu index="logs">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>日志管理</span>
            </template>
            <el-menu-item index="/admin/logs">
              <el-icon><List /></el-icon>
              <span>系统日志</span>
            </el-menu-item>
            <el-menu-item index="/admin/archived-logs">
              <el-icon><Files /></el-icon>
              <span>归档日志</span>
            </el-menu-item>
          </el-sub-menu>
          
          <el-menu-item index="/" @click="goToHome">
            <el-icon><Back /></el-icon>
            <span>返回首页</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部导航栏 -->
        <el-header height="60px">
          <div class="breadcrumb">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="currentRouteMeta.title">{{ currentRouteMeta.title }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          
          <div class="header-right">
            <el-dropdown trigger="click" @command="handleCommand">
              <span class="el-dropdown-link">
                <el-avatar :size="32" icon="UserFilled" />
                <span>{{ username }}</span>
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <!-- 页面内容 -->
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  HomeFilled, 
  UserFilled, 
  User, 
  Reading, 
  DataLine, 
  Upload, 
  Monitor, 
  Back, 
  ArrowDown,
  Document,
  List,
  Files
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const username = ref('管理员')

// 计算当前激活的菜单项
const activeMenu = computed(() => route.path)

// 计算当前路由的元信息
const currentRouteMeta = computed(() => route.meta || {})

// 返回首页
const goToHome = () => {
  router.push('/')
}

// 处理下拉菜单命令
const handleCommand = (command: string) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    ElMessage.success('退出登录成功')
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/admin/profile')
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  width: 100%;
}

.admin-container {
  height: 100%;
}

/* 侧边栏样式 */
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  background-color: #002140;
}

.el-aside {
  background-color: #001529;
  transition: width 0.3s;
}

.sidebar-menu {
  border-right: none;
  height: calc(100% - 60px);
}

/* 顶部导航栏样式 */
.el-header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.breadcrumb {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.el-dropdown-link span {
  margin: 0 8px;
}

/* 主内容区样式 */
.el-main {
  padding: 20px;
  background-color: #f0f2f5;
  overflow: auto;
}
</style> 