<template>
  <div class="teacher-assignments">
    <h1>作业管理</h1>
    
    <div class="assignments-header">
      <div class="filter-section">
        <el-select v-model="selectedCourse" placeholder="选择课程" clearable>
          <el-option 
            v-for="course in courses" 
            :key="course.id" 
            :label="course.name" 
            :value="course.id" 
          />
        </el-select>
        
        <el-select v-model="assignmentStatus" placeholder="作业状态" clearable>
          <el-option label="待发布" value="pending" />
          <el-option label="已发布" value="published" />
          <el-option label="已截止" value="closed" />
        </el-select>
      </div>
      
      <el-button type="primary" @click="openNewAssignmentDialog">
        新建作业
      </el-button>
    </div>
    
    <el-table :data="filteredAssignments" style="width: 100%" v-loading="loading">
      <el-table-column prop="title" label="作业标题" />
      <el-table-column prop="courseName" label="所属课程" />
      <el-table-column prop="dueDate" label="截止日期" />
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="submissionCount" label="提交数/总数" />
      <el-table-column label="操作" width="220">
        <template #default="scope">
          <el-button size="small" @click="viewAssignmentDetails(scope.row.id)">
            查看
          </el-button>
          <el-button 
            size="small" 
            type="primary" 
            @click="editAssignment(scope.row.id)"
            :disabled="scope.row.status === 'closed'"
          >
            编辑
          </el-button>
          <el-button 
            size="small" 
            type="danger" 
            @click="deleteAssignment(scope.row.id)"
            :disabled="scope.row.status === 'published' && scope.row.submissionCount > 0"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="totalAssignments"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
// import assignmentService from '../api/assignment-service'

// 模拟数据
const courses = ref([
  { id: 1, name: '高级数学' },
  { id: 2, name: '计算机科学导论' },
  { id: 3, name: '软件工程实践' }
])

const assignments = ref([
  {
    id: 1,
    title: '第一章习题',
    courseId: 1,
    courseName: '高级数学',
    dueDate: '2024-04-20',
    status: 'published',
    submissionCount: '30/45'
  },
  {
    id: 2,
    title: 'C语言基础编程',
    courseId: 2,
    courseName: '计算机科学导论',
    dueDate: '2024-04-15',
    status: 'published',
    submissionCount: '25/38'
  },
  {
    id: 3,
    title: '软件需求分析文档',
    courseId: 3,
    courseName: '软件工程实践',
    dueDate: '2024-04-30',
    status: 'pending',
    submissionCount: '0/32'
  },
  {
    id: 4,
    title: '微积分期中作业',
    courseId: 1,
    courseName: '高级数学',
    dueDate: '2024-04-10',
    status: 'closed',
    submissionCount: '42/45'
  }
])

const selectedCourse = ref('')
const assignmentStatus = ref('')
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalAssignments = ref(assignments.value.length)

// 根据筛选条件过滤作业
const filteredAssignments = computed(() => {
  let result = [...assignments.value]
  
  if (selectedCourse.value) {
    result = result.filter(item => item.courseId === selectedCourse.value)
  }
  
  if (assignmentStatus.value) {
    result = result.filter(item => item.status === assignmentStatus.value)
  }
  
  return result
})

// 获取状态标签类型
function getStatusType(status: string) {
  const map: Record<string, string> = {
    pending: 'info',
    published: 'success',
    closed: 'danger'
  }
  return map[status] || 'info'
}

// 获取状态文本
function getStatusText(status: string) {
  const map: Record<string, string> = {
    pending: '待发布',
    published: '已发布',
    closed: '已截止'
  }
  return map[status] || status
}

// 页码变化
function handlePageChange(page: number) {
  currentPage.value = page
  // 实际应用中，这里会重新获取数据
  // fetchAssignments()
}

// 查看作业详情
function viewAssignmentDetails(id: number) {
  console.log('查看作业详情', id)
}

// 编辑作业
function editAssignment(id: number) {
  console.log('编辑作业', id)
}

// 删除作业
function deleteAssignment(id: number) {
  ElMessageBox.confirm('确定要删除这个作业吗？此操作无法撤销', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 实际应用中，这里会调用API删除作业
    // 模拟删除
    assignments.value = assignments.value.filter(item => item.id !== id)
    ElMessage.success('删除成功')
  }).catch(() => {
    // 用户取消删除
  })
}

// 打开新建作业对话框
function openNewAssignmentDialog() {
  console.log('打开新建作业对话框')
}

onMounted(() => {
  // 实际应用中，这里会调用API获取真实数据
  // fetchAssignments()
})
</script>

<style scoped>
.teacher-assignments {
  padding: 20px;
}

.assignments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-section {
  display: flex;
  gap: 15px;
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