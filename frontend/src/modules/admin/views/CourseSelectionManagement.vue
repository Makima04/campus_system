<template>
  <div class="course-selection-management">
    <h2 class="page-title">选课记录管理</h2>

    <!-- 搜索和操作栏 -->
    <div class="operation-bar">
      <el-input
        v-model="searchQuery"
        placeholder="搜索学生姓名或课程名称"
        class="search-input"
        clearable
        @clear="handleSearch"
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        添加选课记录
      </el-button>
    </div>

    <!-- 选课记录列表 -->
    <el-table :data="filteredSelections" style="width: 100%" v-loading="loading">
      <el-table-column prop="studentName" label="学生姓名" width="120" />
      <el-table-column prop="courseName" label="课程名称" width="200" />
      <el-table-column prop="courseCode" label="课程代码" width="120" />
      <el-table-column prop="teacherName" label="授课教师" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="选课时间" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="200">
        <template #default="scope">
          <el-button type="primary" link @click="showEditDialog(scope.row)">
            编辑
          </el-button>
          <el-button type="danger" link @click="handleDelete(scope.row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/编辑选课记录对话框 -->
    <el-dialog
      :title="dialogType === 'add' ? '添加选课记录' : '编辑选课记录'"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form :model="selectionForm" :rules="rules" ref="selectionFormRef" label-width="100px">
        <el-form-item label="学生" prop="studentId">
          <el-select v-model="selectionForm.studentId" placeholder="请选择学生">
            <el-option
              v-for="student in students"
              :key="student.id"
              :label="student.realName"
              :value="student.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="selectionForm.courseId" placeholder="请选择课程">
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="selectionForm.status" placeholder="请选择状态">
            <el-option label="已选" value="SELECTED" />
            <el-option label="已退选" value="DROPPED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from '@vue/runtime-dom'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import http from '@/utils/http'

interface CourseSelection {
  id: number
  studentId: number
  studentName: string
  courseId: number
  courseName: string
  courseCode: string
  teacherName: string
  status: string
  createdAt: string
}

interface Student {
  id: number
  realName: string
}

interface Course {
  id: number
  name: string
  code: string
}

interface ApiResponse<T> {
  data: T
  code: number
  message: string
}

const loading = ref(false)
const searchQuery = ref('')
const selections = ref<CourseSelection[]>([])
const students = ref<Student[]>([])
const courses = ref<Course[]>([])
const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const selectionFormRef = ref<FormInstance>()

const selectionForm = ref({
  id: 0,
  studentId: 0,
  courseId: 0,
  status: 'SELECTED'
})

const rules = {
  studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const filteredSelections = computed(() => {
  if (!searchQuery.value) return selections.value
  const query = searchQuery.value.toLowerCase()
  return selections.value.filter(
    (selection: CourseSelection) =>
      selection.studentName.toLowerCase().includes(query) ||
      selection.courseName.toLowerCase().includes(query)
  )
})

// 获取选课记录列表
const fetchSelections = async () => {
  loading.value = true
  try {
    const response = await http.get<ApiResponse<CourseSelection[]>>('/course-selection/list')
    selections.value = response.data.data
  } catch (error) {
    console.error('获取选课记录列表失败:', error)
    ElMessage.error('获取选课记录列表失败')
  } finally {
    loading.value = false
  }
}

// 获取学生列表
const fetchStudents = async () => {
  try {
    const response = await http.get<ApiResponse<Student[]>>('/user/students')
    students.value = response.data.data
  } catch (error) {
    console.error('获取学生列表失败:', error)
    ElMessage.error('获取学生列表失败')
  }
}

// 获取课程列表
const fetchCourses = async () => {
  try {
    const response = await http.get<ApiResponse<Course[]>>('/course/list')
    courses.value = response.data.data
  } catch (error) {
    console.error('获取课程列表失败:', error)
    ElMessage.error('获取课程列表失败')
  }
}

// 显示添加对话框
const showAddDialog = () => {
  dialogType.value = 'add'
  selectionForm.value = {
    id: 0,
    studentId: 0,
    courseId: 0,
    status: 'SELECTED'
  }
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (selection: CourseSelection) => {
  dialogType.value = 'edit'
  selectionForm.value = {
    id: selection.id,
    studentId: selection.studentId,
    courseId: selection.courseId,
    status: selection.status
  }
  dialogVisible.value = true
}

// 处理删除
const handleDelete = async (selection: CourseSelection) => {
  try {
    await ElMessageBox.confirm('确定要删除该选课记录吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await http.delete(`/course-selection/${selection.id}`)
    ElMessage.success('删除成功')
    fetchSelections()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除选课记录失败:', error)
      ElMessage.error('删除选课记录失败')
    }
  }
}

// 处理提交
const handleSubmit = async () => {
  if (!selectionFormRef.value) return
  
  await selectionFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (dialogType.value === 'add') {
          await http.post('/course-selection', selectionForm.value)
          ElMessage.success('添加成功')
        } else {
          await http.put(`/course-selection/${selectionForm.value.id}`, selectionForm.value)
          ElMessage.success('更新成功')
        }
        dialogVisible.value = false
        fetchSelections()
      } catch (error) {
        console.error('保存选课记录失败:', error)
        ElMessage.error('保存选课记录失败')
      }
    }
  })
}

// 处理搜索
const handleSearch = () => {
  // 搜索逻辑已通过计算属性实现
}

// 获取状态类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'SELECTED':
      return 'success'
    case 'DROPPED':
      return 'warning'
    case 'CANCELLED':
      return 'danger'
    default:
      return 'info'
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'SELECTED':
      return '已选'
    case 'DROPPED':
      return '已退选'
    case 'CANCELLED':
      return '已取消'
    default:
      return '未知'
  }
}

// 格式化日期
const formatDate = (date: string) => {
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

onMounted(() => {
  fetchSelections()
  fetchStudents()
  fetchCourses()
})
</script>

<style scoped>
.course-selection-management {
  padding: 20px;
}

.page-title {
  margin-bottom: 30px;
  color: #303133;
}

.operation-bar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.search-input {
  width: 300px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 