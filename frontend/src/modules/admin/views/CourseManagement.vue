<template>
  <div class="course-management">
    <h2 class="page-title">课程管理</h2>

    <!-- 搜索和操作栏 -->
    <div class="operation-bar">
      <div class="search-group">
        <el-input
          v-model="searchQuery"
          placeholder="搜索课程名称或编号"
          class="search-input"
          clearable
          @clear="handleSearch"
          @input="handleSearchInput"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select 
          v-model="filterDepartment" 
          placeholder="选择开课院系" 
          clearable 
          @change="handleSearch"
          :value="filterDepartment"
        >
          <el-option
            v-for="dept in departments"
            :key="dept.id"
            :label="dept.name"
            :value="dept.id"
          />
        </el-select>
      </div>

      <div class="action-group">
        <el-upload
          action="#"
          :auto-upload="false"
          :show-file-list="false"
          accept=".xlsx,.xls"
          :on-change="handleFileChange"
        >
          <el-button type="success">
            <el-icon><Upload /></el-icon>
            批量导入
          </el-button>
        </el-upload>
        <el-button type="primary" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出模板
        </el-button>
        <el-button type="warning" @click="handleExportCourses">
          <el-icon><Document /></el-icon>
          导出数据
        </el-button>
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon>
          添加课程
        </el-button>
      </div>
    </div>

    <!-- 课程列表 -->
    <el-table :data="filteredCourses" style="width: 100%" v-loading="loading">
      <el-table-column prop="courseCode" label="课程编号" width="120" />
      <el-table-column prop="courseName" label="课程名称" width="200" />
      <el-table-column prop="departmentName" label="开课院系" width="180" />
      <el-table-column prop="teacherName" label="授课教师" width="120" />
      <el-table-column prop="credits" label="学分" width="80" />
      <el-table-column prop="courseType" label="课程类型" width="120">
        <template #default="scope">
          <el-tag :type="getCourseTypeTag(scope.row.courseType)">
            {{ getCourseTypeLabel(scope.row.courseType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="semester" label="学期" width="120" />
      <el-table-column prop="capacity" label="容量" width="100">
        <template #default="scope">
          {{ scope.row.selectedCount }}/{{ scope.row.capacity }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="200">
        <template #default="scope">
          <el-button type="primary" link @click="showEditDialog(scope.row)">
            编辑
          </el-button>
          <el-button type="primary" link @click="showDetailsDialog(scope.row)">
            详情
          </el-button>
          <el-button type="danger" link @click="handleDelete(scope.row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
      <template #empty>
        <div class="empty-data">
          <el-icon><Folder /></el-icon>
          <p>暂无课程数据</p>
          <el-button type="primary" @click="showAddDialog">添加课程</el-button>
        </div>
      </template>
    </el-table>

    <!-- 分页控件 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="totalCount"
        :page-sizes="[10, 20, 50, 100]"
        :background="true"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 添加/编辑课程对话框 -->
    <el-dialog
      :title="dialogType === 'add' ? '添加课程' : '编辑课程'"
      v-model="dialogVisible"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="courseFormRef"
        :model="courseForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="课程编号" prop="courseCode">
          <el-input v-model="courseForm.courseCode" placeholder="请输入课程编号" />
        </el-form-item>
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="courseForm.courseName" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="开课院系" prop="departmentId">
          <el-select v-model="courseForm.departmentId" placeholder="请选择开课院系" style="width: 100%">
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="授课教师" prop="teacherId">
          <el-select 
            v-model="courseForm.teacherId" 
            placeholder="请选择授课教师" 
            style="width: 100%"
            filterable
            remote
            :remote-method="searchTeachers"
            :loading="teacherLoading"
          >
            <el-option
              v-for="teacher in teachers"
              :key="teacher.id"
              :label="teacher.realName"
              :value="teacher.id"
            >
              <span>{{ teacher.realName }}</span>
              <span style="color: #8492a6; font-size: 13px">({{ teacher.department }})</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="学分" prop="credits">
          <el-input-number v-model="courseForm.credits" :min="0.5" :max="10" :step="0.5" />
          <span class="form-tip">学分必须是0.5的倍数，范围在0.5-10之间</span>
        </el-form-item>
        <el-form-item label="课程类型" prop="courseType">
          <el-select v-model="courseForm.courseType" placeholder="请选择课程类型" style="width: 100%">
            <el-option label="必修课" value="REQUIRED">
              <el-tag type="danger" size="small">必修课</el-tag>
            </el-option>
            <el-option label="选修课" value="ELECTIVE">
              <el-tag type="success" size="small">选修课</el-tag>
            </el-option>
            <el-option label="公共课" value="PUBLIC">
              <el-tag type="warning" size="small">公共课</el-tag>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-select v-model="courseForm.semester" placeholder="请选择学期" style="width: 100%">
            <el-option
              v-for="semester in semesters"
              :key="semester"
              :label="semester"
              :value="semester"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number v-model="courseForm.capacity" :min="1" :max="500" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="courseForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="未开始" value="NOT_STARTED">
              <el-tag type="info" size="small">未开始</el-tag>
            </el-option>
            <el-option label="进行中" value="IN_PROGRESS">
              <el-tag type="success" size="small">进行中</el-tag>
            </el-option>
            <el-option label="已结束" value="ENDED">
              <el-tag type="warning" size="small">已结束</el-tag>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="课程简介" prop="description">
          <el-input
            v-model="courseForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入课程简介"
          />
        </el-form-item>
        <el-form-item label="上课时间" prop="classTime">
          <el-select v-model="courseForm.classTime" placeholder="请选择上课时间" style="width: 100%">
            <el-option label="周一" value="MONDAY" />
            <el-option label="周二" value="TUESDAY" />
            <el-option label="周三" value="WEDNESDAY" />
            <el-option label="周四" value="THURSDAY" />
            <el-option label="周五" value="FRIDAY" />
            <el-option label="周六" value="SATURDAY" />
            <el-option label="周日" value="SUNDAY" />
          </el-select>
        </el-form-item>
        <el-form-item label="上课地点" prop="classroom">
          <el-input v-model="courseForm.classroom" placeholder="请输入上课地点" />
        </el-form-item>
        <el-form-item label="上课周次" prop="weeks">
          <el-input v-model="courseForm.weeks" placeholder="请输入上课周次，例如：1-8,10-16" />
        </el-form-item>
        <el-form-item label="上课节次" prop="sections">
          <el-input v-model="courseForm.sections" placeholder="请输入上课节次，例如：1-2,3-4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleReset">重置</el-button>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 课程详情对话框 -->
    <el-dialog
      title="课程详情"
      v-model="detailsDialogVisible"
      width="600px"
    >
      <el-descriptions :column="2" border v-if="currentCourse">
        <el-descriptions-item label="课程编号">{{ currentCourse.courseCode }}</el-descriptions-item>
        <el-descriptions-item label="课程名称">{{ currentCourse.courseName }}</el-descriptions-item>
        <el-descriptions-item label="开课院系">{{ currentCourse.departmentName }}</el-descriptions-item>
        <el-descriptions-item label="授课教师">{{ currentCourse.teacherName }}</el-descriptions-item>
        <el-descriptions-item label="学分">{{ currentCourse.credits }}</el-descriptions-item>
        <el-descriptions-item label="课程类型">
          <el-tag :type="getCourseTypeTag(currentCourse.courseType)">
            {{ getCourseTypeLabel(currentCourse.courseType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="学期">{{ currentCourse.semester }}</el-descriptions-item>
        <el-descriptions-item label="容量">{{ currentCourse.selectedCount }}/{{ currentCourse.capacity }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentCourse.status)">
            {{ getStatusLabel(currentCourse.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="上课时间">{{ currentCourse.classTime }}</el-descriptions-item>
        <el-descriptions-item label="上课地点">{{ currentCourse.classroom }}</el-descriptions-item>
        <el-descriptions-item label="上课周次">{{ currentCourse.weeks }}</el-descriptions-item>
        <el-descriptions-item label="上课节次">{{ currentCourse.sections }}</el-descriptions-item>
        <el-descriptions-item label="课程简介" :span="2">{{ currentCourse.description }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailsDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from '@vue/runtime-dom'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Folder, Upload, Download, Document } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
// 使用真实API服务
import courseService from '../api/course-service'
// import mockCourseService from '../api/mock-course-service'
import {
  Course, CourseForm, Department, Teacher,
  courseTypeTagMap, courseTypeLabelMap,
  courseStatusTagMap, courseStatusLabelMap
} from '../types/course'
import { courseRules } from '@/tool/rules-generator'
import { resetForm, useFormSubmit, extractFormFields } from '@/tool/form-helper'

// 使用真实服务
// const courseService = mockCourseService;

// 响应式数据
const loading = ref(false)
const teacherLoading = ref(false)
const searchQuery = ref('')
const filterDepartment = ref<number | null>(null)
const courses = ref<Course[]>([])
const departments = ref<Department[]>([])
const teachers = ref<Teacher[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const totalCount = ref(0)
const dialogVisible = ref(false)
const detailsDialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const courseFormRef = ref<FormInstance>()
const currentCourse = ref<Course | null>(null)
const submitLoading = ref(false)

// 生成最近4年的学期选项
const currentYear = new Date().getFullYear()
const semesters = computed(() => {
  const result: string[] = []
  for (let year = currentYear; year >= currentYear - 3; year--) {
    result.push(`${year}-${year + 1}-1`)
    result.push(`${year}-${year + 1}-2`)
  }
  return result
})

// 表单数据
const courseForm = ref<CourseForm>({
  courseCode: '',
  courseName: '',
  departmentId: 0,
  teacherId: 0,
  credits: 2,
  courseType: 'REQUIRED',
  semester: `${currentYear}-${currentYear + 1}-1`,
  capacity: 50,
  status: 'NOT_STARTED',
  description: '',
  classTime: '',
  classroom: '',
  weeks: '',
  sections: ''
})

// 使用预配置的课程表单验证规则
const rules = courseRules

// 过滤后的课程列表
const filteredCourses = computed(() => {
  if (!searchQuery.value && !filterDepartment.value) return courses.value
  
  return courses.value.filter(course => {
    const matchesSearch = !searchQuery.value || 
      course.courseName.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      course.courseCode.toLowerCase().includes(searchQuery.value.toLowerCase())
    
    const matchesDepartment = !filterDepartment.value || 
      course.departmentId === filterDepartment.value
    
    return matchesSearch && matchesDepartment
  })
})

// 监听搜索查询变化，实现延迟搜索
watch(searchQuery, (newVal) => {
  if (newVal === '') {
    handleSearch();
  }
}, { flush: 'sync' })

// 定义延迟搜索函数
const debouncedSearch = (() => {
  let timer: number | null = null;
  return (value: string) => {
    if (timer) clearTimeout(timer);
    timer = window.setTimeout(() => {
      handleSearch();
      timer = null;
    }, 300);
  };
})();

// 处理输入变化
const handleSearchInput = (value: string) => {
  debouncedSearch(value);
};

// 获取课程列表
const fetchCourses = async () => {
  try {
    loading.value = true
    console.log('请求课程数据', {
      page: currentPage.value - 1,
      size: pageSize.value,
      keyword: searchQuery.value,
      departmentId: filterDepartment.value
    });

    const response = await courseService.getCourses({
      page: currentPage.value - 1, // 后端从0开始计算页码
      size: pageSize.value,
      keyword: searchQuery.value,
      departmentId: filterDepartment.value
    })
    
    console.log('课程数据响应', response);
    
    if (response.data) {
      courses.value = response.data.content || [];
      totalCount.value = response.data.totalElements || 0;
      console.log('解析后的课程数据', courses.value);
    } else {
      console.error('响应中没有数据字段', response);
      courses.value = [];
      totalCount.value = 0;
    }
  } catch (error: any) {
    console.error('获取课程列表失败:', error)
    ElMessage.error('获取课程列表失败: ' + (error.message || '未知错误'))
    courses.value = [];
    totalCount.value = 0;
  } finally {
    loading.value = false
  }
}

// 获取院系列表
const fetchDepartments = async () => {
  try {
    // 检查用户是否已登录
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');
    
    if (!token) {
      ElMessage.warning('请先登录');
      window.location.href = '/login';
      return;
    }
    
    if (role !== 'admin' && role !== 'ROLE_ADMIN') {
      ElMessage.error('您没有权限访问此功能');
      return;
    }
    
    const response = await courseService.getDepartments()
    console.log('院系数据响应:', response)
    
    if (response && response.data) {
      // 处理标准响应格式 { code, message, data }
      if (response.data.data && Array.isArray(response.data.data)) {
        departments.value = response.data.data.map((dept: any) => ({
          id: dept.id,
          name: dept.name,
          code: dept.code
        }))
      } else if (Array.isArray(response.data)) {
        // 处理直接返回数组的情况
        departments.value = response.data.map((dept: any) => ({
          id: dept.id,
          name: dept.name,
          code: dept.code
        }))
      } else if (response.data.content && Array.isArray(response.data.content)) {
        // 处理分页响应的情况
        departments.value = response.data.content.map((dept: any) => ({
          id: dept.id,
          name: dept.name,
          code: dept.code
        }))
      } else {
        console.error('院系数据格式不正确:', response.data)
        departments.value = []
        ElMessage.warning('院系数据格式不正确')
      }
    } else {
      departments.value = []
      ElMessage.warning('未获取到院系数据')
    }
  } catch (error: any) {
    console.error('获取院系列表失败:', error)
    if (error.response?.status === 403) {
      ElMessage.error('您没有权限访问此功能')
    } else if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      window.location.href = '/login'
    } else {
      ElMessage.error('获取院系列表失败: ' + (error.message || '未知错误'))
    }
    departments.value = [] // 确保departments数组不为undefined
  }
}

// 搜索教师
const searchTeachers = async (query: string) => {
  if (query.length < 2) return
  
  try {
    teacherLoading.value = true
    const response = await courseService.searchTeachers(query)
    if (response.data) {
      teachers.value = response.data
    }
  } catch (error) {
    console.error('搜索教师失败:', error)
    ElMessage.error('搜索教师失败')
  } finally {
    teacherLoading.value = false
  }
}

// 初始化表单数据
const initFormData = () => {
  return {
    courseCode: '',
    courseName: '',
    departmentId: departments.value.length > 0 ? departments.value[0].id : 0,
    teacherId: 0,
    credits: 2,
    courseType: 'REQUIRED',
    semester: `${currentYear}-${currentYear + 1}-1`,
    capacity: 50,
    status: 'NOT_STARTED',
    description: '',
    classTime: '',
    classroom: '',
    weeks: '',
    sections: ''
  }
}

// 显示添加对话框
const showAddDialog = () => {
  dialogType.value = 'add'
  courseForm.value = initFormData()
  nextTick(() => {
    if (courseFormRef.value) {
      courseFormRef.value.clearValidate()
    }
  })
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (course: Course) => {
  dialogType.value = 'edit'
  courseForm.value = extractFormFields(course, [
    'id', 'courseCode', 'courseName', 'departmentId', 'teacherId', 
    'credits', 'courseType', 'semester', 'capacity', 'status', 'description',
    'classTime', 'classroom', 'weeks', 'sections'
  ]) as CourseForm
  
  // 添加当前教师到选项列表
  if (course.teacherId && course.teacherName) {
    teachers.value = [{
      id: course.teacherId,
      realName: course.teacherName,
      department: course.departmentName
    }]
  }
  
  nextTick(() => {
    if (courseFormRef.value) {
      courseFormRef.value.clearValidate()
    }
  })
  
  dialogVisible.value = true
}

// 显示详情对话框
const showDetailsDialog = (course: Course) => {
  currentCourse.value = course
  detailsDialogVisible.value = true
}

// 处理删除
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该课程吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    loading.value = true
    await courseService.deleteCourse(id)
    ElMessage.success('删除成功')
    await fetchCourses()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除课程失败:', error)
      ElMessage.error('删除课程失败')
    }
  } finally {
    loading.value = false
  }
}

// 处理表单提交
const handleSubmit = async () => {
  if (!courseFormRef.value) return
  
  await courseFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        submitLoading.value = true
        
        if (dialogType.value === 'add') {
          await courseService.createCourse(courseForm.value)
          ElMessage.success('添加课程成功')
        } else {
          await courseService.updateCourse(courseForm.value.id!, courseForm.value)
          ElMessage.success('更新课程成功')
        }
        
        dialogVisible.value = false
        await fetchCourses()
      } catch (error: any) {
        console.error('提交失败:', error)
        ElMessage.error('提交失败: ' + (error.message || '未知错误'))
      } finally {
        submitLoading.value = false
      }
    } else {
      ElMessage.warning('请完善表单信息')
    }
  })
}

// 重置表单
const handleReset = () => {
  resetForm(courseFormRef.value, courseForm.value, dialogType.value === 'add' ? initFormData() : undefined)
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchCourses()
}

// 处理页码变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchCourses()
}

// 处理每页数量变化
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  fetchCourses()
}

// 获取课程类型标签样式
const getCourseTypeTag = (type: string): 'success' | 'warning' | 'danger' | 'info' => {
  return courseTypeTagMap[type] as 'success' | 'warning' | 'danger' | 'info' || 'info'
}

// 获取课程类型标签文本
const getCourseTypeLabel = (type: string): string => {
  return courseTypeLabelMap[type] || type
}

// 获取状态标签样式
const getStatusType = (status: string): 'success' | 'warning' | 'danger' | 'info' => {
  return courseStatusTagMap[status] as 'success' | 'warning' | 'danger' | 'info' || 'info'
}

// 获取状态标签文本
const getStatusLabel = (status: string): string => {
  return courseStatusLabelMap[status] || status
}

// 处理文件变化
const handleFileChange = (file: any) => {
  if (!file) return;
  
  const formData = new FormData();
  formData.append('file', file.raw);
  
  ElMessageBox.confirm(
    '确定要导入课程数据吗？导入操作会根据课程编号进行新增或更新。',
    '导入确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      loading.value = true;
      
      // 调用实际的批量导入API
      await courseService.importCourses(formData);
      
      ElMessage.success('课程数据导入成功');
      await fetchCourses();
    } catch (error: any) {
      console.error('导入失败:', error);
      ElMessage.error('导入失败: ' + (error.message || '未知错误'));
    } finally {
      loading.value = false;
    }
  }).catch(() => {
    // 用户取消导入
  });
};

// 处理导出模板
const handleExport = () => {
  // 使用完整URL在新窗口中打开导出模板
  window.open(courseService.getExportTemplateUrl(), '_blank');
};

// 处理导出课程数据
const handleExportCourses = () => {
  // 收集当前的筛选条件
  const params = {
    keyword: searchQuery.value,
    departmentId: filterDepartment.value
  };
  
  // 打开导出链接
  window.open(courseService.exportCourses(params), '_blank');
};

// 页面加载时获取数据
onMounted(async () => {
  await Promise.all([
    fetchDepartments(),
    fetchCourses()
  ])
})
</script>

<style scoped>
.course-management {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: bold;
}

.operation-bar {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-group {
  display: flex;
  gap: 10px;
}

.action-group {
  display: flex;
  gap: 10px;
}

.search-input {
  width: 300px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

.empty-data {
  text-align: center;
  padding: 40px 0;
  color: #909399;
}

.empty-data .el-icon {
  font-size: 48px;
  margin-bottom: 20px;
}
</style> 