<template>
  <div class="admin-dashboard">
    <div class="content">
      <!-- 系统概览卡片 -->
      <h2>系统概览</h2>
      <el-row :gutter="20" class="overview-cards" v-loading="loading">
        <el-col :span="6">
          <el-card shadow="hover" class="overview-card">
            <template #header>
              <div class="card-header">
                <el-icon><User /></el-icon>
                <span>用户总数</span>
              </div>
            </template>
            <div class="card-content">
              <span class="number">{{ statistics?.userCount || 0 }}</span>
              <span class="label">人</span>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="overview-card">
            <template #header>
              <div class="card-header">
                <el-icon><Reading /></el-icon>
                <span>课程总数</span>
              </div>
            </template>
            <div class="card-content">
              <span class="number">{{ statistics?.courseCount || 0 }}</span>
              <span class="label">门</span>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="overview-card">
            <template #header>
              <div class="card-header">
                <el-icon><List /></el-icon>
                <span>选课记录</span>
              </div>
            </template>
            <div class="card-content">
              <span class="number">{{ statistics?.selectionCount || 0 }}</span>
              <span class="label">条</span>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="overview-card">
            <template #header>
              <div class="card-header">
                <el-icon><Wallet /></el-icon>
                <span>饭卡交易</span>
              </div>
            </template>
            <div class="card-content">
              <span class="number">{{ statistics?.transactionCount || 0 }}</span>
              <span class="label">笔</span>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <h2>功能列表</h2>
      <div class="menu-list">
        <div class="menu-item" @click="router.push('/admin/user')">
          <div class="menu-item-header">
            <el-icon><UserFilled /></el-icon>
            <h3>用户管理</h3>
          </div>
          <p>管理系统用户账号和权限</p>
          <el-button type="primary" size="small" class="menu-item-btn" @click.stop="router.push('/admin/user')">进入</el-button>
        </div>
        <div class="menu-item" @click="router.push('/admin/student')">
          <div class="menu-item-header">
            <el-icon><User /></el-icon>
            <h3>学生管理</h3>
          </div>
          <p>管理学生信息和课程</p>
          <el-button type="primary" size="small" class="menu-item-btn">进入</el-button>
        </div>
        <div class="menu-item" @click="router.push('/admin/course')">
          <div class="menu-item-header">
            <el-icon><Document /></el-icon>
            <h3>课程管理</h3>
          </div>
          <p>管理学校课程和教学计划</p>
          <el-button type="primary" size="small" class="menu-item-btn">进入</el-button>
        </div>
        <div class="menu-item" @click="router.push('/admin/data-management')">
          <div class="menu-item-header">
            <el-icon><DataLine /></el-icon>
            <h3>数据管理</h3>
          </div>
          <p>管理课程、选课记录、饭卡和交易记录</p>
          <el-button type="primary" size="small" class="menu-item-btn">进入</el-button>
        </div>
        <div class="menu-item" @click="router.push('/file/demo')">
          <div class="menu-item-header">
            <el-icon><Upload /></el-icon>
            <h3>文件上传</h3>
          </div>
          <p>上传和管理系统文件</p>
          <el-button type="primary" size="small" class="menu-item-btn">进入</el-button>
        </div>
        <div class="menu-item" @click="router.push('/admin/redis')">
          <div class="menu-item-header">
            <el-icon><Monitor /></el-icon>
            <h3>Redis缓存管理</h3>
          </div>
          <p>管理系统Redis缓存</p>
          <el-button type="primary" size="small" class="menu-item-btn">进入</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from '@vue/runtime-dom'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { HomeFilled, UserFilled, User, Document, Upload, Monitor, Reading, List, Wallet, DataLine } from '@element-plus/icons-vue'
import http from '@/utils/http'

interface Statistics {
  userCount: number
  courseCount: number
  selectionCount: number
  transactionCount: number
}

interface ApiResponse<T> {
  data: T
  code: number
  message: string
}

const router = useRouter()

const statistics = ref<Statistics>({
  userCount: 0,
  courseCount: 0,
  selectionCount: 0,
  transactionCount: 0
})

const loading = ref(false)

// 获取系统统计数据
const fetchStatistics = async () => {
  loading.value = true
  try {
    const response = await http.get<ApiResponse<Statistics>>('/admin/statistics')
    if (response.data && response.data.data) {
      statistics.value = response.data.data
    } else {
      console.warn('统计数据格式不正确:', response)
      ElMessage.warning('获取统计数据失败，使用默认值')
    }
  } catch (error) {
    console.error('获取系统统计数据失败:', error)
    ElMessage.error('获取统计数据失败，使用默认值')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStatistics()
})
</script>

<style scoped>
.admin-dashboard {
  width: 100%;
}

.content {
  padding: 20px;
  background-color: #fff;
  min-height: calc(100vh - 140px);
}

/* 统计卡片 */
.overview-cards {
  margin-bottom: 30px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-content {
  display: flex;
  align-items: baseline;
  justify-content: center;
  padding: 10px 0;
}

.number {
  font-size: 36px;
  font-weight: bold;
  color: #1890ff;
  margin-right: 5px;
}

.label {
  font-size: 14px;
  color: #666;
}

/* 功能菜单 */
.menu-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.menu-item {
  border-radius: 8px;
  background-color: #f5f7fa;
  padding: 20px;
  transition: all 0.3s;
  cursor: pointer;
  position: relative;
}

.menu-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.menu-item-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.menu-item-header h3 {
  margin: 0;
  font-size: 18px;
}

.menu-item p {
  color: #666;
  margin-bottom: 15px;
}

.menu-item-btn {
  position: absolute;
  bottom: 20px;
  right: 20px;
}
</style> 