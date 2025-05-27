<template>
  <div class="course-selection">
    <div class="page-header">
      <h2>网上选课</h2>
      <div class="filter-options">
        <el-select v-model="selectedSemester" placeholder="选择学期" clearable @change="fetchCourses">
          <el-option
            v-for="item in semesters"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        
        <el-select v-model="selectedCategory" placeholder="课程类别" clearable @change="filterCourses">
          <el-option label="必修课" value="必修课" />
          <el-option label="选修课" value="选修课" />
          <el-option label="公共课" value="公共课" />
        </el-select>
        
        <el-select v-model="selectedDepartment" placeholder="开课院系" clearable @change="filterCourses">
          <el-option
            v-for="item in departments"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
        
        <el-input
          v-model="searchKeyword"
          placeholder="搜索课程"
          clearable
          @input="filterCourses"
        >
          <template #prefix>
            <i class="el-icon-search"></i>
          </template>
        </el-input>
      </div>
    </div>
    
    <div class="selection-status">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="status-card">
            <template #header>
              <div class="card-header">
                <span>已选学分</span>
              </div>
            </template>
            <div class="card-content">
              <span class="status-number">{{ getSelectedCredits() }}</span>
              <span class="status-label">/ {{ creditLimit }} 学分</span>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="status-card">
            <template #header>
              <div class="card-header">
                <span>已选课程</span>
              </div>
            </template>
            <div class="card-content">
              <span class="status-number">{{ selectedCourses.length }}</span>
              <span class="status-label">门</span>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="status-card">
            <template #header>
              <div class="card-header">
                <span>选课阶段</span>
              </div>
            </template>
            <div class="card-content">
              <el-tag :type="getSelectionPhaseType()">{{ selectionPhase }}</el-tag>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="可选课程" name="available">
        <el-table 
          :data="filteredCourses" 
          border 
          style="width: 100%"
          v-loading="loading"
        >
          <el-table-column prop="courseCode" label="课程代码" width="120" />
          <el-table-column prop="courseName" label="课程名称" width="180" />
          <el-table-column prop="credits" label="学分" width="80" />
          <el-table-column prop="category" label="类别" width="100" />
          <el-table-column prop="department" label="开课院系" width="150" />
          <el-table-column prop="teacherName" label="授课教师" width="120" />
          <el-table-column prop="schedule" label="上课时间/地点" min-width="180" />
          <el-table-column prop="capacity" label="容量" width="100">
            <template #default="scope">
              {{ scope.row.enrolled }}/{{ scope.row.capacity }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getCourseStatusType(scope.row)">
                {{ getCourseStatus(scope.row) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="150">
            <template #default="scope">
              <el-button 
                type="primary" 
                size="small" 
                :disabled="!canSelectCourse(scope.row)"
                @click="selectCourse(scope.row)"
              >
                选课
              </el-button>
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
        
        <div class="pagination-container">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="totalCourses"
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="handlePageChange"
          />
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="已选课程" name="selected">
        <el-table 
          :data="selectedCourses" 
          border 
          style="width: 100%"
        >
          <el-table-column prop="courseCode" label="课程代码" width="120" />
          <el-table-column prop="courseName" label="课程名称" width="180" />
          <el-table-column prop="credits" label="学分" width="80" />
          <el-table-column prop="category" label="类别" width="100" />
          <el-table-column prop="teacherName" label="授课教师" width="120" />
          <el-table-column prop="schedule" label="上课时间/地点" min-width="180" />
          <el-table-column prop="selectTime" label="选课时间" width="180" />
          <el-table-column label="操作" fixed="right" width="120">
            <template #default="scope">
              <el-button 
                type="danger" 
                size="small" 
                :disabled="!canDropCourse()"
                @click="dropCourse(scope.row)"
              >
                退课
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="no-data" v-if="selectedCourses.length === 0">
          <el-empty description="您还没有选择任何课程" />
        </div>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 课程详情弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="selectedCourseDetail.courseName" 
      width="700px"
    >
      <div class="course-detail">
        <el-descriptions title="基本信息" :column="3" border>
          <el-descriptions-item label="课程代码">{{ selectedCourseDetail.courseCode }}</el-descriptions-item>
          <el-descriptions-item label="学分">{{ selectedCourseDetail.credits }}</el-descriptions-item>
          <el-descriptions-item label="课程类别">{{ selectedCourseDetail.category }}</el-descriptions-item>
          <el-descriptions-item label="授课教师">{{ selectedCourseDetail.teacherName }}</el-descriptions-item>
          <el-descriptions-item label="开课院系">{{ selectedCourseDetail.department }}</el-descriptions-item>
          <el-descriptions-item label="容量">{{ selectedCourseDetail.enrolled }}/{{ selectedCourseDetail.capacity }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider />
        
        <h3>课程安排</h3>
        <el-table :data="selectedCourseDetail.scheduleDetails || []" border style="width: 100%; margin-bottom: 20px;">
          <el-table-column prop="day" label="星期" width="100" />
          <el-table-column prop="time" label="时间" width="180" />
          <el-table-column prop="location" label="地点" width="150" />
          <el-table-column prop="weekRange" label="周次" />
        </el-table>
        
        <el-divider />
        
        <h3>课程描述</h3>
        <div class="course-description">
          {{ selectedCourseDetail.description || '暂无课程描述' }}
        </div>
        
        <el-divider />
        
        <h3>先修课程</h3>
        <div v-if="selectedCourseDetail.prerequisites && selectedCourseDetail.prerequisites.length > 0">
          <el-tag 
            v-for="prerequisite in selectedCourseDetail.prerequisites" 
            :key="prerequisite.courseCode" 
            style="margin-right: 10px; margin-bottom: 10px;"
          >
            {{ prerequisite.courseName }}
          </el-tag>
        </div>
        <div v-else class="no-data-text">
          无先修课程要求
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button 
            type="primary" 
            :disabled="!canSelectCourse(selectedCourseDetail)"
            @click="selectAndClose(selectedCourseDetail)"
          >
            选课
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api/index'

// API响应接口
interface ApiResponse<T> {
  success: boolean;
  courses?: T[];
  message?: string;
}

// 课程接口定义
interface Course {
  id: number;
  courseCode: string;
  courseName: string;
  credits: number;
  category: string;
  department: string;
  teacherName: string;
  schedule: string;
  enrolled: number;
  capacity: number;
  description?: string;
  prerequisites?: Array<{courseCode: string, courseName: string}>;
  scheduleDetails?: Array<{day: string, time: string, location: string, weekRange: string}>;
  selectTime?: string; // 添加选课时间属性
}

const store = useStore()
const userId = computed(() => store.state.user.userInfo?.id)

// 学期和筛选选项
const semesters = [
  { label: '2023-2024学年第一学期', value: '2023-1' },
  { label: '2023-2024学年第二学期', value: '2023-2' },
  { label: '2024-2025学年第一学期', value: '2024-1' }
]
const selectedSemester = ref('2023-2')
const selectedCategory = ref('')
const selectedDepartment = ref('')
const searchKeyword = ref('')
const departments = ref<string[]>([])

// 选课状态
const selectionPhase = ref('第一轮选课')
const creditLimit = ref(30)

// 选项卡
const activeTab = ref('available')

// 课程数据
const loading = ref(false)
const allCourses = ref<Course[]>([])
const filteredCourses = ref<Course[]>([])
const selectedCourses = ref<Course[]>([])
const selectedCourseDetail = ref<Course>({} as Course)
const dialogVisible = ref(false)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const totalCourses = computed(() => filteredCourses.value.length)

// 获取课程列表
const fetchCourses = async () => {
  if (!selectedSemester.value) return
  
  loading.value = true
  try {
    const response = await api.getAvailableCourses(
      Number(userId.value), 
      selectedSemester.value, 
      currentPage.value, 
      pageSize.value
    ) as ApiResponse<Course>
    allCourses.value = response.courses || []
    
    // 提取所有开课院系
    const deptSet = new Set<string>()
    allCourses.value.forEach(course => {
      if (course.department) {
        deptSet.add(course.department)
      }
    })
    departments.value = [...deptSet]
    
    filterCourses()
  } catch (error) {
    console.error('获取课程列表失败:', error)
    ElMessage.error('获取课程列表失败')
  } finally {
    loading.value = false
  }
}

// 获取已选课程
const fetchSelectedCourses = async () => {
  if (!userId.value || !selectedSemester.value) return
  
  try {
    const response = await api.getSelectedCourses(
      Number(userId.value), 
      selectedSemester.value
    ) as ApiResponse<Course>
    selectedCourses.value = response.courses || []
  } catch (error) {
    console.error('获取已选课程失败:', error)
    ElMessage.error('获取已选课程失败')
  }
}

// 筛选课程
const filterCourses = () => {
  let filtered = [...allCourses.value]
  
  if (selectedCategory.value) {
    filtered = filtered.filter(course => course.category === selectedCategory.value)
  }
  
  if (selectedDepartment.value) {
    filtered = filtered.filter(course => course.department === selectedDepartment.value)
  }
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(course => 
      course.courseName.toLowerCase().includes(keyword) || 
      course.courseCode.toLowerCase().includes(keyword) ||
      course.teacherName.toLowerCase().includes(keyword)
    )
  }
  
  filteredCourses.value = filtered
  currentPage.value = 1
}

// 选课
const selectCourse = async (course: Course) => {
  if (!userId.value) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    const response = await api.enrollCourse(
      Number(userId.value), 
      course.id
    ) as ApiResponse<Course>
    
    if (response.success) {
      ElMessage.success(`成功选择课程: ${course.courseName}`)
      
      // 更新课程状态
      course.enrolled++
      
      // 添加到已选课程列表
      const now = new Date()
      selectedCourses.value.push({
        ...course,
        selectTime: now.toLocaleString()
      })
    }
  } catch (error: unknown) {
    console.error('选课失败:', error)
    const typedError = error as { response?: { data?: string } }
    ElMessage.error(typedError.response?.data || '选课失败')
  }
}

// 退课
const dropCourse = async (course: Course) => {
  ElMessageBox.confirm(`确定要退选课程 ${course.courseName} 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await api.dropCourse(
        Number(userId.value), 
        course.id
      ) as ApiResponse<Course>
      
      if (response.success) {
        ElMessage.success(`成功退选课程: ${course.courseName}`)
        
        // 从已选课程中移除
        selectedCourses.value = selectedCourses.value.filter(c => c.id !== course.id)
        
        // 更新可选课程中的已选人数
        const availableCourse = allCourses.value.find(c => c.id === course.id)
        if (availableCourse) {
          availableCourse.enrolled--
        }
      }
    } catch (error: unknown) {
      console.error('退课失败:', error)
      const typedError = error as { response?: { data?: string } }
      ElMessage.error(typedError.response?.data || '退课失败')
    }
  }).catch(() => {})
}

// 显示课程详情
const showCourseDetail = (course: Course) => {
  selectedCourseDetail.value = course
  dialogVisible.value = true
}

// 在详情页选课并关闭弹窗
const selectAndClose = (course: Course) => {
  selectCourse(course)
  dialogVisible.value = false
}

// 获取已选学分
const getSelectedCredits = () => {
  return selectedCourses.value.reduce((sum, course) => sum + course.credits, 0)
}

// 处理标签页点击
const handleTabClick = () => {
  if (activeTab.value === 'selected') {
    fetchSelectedCourses()
  }
}

// 处理分页变化
const handlePageChange = (page: number) => {
  currentPage.value = page
}

// 获取课程状态
const getCourseStatus = (course: Course) => {
  if (isAlreadySelected(course.id)) {
    return '已选'
  }
  if (course.enrolled >= course.capacity) {
    return '已满'
  }
  return '可选'
}

// 获取课程状态样式类型
const getCourseStatusType = (course: Course) => {
  if (isAlreadySelected(course.id)) {
    return 'success'
  }
  if (course.enrolled >= course.capacity) {
    return 'danger'
  }
  return 'primary'
}

// 判断是否可以选课
const canSelectCourse = (course: Course) => {
  if (!course) return false
  
  // 已选择
  if (isAlreadySelected(course.id)) {
    return false
  }
  
  // 课程已满
  if (course.enrolled >= course.capacity) {
    return false
  }
  
  // 学分限制
  const currentCredits = getSelectedCredits()
  if (currentCredits + course.credits > creditLimit.value) {
    return false
  }
  
  // 时间冲突
  if (hasTimeConflict(course)) {
    return false
  }
  
  return true
}

// 判断是否已选该课程
const isAlreadySelected = (courseId: number) => {
  return selectedCourses.value.some(course => course.id === courseId)
}

// 判断是否有时间冲突
const hasTimeConflict = (newCourse: Course) => {
  // 这里需要实现更复杂的时间冲突检测逻辑
  // 简化版本：如果新课程与已选课程在相同的时间段有重叠就返回true
  console.log("检查时间冲突:", newCourse.courseCode)
  return false
}

// 判断是否可以退课
const canDropCourse = () => {
  // 根据选课阶段判断是否可退课
  return true
}

// 获取选课阶段类型
const getSelectionPhaseType = () => {
  switch(selectionPhase.value) {
    case '第一轮选课': return 'success'
    case '第二轮选课': return 'primary'
    case '补选阶段': return 'warning'
    case '已结束': return 'info'
    default: return 'info'
  }
}

onMounted(() => {
  // 在实际环境中调用获取数据API
  fetchCourses()
  fetchSelectedCourses()
})
</script>

<style scoped>
.course-selection {
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
  align-items: center;
}

.selection-status {
  margin-bottom: 20px;
}

.status-card {
  text-align: center;
}

.card-content {
  display: flex;
  justify-content: center;
  align-items: baseline;
}

.status-number {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  margin-right: 8px;
}

.status-label {
  font-size: 16px;
  color: #666;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.course-detail h3 {
  margin-top: 20px;
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: bold;
}

.course-description {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  line-height: 1.6;
}

.no-data {
  padding: 40px 0;
}

.no-data-text {
  color: #909399;
  text-align: center;
  margin: 20px 0;
}
</style> 