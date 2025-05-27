<template>
  <div class="not-found">
    <div class="content">
      <h1>{{ is403 ? '403' : '404' }}</h1>
      <h2>{{ is403 ? '访问被拒绝' : '页面未找到' }}</h2>
      <p>{{ is403 ? '您没有权限访问此页面' : '您访问的页面不存在' }}</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from '@vue/runtime-dom'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const is403 = computed(() => {
  return route.path === '/403'
})

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.not-found {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}

.content {
  text-align: center;
  padding: 40px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

h1 {
  font-size: 72px;
  margin: 0;
  color: #409eff;
}

h2 {
  font-size: 24px;
  margin: 20px 0;
  color: #303133;
}

p {
  font-size: 16px;
  color: #606266;
  margin-bottom: 30px;
}
</style> 