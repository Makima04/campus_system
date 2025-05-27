<template>
  <div class="data-management">
    <h2 class="page-title">数据管理</h2>
    
    <el-row :gutter="20">
      <!-- 课程管理卡片 -->
      <el-col :span="8">
        <el-card class="management-card" shadow="hover" @click="goToCourseManagement">
          <template #header>
            <div class="card-header">
              <el-icon><Reading /></el-icon>
              <span>课程管理</span>
            </div>
          </template>
          <div class="card-content">
            <p>管理课程信息，包括添加、修改、删除课程等操作</p>
          </div>
        </el-card>
      </el-col>

      <!-- 选课记录管理卡片 -->
      <el-col :span="8">
        <el-card class="management-card" shadow="hover" @click="goToCourseSelectionManagement">
          <template #header>
            <div class="card-header">
              <el-icon><List /></el-icon>
              <span>选课记录管理</span>
            </div>
          </template>
          <div class="card-content">
            <p>管理学生选课记录，包括查看、修改、删除选课记录等操作</p>
          </div>
        </el-card>
      </el-col>

      <!-- 饭卡管理卡片 -->
      <el-col :span="8">
        <el-card class="management-card" shadow="hover" @click="goToMealCardManagement">
          <template #header>
            <div class="card-header">
              <el-icon><Wallet /></el-icon>
              <span>饭卡管理</span>
            </div>
          </template>
          <div class="card-content">
            <p>管理学生饭卡信息，包括余额查询、充值、消费等操作</p>
          </div>
        </el-card>
      </el-col>

      <!-- 饭卡交易记录管理卡片 -->
      <el-col :span="8">
        <el-card class="management-card" shadow="hover" @click="goToMealCardTransactionManagement">
          <template #header>
            <div class="card-header">
              <el-icon><Document /></el-icon>
              <span>饭卡交易记录管理</span>
            </div>
          </template>
          <div class="card-content">
            <p>管理饭卡交易记录，包括查看、修改、删除交易记录等操作</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Reading, List, Wallet, Document } from '@element-plus/icons-vue'

const router = useRouter()

const goToCourseManagement = () => {
  router.push('/admin/course-management')
}

const goToCourseSelectionManagement = () => {
  router.push('/admin/course-selection-management')
}

const goToMealCardManagement = () => {
  router.push('/admin/meal-card-management')
}

const goToMealCardTransactionManagement = () => {
  router.push('/admin/meal-card-transaction-management')
}
</script>

<style scoped>
.data-management {
  padding: 20px;
}

.page-title {
  margin-bottom: 30px;
  color: #303133;
}

.management-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.3s;
}

.management-card:hover {
  transform: translateY(-5px);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  color: #303133;
}

.card-content {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.el-icon {
  font-size: 24px;
  color: #409EFF;
}
</style> 