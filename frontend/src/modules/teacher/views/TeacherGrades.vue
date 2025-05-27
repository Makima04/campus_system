<template>
  <div class="teacher-grades">
    <h1>成绩管理</h1>
    
    <div class="grades-header">
      <div class="filter-section">
        <el-select v-model="selectedCourse" placeholder="选择课程" clearable @change="handleCourseChange">
          <el-option 
            v-for="course in courses" 
            :key="course.id" 
            :label="course.name" 
            :value="course.id"
          />
        </el-select>
        
        <el-select v-model="selectedAssignment" placeholder="选择作业/考试" clearable>
          <el-option 
            v-for="assignment in assignments" 
            :key="assignment.id" 
            :label="assignment.title" 
            :value="assignment.id"
          />
        </el-select>
      </div>
      
      <div class="action-buttons">
        <el-button @click="downloadGradeTemplate">下载模板</el-button>
        <el-button type="primary" @click="importGrades">导入成绩</el-button>
        <el-button type="success" @click="exportGrades">导出成绩</el-button>
      </div>
    </div>
    
    <el-table :data="filteredGrades" style="width: 100%" v-loading="loading">
      <el-table-column prop="studentId" label="学号" sortable />
      <el-table-column prop="studentName" label="学生姓名" />
      <el-table-column prop="assignmentTitle" label="作业/考试" />
      <el-table-column prop="score" label="分数" sortable>
        <template #default="scope">
          <span v-if="scope.row.submitted">{{ scope.row.score }}/{{ scope.row.totalScore }}</span>
          <el-tag v-else type="danger">未提交</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="submitTime" label="提交时间" />
      <el-table-column prop="gradeStatus" label="状态">
        <template #default="scope">
          <el-tag :type="getGradeStatusType(scope.row.gradeStatus)">
            {{ getGradeStatusText(scope.row.gradeStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="scope">
          <el-button 
            size="small" 
            type="primary" 
            @click="gradeSubmission(scope.row)"
            :disabled="!scope.row.submitted"
          >
            评分
          </el-button>
          <el-button 
            size="small" 
            @click="viewSubmission(scope.row)"
            :disabled="!scope.row.submitted"
          >
            查看
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="totalRecords"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
// import gradeService from '../api/grade-service'

// 模拟数据
const courses = ref([
  { id: 1, name: '高级数学' },
  { id: 2, name: '计算机科学导论' },
  { id: 3, name: '软件工程实践' }
])

const assignments = ref([
  { id: 1, title: '第一章习题', courseId: 1 },
  { id: 2, title: '期中考试', courseId: 1 },
  { id: 3, title: 'C语言基础编程', courseId: 2 },
  { id: 4, title: '软件需求分析文档', courseId: 3 }
])

const grades = ref([
  {
    id: 1,
    studentId: '202101001',
    studentName: '张三',
    courseId: 1,
    assignmentId: 1,
    assignmentTitle: '第一章习题',
    score: 85,
    totalScore: 100,
    submitted: true,
    submitTime: '2024-04-01 14:30',
    gradeStatus: 'graded'
  },
  {
    id: 2,
    studentId: '202101002',
    studentName: '李四',
    courseId: 1,
    assignmentId: 1,
    assignmentTitle: '第一章习题',
    score: 92,
    totalScore: 100,
    submitted: true,
    submitTime: '2024-04-01 15:45',
    gradeStatus: 'graded'
  },
  {
    id: 3,
    studentId: '202101003',
    studentName: '王五',
    courseId: 1,
    assignmentId: 1,
    assignmentTitle: '第一章习题',
    score: 0,
    totalScore: 100,
    submitted: false,
    submitTime: '-',
    gradeStatus: 'notSubmitted'
  },
  {
    id: 4,
    studentId: '202101001',
    studentName: '张三',
    courseId: 1,
    assignmentId: 2,
    assignmentTitle: '期中考试',
    score: 78,
    totalScore: 100,
    submitted: true,
    submitTime: '2024-04-10 10:30',
    gradeStatus: 'graded'
  },
  {
    id: 5,
    studentId: '202101004',
    studentName: '赵六',
    courseId: 2,
    assignmentId: 3,
    assignmentTitle: 'C语言基础编程',
    score: 0,
    totalScore: 100,
    submitted: true,
    submitTime: '2024-04-05 23:50',
    gradeStatus: 'pending'
  }
])

const selectedCourse = ref('')
const selectedAssignment = ref('')
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalRecords = ref(grades.value.length)

// 根据筛选条件过滤成绩
const filteredGrades = computed(() => {
  let result = [...grades.value]
  
  if (selectedCourse.value) {
    result = result.filter(item => item.courseId === selectedCourse.value)
  }
  
  if (selectedAssignment.value) {
    result = result.filter(item => item.assignmentId === selectedAssignment.value)
  }
  
  return result
})

// 课程选择变化时过滤作业
function handleCourseChange() {
  // 重置作业选择
  selectedAssignment.value = ''
  
  // 实际应用中，这里会调用API获取所选课程的作业
  // 模拟根据课程ID过滤作业
  if (selectedCourse.value) {
    // 更新作业列表
  }
}

// 获取成绩状态标签类型
function getGradeStatusType(status: string) {
  const map: Record<string, string> = {
    graded: 'success',
    pending: 'warning',
    notSubmitted: 'danger'
  }
  return map[status] || 'info'
}

// 获取成绩状态文本
function getGradeStatusText(status: string) {
  const map: Record<string, string> = {
    graded: '已评分',
    pending: '待评分',
    notSubmitted: '未提交'
  }
  return map[status] || status
}

// 页码变化
function handlePageChange(page: number) {
  currentPage.value = page
  // 实际应用中，这里会重新获取数据
  // fetchGrades()
}

// 评分
function gradeSubmission(row: any) {
  console.log('评分作业', row.id)
  // 在实际应用中，这里会打开评分对话框或跳转到评分页面
}

// 查看提交
function viewSubmission(row: any) {
  console.log('查看提交', row.id)
  // 在实际应用中，这里会跳转到提交详情页面
}

// 下载成绩模板
function downloadGradeTemplate() {
  ElMessage.success('成绩模板下载中...')
  // 在实际应用中，这里会调用API下载模板
}

// 导入成绩
function importGrades() {
  ElMessage.info('请选择成绩文件进行导入')
  // 在实际应用中，这里会打开文件选择器
}

// 导出成绩
function exportGrades() {
  ElMessage.success('成绩导出中...')
  // 在实际应用中，这里会调用API导出成绩
}

onMounted(() => {
  // 实际应用中，这里会调用API获取真实数据
  // fetchInitialData()
})
</script>

<style scoped>
.teacher-grades {
  padding: 20px;
}

.grades-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-section {
  display: flex;
  gap: 15px;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

h1 {
  margin-bottom: 25px;
  color: #333;
}
</style> 