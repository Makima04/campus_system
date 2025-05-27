<template>
  <div class="schedule-container">
    <div class="schedule-header">
      <h2>我的课表</h2>
      <div class="semester-selector">
        <el-select v-model="selectedSemester" placeholder="选择学期">
          <el-option
            v-for="semester in semesters"
            :key="semester"
            :label="semester"
            :value="semester"
          />
        </el-select>
      </div>
    </div>

    <!-- 课表主体 -->
    <div class="schedule-table">
      <table>
        <thead>
          <tr>
            <th class="time-column">时间</th>
            <th v-for="day in weekDays" :key="day">{{ day }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="timeIndex in 6" :key="timeIndex">
            <td class="time-column">
              <div>第{{ timeIndex * 2 - 1 }}-{{ timeIndex * 2 }}节</div>
              <div class="time-detail">{{ getTimeRange(timeIndex) }}</div>
            </td>
            <td 
              v-for="day in 7" 
              :key="day"
              :class="{ 'has-course': getCourseAt(day, timeIndex) }"
              @click="showCourseDetail(getCourseAt(day, timeIndex))"
            >
              <template v-if="getCourseAt(day, timeIndex)">
                <div class="course-name">{{ getCourseAt(day, timeIndex)?.courseName }}</div>
                <div class="course-info">{{ getCourseAt(day, timeIndex)?.classroom }}</div>
                <div class="course-info">{{ getCourseAt(day, timeIndex)?.teacherName }}</div>
              </template>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 课程详情对话框 -->
    <el-dialog
      v-model="showDetail"
      title="课程详情"
      width="500px"
    >
      <template v-if="selectedCourse">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="课程名称">{{ selectedCourse.courseName }}</el-descriptions-item>
          <el-descriptions-item label="课程代码">{{ selectedCourse.courseCode }}</el-descriptions-item>
          <el-descriptions-item label="任课教师">{{ selectedCourse.teacherName }}</el-descriptions-item>
          <el-descriptions-item label="上课地点">{{ selectedCourse.classroom }}</el-descriptions-item>
          <el-descriptions-item label="上课时间">{{ selectedCourse.classTime }}</el-descriptions-item>
          <el-descriptions-item label="上课周次">{{ selectedCourse.weeks }}</el-descriptions-item>
          <el-descriptions-item label="考勤情况">{{ selectedCourse.attendance || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="成绩">{{ selectedCourse.score || '暂无' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import type { Ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { StudentCourseDTO } from '../types'
import { api } from '../api'

// 状态定义
const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const semesters = ['2023-2024-1', '2023-2024-2']
const selectedSemester: Ref<string> = ref('2023-2024-2')
const courses: Ref<StudentCourseDTO[]> = ref([])
const showDetail: Ref<boolean> = ref(false)
const selectedCourse: Ref<StudentCourseDTO | null> = ref(null)

// 获取当前用户ID
const userId: Ref<number> = ref(1) // 这里应该从用户状态或路由参数中获取

// 获取课表数据
const fetchSchedule = async (): Promise<void> => {
  try {
    const data = await api.getSchedule(userId.value, selectedSemester.value)
    courses.value = data as StudentCourseDTO[]
  } catch (error) {
    console.error('获取课表失败:', error)
    ElMessage.error('获取课表失败')
  }
}

// 根据时间段获取对应的课程
const getCourseAt = (day: number, timeIndex: number): StudentCourseDTO | undefined => {
  return courses.value.find(course => {
    const courseDay = getDayFromClassTime(course.classTime)
    const courseSection = getSectionFromClassTime(course.sections)
    return courseDay === day && courseSection === timeIndex
  })
}

// 从课程时间字符串中提取星期几（1-7）
const getDayFromClassTime = (classTime: string): number => {
  const dayMap: { [key: string]: number } = {
    '一': 1, '二': 2, '三': 3, '四': 4, '五': 5, '六': 6, '日': 7
  }
  const match = classTime.match(/周(.)/)
  return match ? dayMap[match[1]] : 0
}

// 从节次字符串中提取时间段索引（1-6）
const getSectionFromClassTime = (sections: string): number => {
  const match = sections.match(/(\d+)-(\d+)/)
  if (match) {
    const startSection = parseInt(match[1])
    return Math.ceil(startSection / 2)
  }
  return 0
}

// 获取时间范围显示
const getTimeRange = (timeIndex: number): string => {
  const timeRanges = [
    '08:00-09:40',
    '10:00-11:40',
    '14:00-15:40',
    '16:00-17:40',
    '19:00-20:40',
    '20:50-22:30'
  ]
  return timeRanges[timeIndex - 1]
}

// 显示课程详情
const showCourseDetail = (course: StudentCourseDTO | undefined): void => {
  if (course) {
    selectedCourse.value = course
    showDetail.value = true
  }
}

// 监听学期变化
watch(selectedSemester, () => {
  fetchSchedule()
})

// 组件挂载时获取数据
onMounted(() => {
  fetchSchedule()
})
</script>

<style scoped>
.schedule-container {
  padding: 20px;
}

.schedule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.schedule-table {
  width: 100%;
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

th, td {
  border: 1px solid #dcdfe6;
  padding: 8px;
  text-align: center;
  height: 100px;
  vertical-align: top;
}

.time-column {
  width: 100px;
  background-color: #f5f7fa;
}

.time-detail {
  font-size: 12px;
  color: #909399;
}

.has-course {
  background-color: #ecf5ff;
  cursor: pointer;
}

.has-course:hover {
  background-color: #d9ecff;
}

.course-name {
  font-weight: bold;
  margin-bottom: 4px;
}

.course-info {
  font-size: 12px;
  color: #606266;
  margin-bottom: 2px;
}
</style> 