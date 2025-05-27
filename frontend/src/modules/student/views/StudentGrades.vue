<template>
  <div class="student-grades">
    <div class="page-header">
      <h2>我的成绩</h2>
      
      <div class="filter-options">
        <el-select v-model="selectedSemester" placeholder="选择学期" clearable @change="filterGrades">
          <el-option
            v-for="item in semesters"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        
        <el-select v-model="selectedCourseType" placeholder="课程类型" clearable @change="filterGrades">
          <el-option label="必修" value="必修" />
          <el-option label="选修" value="选修" />
          <el-option label="公共课" value="公共课" />
        </el-select>
        
        <el-button type="primary" @click="printGrades">打印成绩单</el-button>
      </div>
    </div>
    
    <div class="statistics-cards">
      <el-card class="stat-card">
        <template #header>
          <div class="stat-header">
            <i class="el-icon-data-line"></i>
            <span>平均成绩</span>
          </div>
        </template>
        <div class="stat-value">{{ getAverageScore().toFixed(2) }}</div>
      </el-card>
      
      <el-card class="stat-card">
        <template #header>
          <div class="stat-header">
            <i class="el-icon-medal"></i>
            <span>已修学分</span>
          </div>
        </template>
        <div class="stat-value">{{ getTotalCredits() }}</div>
      </el-card>
      
      <el-card class="stat-card">
        <template #header>
          <div class="stat-header">
            <i class="el-icon-collection-tag"></i>
            <span>课程数量</span>
          </div>
        </template>
        <div class="stat-value">{{ filteredGrades.length }}</div>
      </el-card>
      
      <el-card class="stat-card">
        <template #header>
          <div class="stat-header">
            <i class="el-icon-trophy"></i>
            <span>通过率</span>
          </div>
        </template>
        <div class="stat-value">{{ getPassRate() }}%</div>
      </el-card>
    </div>
    
    <el-card class="grades-table-card" id="printableArea">
      <template #header>
        <div class="table-header">
          <span class="header-title">成绩单</span>
          <span class="student-info" v-if="studentInfo">
            学号: {{ studentInfo.studentId }} | 姓名: {{ studentInfo.name }}
          </span>
        </div>
      </template>
      
      <el-table 
        :data="filteredGrades" 
        border 
        style="width: 100%"
        :default-sort="{ prop: 'semester', order: 'descending' }"
      >
        <el-table-column prop="semester" label="学期" sortable width="180" />
        <el-table-column prop="courseCode" label="课程代码" width="120" />
        <el-table-column prop="courseName" label="课程名称" width="180" />
        <el-table-column prop="courseType" label="课程类型" width="120" />
        <el-table-column prop="credits" label="学分" width="80" />
        <el-table-column label="成绩" sortable width="100">
          <template #default="scope">
            <span :class="getScoreClass(scope.row.score)">{{ scope.row.score }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="attendance" label="出勤率" width="100">
          <template #default="scope">
            {{ scope.row.attendance }}%
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="teacherName" label="授课教师" width="120" />
        <el-table-column label="操作" fixed="right" width="120">
          <template #default="scope">
            <el-button 
              type="text" 
              size="small" 
              @click="showCourseDetail(scope.row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <el-dialog
      v-model="dialogVisible"
      :title="selectedCourse.courseName"
      width="600px"
    >
      <div class="course-details">
        <div class="detail-section">
          <h3>课程信息</h3>
          <div class="detail-item">
            <span class="label">课程代码:</span>
            <span>{{ selectedCourse.courseCode }}</span>
          </div>
          <div class="detail-item">
            <span class="label">课程类型:</span>
            <span>{{ selectedCourse.courseType }}</span>
          </div>
          <div class="detail-item">
            <span class="label">学分:</span>
            <span>{{ selectedCourse.credits }}</span>
          </div>
          <div class="detail-item">
            <span class="label">教师:</span>
            <span>{{ selectedCourse.teacherName }}</span>
          </div>
          <div class="detail-item">
            <span class="label">学期:</span>
            <span>{{ selectedCourse.semester }}</span>
          </div>
        </div>
        
        <div class="detail-section">
          <h3>成绩详情</h3>
          <div class="score-card">
            <el-progress 
              type="circle" 
              :percentage="selectedCourse.score" 
              :color="getScoreColor(selectedCourse.score)"
              :status="getProgressStatus(selectedCourse.score)"
            />
            <div class="score-label">成绩</div>
          </div>
          
          <div class="detail-item">
            <span class="label">出勤率:</span>
            <el-progress 
              :percentage="selectedCourse.attendance" 
              :color="getAttendanceColor(selectedCourse.attendance)"
            />
          </div>
          
          <div class="detail-item">
            <span class="label">状态:</span>
            <el-tag :type="getStatusType(selectedCourse.status)">
              {{ selectedCourse.status }}
            </el-tag>
          </div>
        </div>
        
        <div class="detail-section" v-if="selectedCourse.comment">
          <h3>教师评语</h3>
          <div class="comment">{{ selectedCourse.comment }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

interface ApiResponse<T> {
  data: T;
  code?: number;
  message?: string;
}

interface StudentInfo {
  studentId: string;
  name: string;
  department: string;
}

interface Grade {
  id: number;
  courseCode: string;
  courseName: string;
  courseType: string;
  credits: number;
  score: number;
  attendance: number;
  status: string;
  teacherName: string;
  semester: string;
  comment?: string;
}

const studentInfo = ref<StudentInfo | null>(null)
const loading = ref(false)

// 学期数据
const semesters = [
  { label: '2023-2024学年第一学期', value: '2023-1' },
  { label: '2023-2024学年第二学期', value: '2023-2' },
  { label: '2024-2025学年第一学期', value: '2024-1' }
]

// 筛选条件
const selectedSemester = ref('')
const selectedCourseType = ref('')

// 成绩数据
const allGrades = ref<Grade[]>([])
const filteredGrades = ref<Grade[]>([])

// 课程详情弹窗
const dialogVisible = ref(false)
const selectedCourse = ref<Grade>({} as Grade)

// 获取成绩数据
const fetchGrades = async () => {
  try {
    loading.value = true
    
    // 获取学生信息
    const profileResponse = await axios.get<ApiResponse<StudentInfo>>('/api/student/profile')
    if (profileResponse.data?.data) {
      studentInfo.value = profileResponse.data.data
    }
    
    // 获取成绩信息
    const gradesResponse = await axios.get<ApiResponse<Grade[]>>('/api/student/grades')
    if (gradesResponse.data?.data) {
      allGrades.value = gradesResponse.data.data
      filterGrades()
    }
  } catch (error) {
    console.error('获取成绩数据失败:', error)
    ElMessage.error('获取成绩数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 筛选成绩
const filterGrades = () => {
  filteredGrades.value = allGrades.value.filter(grade => {
    const semesterMatch = !selectedSemester.value || grade.semester === selectedSemester.value
    const typeMatch = !selectedCourseType.value || grade.courseType === selectedCourseType.value
    return semesterMatch && typeMatch
  })
}

// 计算平均分
const getAverageScore = () => {
  if (filteredGrades.value.length === 0) return 0
  
  const totalScore = filteredGrades.value.reduce((sum, grade) => sum + grade.score, 0)
  return totalScore / filteredGrades.value.length
}

// 计算总学分
const getTotalCredits = () => {
  return filteredGrades.value.reduce((sum, grade) => {
    // 只计算及格课程的学分
    if (grade.status === '通过' || grade.status === '优秀') {
      return sum + grade.credits
    }
    return sum
  }, 0)
}

// 计算通过率
const getPassRate = () => {
  if (filteredGrades.value.length === 0) return 0
  
  const passedCourses = filteredGrades.value.filter(
    grade => grade.status === '通过' || grade.status === '优秀'
  ).length
  
  return Math.round((passedCourses / filteredGrades.value.length) * 100)
}

// 获取成绩的CSS类
const getScoreClass = (score: number) => {
  if (score >= 90) return 'score-excellent'
  if (score >= 80) return 'score-good'
  if (score >= 60) return 'score-pass'
  return 'score-fail'
}

// 获取状态的类型
const getStatusType = (status: string) => {
  switch (status) {
    case '优秀': return 'success'
    case '通过': return 'primary'
    case '不通过': return 'danger'
    case '待评分': return 'info'
    default: return 'info'
  }
}

// 获取成绩的颜色
const getScoreColor = (score: number) => {
  if (score >= 90) return '#67c23a'
  if (score >= 80) return '#409eff'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}

// 获取进度条状态
const getProgressStatus = (score: number) => {
  if (score >= 90) return 'success'
  if (score >= 60) return ''
  return 'exception'
}

// 获取出勤率的颜色
const getAttendanceColor = (attendance: number) => {
  if (attendance >= 90) return '#67c23a'
  if (attendance >= 80) return '#409eff'
  if (attendance >= 60) return '#e6a23c'
  return '#f56c6c'
}

// 显示课程详情
const showCourseDetail = (course: Grade) => {
  selectedCourse.value = course
  dialogVisible.value = true
}

// 打印成绩单
const printGrades = () => {
  const printArea = document.getElementById('printableArea')
  if (!printArea) return
  
  const printContent = printArea.innerHTML
  const originalContent = document.body.innerHTML
  
  document.body.innerHTML = `
    <div style="padding: 20px;">
      <h1 style="text-align: center;">成绩单</h1>
      ${printContent}
    </div>
  `
  
  window.print()
  document.body.innerHTML = originalContent
  
  // 重新绑定事件
  setTimeout(() => {
    location.reload()
  }, 100)
}

onMounted(() => {
  // 调用API获取实际数据
  fetchGrades()
})
</script>

<style scoped>
.student-grades {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
}

.filter-options {
  display: flex;
  gap: 16px;
}

.statistics-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.stat-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  margin-top: 8px;
  color: #409EFF;
}

.grades-table-card {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 16px;
  font-weight: bold;
}

.student-info {
  font-size: 14px;
  color: #666;
}

.score-excellent {
  color: #67c23a;
  font-weight: bold;
}

.score-good {
  color: #409eff;
  font-weight: bold;
}

.score-pass {
  color: #e6a23c;
}

.score-fail {
  color: #f56c6c;
  font-weight: bold;
}

.course-details {
  padding: 20px;
}

.detail-section {
  margin-bottom: 30px;
}

.detail-section h3 {
  margin-top: 0;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.detail-item {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}

.label {
  font-weight: bold;
  width: 80px;
  margin-right: 12px;
}

.score-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}

.score-label {
  margin-top: 8px;
  font-size: 14px;
  color: #666;
}

.comment {
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  color: #666;
  line-height: 1.6;
}

@media print {
  .filter-options,
  .el-table__fixed-right {
    display: none !important;
  }
  
  .statistics-cards {
    display: none;
  }
}
</style> 