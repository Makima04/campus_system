<template>
  <div class="teacher-course">
    <h1>课程管理</h1>
    
    <div class="course-list">
      <div v-for="course in courses" :key="course.id" class="course-card">
        <div class="course-header">
          <h2>{{ course.name }}</h2>
          <span class="course-code">{{ course.code }}</span>
        </div>
        <div class="course-info">
          <p><strong>时间:</strong> {{ course.schedule }}</p>
          <p><strong>地点:</strong> {{ course.location }}</p>
          <p><strong>学生数:</strong> {{ course.studentCount }}</p>
        </div>
        <div class="course-actions">
          <button @click="viewCourseDetails(course.id)" class="btn primary">查看详情</button>
          <button @click="manageCourseStudents(course.id)" class="btn secondary">管理学生</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
// import courseService from '../api/course-service'

// 模拟数据
const courses = ref([
  {
    id: 1,
    name: '高级数学',
    code: 'MATH101',
    schedule: '周一, 周三 10:00-11:30',
    location: '理学院 301',
    studentCount: 45
  },
  {
    id: 2,
    name: '计算机科学导论',
    code: 'CS101',
    schedule: '周二, 周四 14:00-15:30',
    location: '信息楼 205',
    studentCount: 38
  },
  {
    id: 3,
    name: '软件工程实践',
    code: 'SE301',
    schedule: '周五 9:00-12:00',
    location: '实验楼 502',
    studentCount: 32
  }
])

onMounted(() => {
  // 实际应用中，这里会调用API获取真实数据
  // fetchCourses()
})

// 查看课程详情
function viewCourseDetails(courseId: number) {
  console.log('查看课程详情', courseId)
  // 跳转到课程详情页或打开详情对话框
}

// 管理课程学生
function manageCourseStudents(courseId: number) {
  console.log('管理课程学生', courseId)
  // 跳转到学生管理页面
}

// 获取课程数据
// async function fetchCourses() {
//   try {
//     const response = await courseService.getCourses()
//     courses.value = response.data
//   } catch (error) {
//     console.error('获取课程数据失败', error)
//   }
// }
</script>

<style scoped>
.teacher-course {
  padding: 20px;
}

.course-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.course-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
  transition: transform 0.2s;
}

.course-card:hover {
  transform: translateY(-5px);
}

.course-header {
  border-bottom: 1px solid #eee;
  margin-bottom: 15px;
  padding-bottom: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.course-code {
  background-color: #f0f0f0;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.9rem;
}

.course-info p {
  margin: 8px 0;
}

.course-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
}

.primary {
  background-color: #1867c0;
  color: white;
}

.secondary {
  background-color: #f0f0f0;
  color: #333;
}

h1 {
  color: #333;
  margin-bottom: 20px;
}

h2 {
  color: #555;
  font-size: 1.2rem;
  margin: 0;
}
</style> 