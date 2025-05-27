<template>
  <div class="teacher-dashboard">
    <h1>教师仪表盘</h1>
    <div class="dashboard-content">
      <div class="dashboard-card">
        <h2>课程概览</h2>
        <p>当前正在教授的课程数量: {{ activeCourses.length }}</p>
        <ul class="course-list">
          <li v-for="course in activeCourses" :key="course.id">
            {{ course.name }}
          </li>
        </ul>
      </div>
      
      <div class="dashboard-card">
        <h2>近期任务</h2>
        <ul class="task-list">
          <li v-for="task in recentTasks" :key="task.id">
            {{ task.title }} - {{ task.dueDate }}
          </li>
        </ul>
      </div>
      
      <div class="dashboard-card">
        <h2>学生统计</h2>
        <p>总学生数: {{ totalStudents }}</p>
        <p>需要关注的学生: {{ studentsNeedingAttention }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
// 假设我们有一个教师服务来获取数据
// import teacherService from '../api/teacher-service'

// 模拟数据
const activeCourses = ref([
  { id: 1, name: '高级数学' },
  { id: 2, name: '计算机科学导论' },
  { id: 3, name: '软件工程实践' }
])

const recentTasks = ref([
  { id: 1, title: '布置期中考试', dueDate: '2024-04-15' },
  { id: 2, title: '评阅学生作业', dueDate: '2024-04-10' },
  { id: 3, title: '准备下周课程内容', dueDate: '2024-04-12' }
])

const totalStudents = ref(120)
const studentsNeedingAttention = ref(5)

onMounted(() => {
  // 实际应用中，这里会调用API获取真实数据
  // fetchDashboardData()
})

// 获取仪表盘数据的函数
// async function fetchDashboardData() {
//   try {
//     const response = await teacherService.getDashboardData()
//     activeCourses.value = response.data.activeCourses
//     recentTasks.value = response.data.recentTasks
//     totalStudents.value = response.data.totalStudents
//     studentsNeedingAttention.value = response.data.studentsNeedingAttention
//   } catch (error) {
//     console.error('获取仪表盘数据失败', error)
//   }
// }
</script>

<style scoped>
.teacher-dashboard {
  padding: 20px;
}

.dashboard-content {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.dashboard-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

h1 {
  color: #333;
  margin-bottom: 20px;
}

h2 {
  color: #555;
  font-size: 1.2rem;
  margin-bottom: 15px;
}

.course-list, .task-list {
  list-style: none;
  padding: 0;
}

.course-list li, .task-list li {
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.course-list li:last-child, .task-list li:last-child {
  border-bottom: none;
}
</style> 