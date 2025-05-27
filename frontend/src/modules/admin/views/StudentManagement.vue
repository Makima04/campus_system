<template>
  <div class="student-management">
    <h2 class="page-title">学生管理</h2>

    <!-- 搜索和操作栏 -->
    <div class="operation-bar">
      <el-input
        v-model="searchQuery"
        placeholder="搜索学生姓名或学号"
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
        添加学生
      </el-button>
    </div>

    <!-- 学生列表 -->
    <el-table :data="filteredStudents" style="width: 100%" v-loading="loading">
      <el-table-column prop="studentId" label="学号" width="120" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column prop="departmentName" label="院系" width="150" />
      <el-table-column prop="majorName" label="专业" width="150" />
      <el-table-column prop="className" label="班级" width="120" />
      <el-table-column prop="enrollYear" label="入学年份" width="100" />
      <el-table-column prop="phone" label="联系电话" width="150" />
      <el-table-column prop="email" label="邮箱" width="180" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ translateStatus(scope.row.status) }}
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

    <!-- 添加/编辑学生对话框 -->
    <el-dialog
      :title="dialogType === 'add' ? '添加学生' : '编辑学生'"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form
        ref="studentFormRef"
        :model="studentForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="学号" prop="studentId">
          <el-input v-model="studentForm.studentId" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="studentForm.realName" />
        </el-form-item>
        <el-form-item label="院系" prop="departmentId">
          <el-select 
            v-model="studentForm.departmentId" 
            placeholder="请选择院系" 
            style="width: 100%"
            @change="handleDepartmentChange"
            filterable
          >
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
            <el-option
              v-if="studentForm.departmentId && studentForm.departmentName && !hasDepartmentIdInList(studentForm.departmentId)"
              :label="studentForm.departmentName"
              :value="studentForm.departmentId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="专业" prop="majorId">
          <el-select 
            v-model="studentForm.majorId" 
            placeholder="请选择专业" 
            :disabled="!studentForm.departmentId"
            style="width: 100%"
            @change="handleMajorChange"
            filterable
          >
            <el-option
              v-for="major in majors"
              :key="major.id"
              :label="major.name"
              :value="major.id"
            />
            <el-option
              v-if="studentForm.majorId && studentForm.majorName && !hasMajorIdInList(studentForm.majorId)"
              :label="studentForm.majorName"
              :value="studentForm.majorId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="班级" prop="classId">
          <el-select 
            v-model="studentForm.classId" 
            placeholder="请选择班级"
            :disabled="!studentForm.majorId"
            style="width: 100%"
            @change="handleClassChange"
            filterable
          >
            <el-option
              v-for="cls in classes"
              :key="cls.id"
              :label="cls.name"
              :value="cls.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="入学年份" prop="enrollYear">
          <el-input-number v-model="studentForm.enrollYear" :min="2000" :max="2100" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="studentForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="studentForm.email" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="studentForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="在读" value="在读" />
            <el-option label="休学" value="休学" />
            <el-option label="退学" value="退学" />
            <el-option label="毕业" value="毕业" />
            <el-option label="在读" value="ACTIVE" v-if="false" />
            <el-option label="休学" value="SUSPENDED" v-if="false" />
            <el-option label="退学" value="WITHDRAWN" v-if="false" />
            <el-option label="毕业" value="GRADUATED" v-if="false" />
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

    <!-- 学生详情对话框 -->
    <el-dialog
      title="学生详情"
      v-model="detailsDialogVisible"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学号">{{ currentStudent?.studentId }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentStudent?.realName }}</el-descriptions-item>
        <el-descriptions-item label="院系">{{ currentStudent?.departmentName }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ currentStudent?.majorName }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ currentStudent?.className }}</el-descriptions-item>
        <el-descriptions-item label="入学年份">{{ currentStudent?.enrollYear }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentStudent?.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentStudent?.email }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentStudent?.status || '')">
            {{ translateStatus(currentStudent?.status || '') }}
          </el-tag>
        </el-descriptions-item>
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
import { ref, computed, onMounted, watch } from '@vue/runtime-dom'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { get, post, put, del } from '@/utils/http'
import { escapeHtml } from '@/utils/security'
import { getApiBaseUrl } from '@/config'

interface Student {
  id: number
  studentId: string
  realName: string
  departmentId: number
  departmentName: string
  majorId: number
  majorName: string
  classId: number
  className: string
  enrollYear: number
  phone: string
  email: string
  status: string
}

interface Department {
  id: number
  name: string
  code: string
  description?: string
}

interface Major {
  id: number
  name: string
  code: string
  description?: string
  departmentId: number
}

interface Class {
  id: number
  name: string
  code: string
  grade: number
  majorId: number
}

interface StudentForm {
  id?: number
  studentId: string
  realName: string
  departmentId: number
  departmentName: string
  majorId: number
  majorName: string
  classId: number
  className: string
  enrollYear: number
  phone: string
  email: string
  status: string
}

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

const loading = ref(false)
const searchQuery = ref('')
const students = ref<Student[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const totalCount = ref(0)
const dialogVisible = ref(false)
const detailsDialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const studentFormRef = ref<FormInstance>()
const currentStudent = ref<Student | null>(null)

const departments = ref<Department[]>([])
const majors = ref<Major[]>([])
const classes = ref<Class[]>([])

const studentForm = ref<StudentForm>({
  studentId: '',
  realName: '',
  departmentId: 0,
  departmentName: '',
  majorId: 0,
  majorName: '',
  classId: 0,
  className: '',
  enrollYear: new Date().getFullYear(),
  phone: '',
  email: '',
  status: '在读'
})

const rules = {
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { min: 10, max: 10, message: '学号长度必须为10位', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在2-20之间', trigger: 'blur' }
  ],
  departmentId: [
    { required: true, message: '请选择院系', trigger: 'change' }
  ],
  majorId: [
    { required: true, message: '请选择专业', trigger: 'change' }
  ],
  classId: [
    { required: true, message: '请选择班级', trigger: 'change' }
  ],
  enrollYear: [
    { required: true, message: '请输入入学年份', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

const filteredStudents = computed(() => {
  if (!searchQuery.value) return students.value
  const query = searchQuery.value.toLowerCase()
  return students.value.filter(
    (student: Student) =>
      student.realName.toLowerCase().includes(query) ||
      student.studentId.toLowerCase().includes(query)
  )
})

// 添加API端点常量
const API_ENDPOINTS = {
  getStudents: '/api/student/list',
  getStudent: (id: number) => `/api/student/${id}`,
  createStudent: '/api/student',
  updateStudent: (id: number) => `/api/student/${id}`,
  deleteStudent: (id: number) => `/api/student/${id}`,
  getDepartments: `/api/department/list`,
  getMajors: `/api/major/list`,
  getClasses: `/api/class/list`
};

// 检查管理员权限
const checkAdminPermission = () => {
  const token = localStorage.getItem('token');
  const role = localStorage.getItem('role')?.toLowerCase();
  
  console.log('权限检查:', {
    token: token ? '存在' : '不存在',
    role: role
  });
  
  if (!token) {
    ElMessage.error('未登录或登录已过期，请重新登录');
    return false;
  }
  
  // 接受多种形式的admin角色标识
  const validAdminRoles = ['admin', 'role_admin', 'admin_role', 'administrator'];
  if (!role || !validAdminRoles.some(r => role.includes(r))) {
    ElMessage.error('只有管理员可以访问此页面');
    return false;
  }
  
  return true;
};

// 处理权限错误
const handlePermissionError = () => {
  ElMessage.error('您没有权限执行此操作，请确认您是否已正确登录为管理员');
};

// 数据适配器 - 尝试从各种后端响应格式中提取数据数组
const extractDataArray = <T>(response: any, entityName: string): T[] => {
  console.log(`尝试为${entityName}提取数据数组，原始响应:`, response);
  
  try {
    // 情况1: 直接是数组
    if (Array.isArray(response)) {
      console.log(`${entityName}响应直接是数组，长度:`, response.length);
      return response.filter((item: any) => item && (item.id !== undefined));
    }
    
    // 情况2: Axios响应包装 + 后端自定义响应结构
    // response.data.data.data格式 (Axios + 后端标准响应 + 数据数组)
    if (response && response.data && typeof response.data === 'object') {
      console.log(`${entityName}响应中的data属性:`, response.data);
      
      // 检查嵌套的后端响应结构
      if (response.data.code === 200 && response.data.data) {
        if (Array.isArray(response.data.data)) {
          console.log(`找到后端标准响应格式中的data数组，长度:`, response.data.data.length);
          return response.data.data.filter((item: any) => item && (item.id !== undefined));
        } else if (response.data.data.content && Array.isArray(response.data.data.content)) {
          // 处理分页数据
          console.log(`找到后端分页响应格式中的content数组，长度:`, response.data.data.content.length);
          return response.data.data.content.filter((item: any) => item && (item.id !== undefined));
        }
      }
      
      // 直接检查response.data (Axios包装的数据体)
      if (Array.isArray(response.data)) {
        console.log(`${entityName}响应的data属性直接是数组，长度:`, response.data.length);
        return response.data.filter((item: any) => item && (item.id !== undefined));
      }
    }
    
    // 情况3: 标准REST响应格式，检查常见数据字段名
    if (response && typeof response === 'object') {
      const commonDataFields = ['content', 'list', 'items', 'rows', 'records', entityName.toLowerCase() + 's'];
      
      for (const field of commonDataFields) {
        if (response[field] && Array.isArray(response[field])) {
          console.log(`${entityName}响应中找到 ${field} 数组字段，长度:`, response[field].length);
          return response[field].filter((item: any) => item && (item.id !== undefined));
        }
        
        // 检查response.data中的字段
        if (response.data && response.data[field] && Array.isArray(response.data[field])) {
          console.log(`${entityName}响应的data属性中找到 ${field} 数组字段，长度:`, response.data[field].length);
          return response.data[field].filter((item: any) => item && (item.id !== undefined));
        }
      }
      
      // 情况4: Spring Data分页响应
      if (response.content && Array.isArray(response.content)) {
        console.log(`${entityName}响应是Spring Data分页格式，数据长度:`, response.content.length);
        return response.content.filter((item: any) => item && (item.id !== undefined));
      }
      
      // 检查response.data中的分页格式
      if (response.data && response.data.content && Array.isArray(response.data.content)) {
        console.log(`${entityName}响应的data属性是Spring Data分页格式，数据长度:`, response.data.content.length);
        return response.data.content.filter((item: any) => item && (item.id !== undefined));
      }
      
      // 情况5: 自定义分页响应
      if (response.pageData && Array.isArray(response.pageData.content)) {
        console.log(`${entityName}响应是自定义分页格式，数据长度:`, response.pageData.content.length);
        return response.pageData.content.filter((item: any) => item && (item.id !== undefined));
      }
      
      // 情况6: 查找任何嵌套的数组
      const arrayProps = Object.keys(response).filter(key => Array.isArray(response[key]));
      if (arrayProps.length > 0) {
        // 使用第一个找到的数组
        const firstArrayProp = arrayProps[0];
        console.log(`${entityName}响应中找到数组属性 ${firstArrayProp}，长度:`, response[firstArrayProp].length);
        return response[firstArrayProp].filter((item: any) => item && (item.id !== undefined));
      }
      
      // 检查response.data中的任何数组属性
      if (response.data && typeof response.data === 'object') {
        const dataArrayProps = Object.keys(response.data).filter(key => Array.isArray(response.data[key]));
        if (dataArrayProps.length > 0) {
          // 使用第一个找到的数组
          const firstDataArrayProp = dataArrayProps[0];
          console.log(`${entityName}响应的data属性中找到数组 ${firstDataArrayProp}，长度:`, response.data[firstDataArrayProp].length);
          return response.data[firstDataArrayProp].filter((item: any) => item && (item.id !== undefined));
        }
      }
      
      // 输出所有顶级属性，帮助调试
      console.log(`${entityName}响应是对象，但未找到数组。所有顶级属性:`, Object.keys(response));
      if (response.data && typeof response.data === 'object') {
        console.log(`${entityName}响应的data属性中的所有顶级属性:`, Object.keys(response.data));
      }
    }
    
    // 没有找到任何可用的数据数组
    console.warn(`无法从${entityName}响应中提取数据数组`);
    return [];
  } catch (error) {
    console.error(`解析${entityName}数据时出错:`, error);
    return [];
  }
};

// 直接使用StudentController端点获取学生列表
const fetchStudentsFromController = async () => {
  if (!checkAdminPermission()) {
    return;
  }

  loading.value = true;
  try {
    console.log('直接从StudentController获取学生列表');
    // 使用正确的Spring Data JPA分页参数
    // StudentController.list方法接受page和size参数，page从1开始（不是0）
    const response = await get('/api/student/list', {
      page: currentPage.value, // 后端页码从1开始
      size: pageSize.value,
      sort: 'id,desc' // 确保按ID降序排序
    });

    // 获取响应数据
    const responseData = response && response.data;
    console.log('StudentController响应:', responseData);

    // 处理不同格式的响应
    if (responseData && responseData.content && Array.isArray(responseData.content)) {
      students.value = responseData.content;
      totalCount.value = responseData.totalElements || responseData.content.length;
      console.log('成功获取学生列表:', students.value);
    } else if (Array.isArray(responseData)) {
      students.value = responseData;
      totalCount.value = responseData.length;
      console.log('成功获取学生列表(数组格式):', students.value);
    } else if (responseData && responseData.data && Array.isArray(responseData.data)) {
      students.value = responseData.data;
      totalCount.value = responseData.total || responseData.data.length;
      console.log('成功获取学生列表(带data字段):', students.value);
    } else {
      console.warn('无法识别的响应格式:', responseData);
      if (responseData && typeof responseData === 'object') {
        // 尝试查找任何数组属性
        const arrays = Object.values(responseData).filter(Array.isArray);
        if (arrays.length > 0) {
          students.value = arrays[0];
          totalCount.value = arrays[0].length;
          console.log('找到疑似学生列表的数组:', students.value);
        } else {
          students.value = [];
          totalCount.value = 0;
          console.error('响应中没有找到任何数组');
          loading.value = false;
          return false;
        }
      } else {
        students.value = [];
        totalCount.value = 0;
        console.error('响应不是对象或数组');
        loading.value = false;
        return false;
      }
    }
    
    // 设置正确的API端点
    API_ENDPOINTS.getStudents = '/api/student/list';
    API_ENDPOINTS.getStudent = (id: number) => `/api/student/${id}`;
    API_ENDPOINTS.createStudent = '/api/student';
    API_ENDPOINTS.updateStudent = (id: number) => `/api/student/${id}`;
    API_ENDPOINTS.deleteStudent = (id: number) => `/api/student/${id}`;
    
    loading.value = false;
    return true;
  } catch (error) {
    console.error('直接获取学生列表失败:', error);
    loading.value = false;
    return false;
  }
};

// 获取学生列表
const fetchStudents = async () => {
  if (!checkAdminPermission()) {
    return false;
  }

  // 定义可能的API路径列表，首先尝试正确的路径
  const possibleApiEndpoints = [
    { name: "学生列表", url: `/api/student/list`, method: 'get' },
    { name: "学生API", url: `/api/student`, method: 'get' },
    { name: "管理员API", url: `/api/admin/student`, method: 'get' },
    { name: "管理员学生", url: `/api/admin/students`, method: 'get' },
  ];
  
  loading.value = true;
  
  // 依次尝试每个API路径
  for (const endpoint of possibleApiEndpoints) {
    try {
      console.log(`尝试获取学生列表: ${endpoint.name} - ${endpoint.url} (${endpoint.method})`);
      
      // 重要：这里使用Spring Data JPA分页参数格式（page从1开始）
      const queryParams: Record<string, any> = {
        page: currentPage.value, // 后端页码从1开始
        size: pageSize.value
      };
      
      if (searchQuery.value) {
        queryParams['query'] = escapeHtml(searchQuery.value);
      }
      
      let response;
      if (endpoint.method === 'get') {
        response = await get(endpoint.url, queryParams);
      } else {
        response = await post(endpoint.url, queryParams);
      }
      
      // 获取响应数据
      const responseData = response && response.data;
      console.log(`API路径 ${endpoint.name} 成功:`, responseData);
      
      // 更新API端点以供后续使用
      API_ENDPOINTS.getStudents = endpoint.url;
      
      // 根据成功的端点设置其他相关端点
      if (endpoint.url.includes('student/list')) {
        API_ENDPOINTS.getStudent = (id: number) => `/api/student/${id}`;
        API_ENDPOINTS.createStudent = `/api/student`;
        API_ENDPOINTS.updateStudent = (id: number) => `/api/student/${id}`;
        API_ENDPOINTS.deleteStudent = (id: number) => `/api/student/${id}`;
      } else if (endpoint.url.includes('admin/students')) {
        API_ENDPOINTS.getStudent = (id: number) => `/api/admin/students/${id}`;
        API_ENDPOINTS.createStudent = `/api/admin/students`;
        API_ENDPOINTS.updateStudent = (id: number) => `/api/admin/students/${id}`;
        API_ENDPOINTS.deleteStudent = (id: number) => `/api/admin/students/${id}`;
      } else {
        API_ENDPOINTS.getStudent = (id: number) => `${endpoint.url}/${id}`;
        API_ENDPOINTS.createStudent = endpoint.url;
        API_ENDPOINTS.updateStudent = (id: number) => `${endpoint.url}/${id}`;
        API_ENDPOINTS.deleteStudent = (id: number) => `${endpoint.url}/${id}`;
      }
      
      console.log('成功设置API端点:', API_ENDPOINTS);
      
      // 处理不同格式的响应
      if (Array.isArray(responseData)) {
        students.value = responseData;
        totalCount.value = responseData.length;
      } else if (responseData && responseData.content && Array.isArray(responseData.content)) {
        students.value = responseData.content;
        totalCount.value = responseData.totalElements || 0;
      } else if (responseData && responseData.data && Array.isArray(responseData.data)) {
        // 处理带有data字段的响应
        students.value = responseData.data;
        totalCount.value = responseData.total || responseData.data.length;
      } else if (responseData && typeof responseData === 'object') {
        // 尝试解析任何返回的对象
        const possibleArrays = Object.values(responseData).filter(Array.isArray);
        if (possibleArrays.length > 0) {
          // 使用找到的第一个数组
          students.value = possibleArrays[0];
          totalCount.value = possibleArrays[0].length;
          console.log('使用响应中找到的数组:', possibleArrays[0]);
        } else {
          console.error('无法识别的学生数据格式:', responseData);
          ElMessage.warning('学生数据格式不正确');
          students.value = [];
          totalCount.value = 0;
        }
      } else {
        console.error('无法识别的学生数据格式:', responseData);
        ElMessage.warning('学生数据格式不正确');
        students.value = [];
        totalCount.value = 0;
      }
      
      // 如果成功获取数据，就退出循环
      loading.value = false;
      return true;
    } catch (error: any) {
      console.error(`API路径 ${endpoint.name} 失败:`, error);
      console.error(`错误详情:`, {
        status: error.response?.status,
        statusText: error.response?.statusText,
        data: error.response?.data,
        url: endpoint.url,
        method: endpoint.method
      });
      
      // 继续尝试下一个API路径
    }
  }
  
  loading.value = false;
  ElMessage.error('无法连接到后端API，请检查网络连接或联系管理员');
  return false;
};

// 获取院系列表
const fetchDepartments = async () => {
  if (!checkAdminPermission()) {
    return;
  }

  try {
    console.log('获取院系列表URL:', API_ENDPOINTS.getDepartments);
    const response = await get(API_ENDPOINTS.getDepartments);
    console.log('院系API原始响应:', response);
    
    // 使用适配器提取院系数据
    const departmentsData = extractDataArray<Department>(response, '院系');
    
    // 打印处理结果
    if (departmentsData.length > 0) {
      console.log('院系数据提取成功，示例:', departmentsData[0]);
      
      // 验证数据格式是否符合Department接口
      const checkResults = departmentsData.map(dept => ({
        id: dept.id,
        hasId: dept.id !== undefined,
        hasName: !!dept.name,
        name: dept.name
      }));
      console.log('院系数据格式检查:', checkResults.slice(0, 3));
    } else {
      console.warn('未找到任何院系数据');
    }
    
    // 更新院系列表
    departments.value = departmentsData;
    console.log('最终院系列表:', departments.value.length, '个院系');
  } catch (error) {
    console.error('获取院系列表失败:', error);
    ElMessage.error('获取院系列表失败');
    departments.value = [];
  }
};

// 获取专业列表
const fetchMajors = async (departmentId: number) => {
  if (!departmentId) {
    majors.value = [];
    console.warn('获取专业列表：院系ID为空');
    return;
  }
  try {
    console.log('获取专业列表URL:', `${API_ENDPOINTS.getMajors}?departmentId=${departmentId}`);
    const response = await get(API_ENDPOINTS.getMajors, { departmentId });
    console.log('专业API原始响应:', response);
    
    // 使用适配器提取专业数据
    const majorsData = extractDataArray<Major>(response, '专业');
    
    console.log('提取的所有专业数据（过滤前）:', majorsData);
    
    // 放宽过滤条件，首先使用所有取回的专业数据（不过滤院系）
    if (majorsData.length > 0) {
      // 检查专业数据结构，看是否有departmentId字段
      const hasDepartmentIdField = majorsData.some((major: any) => 'departmentId' in major);
      console.log('专业数据包含departmentId字段:', hasDepartmentIdField);
      
      if (hasDepartmentIdField) {
        // 如果有departmentId字段，尝试按院系过滤，但保留日志
        const filteredByDept = majorsData.filter((major: Major) => 
          !major.departmentId || major.departmentId === departmentId
        );
        console.log('按院系ID过滤后的专业数据:', filteredByDept, '过滤前:', majorsData.length, '过滤后:', filteredByDept.length);
        
        // 如果过滤后为空，使用所有专业数据
        if (filteredByDept.length === 0) {
          console.warn('按院系过滤后专业列表为空，使用所有专业数据');
          majors.value = majorsData;
        } else {
          majors.value = filteredByDept;
        }
      } else {
        // 没有departmentId字段，使用所有数据
        console.warn('专业数据没有departmentId字段，使用所有专业数据');
        majors.value = majorsData;
      }
    } else {
      // 没有获取到任何专业数据
      majors.value = [];
      console.error('未获取到任何专业数据');
    }
    
    console.log('最终专业列表:', majors.value);
  } catch (error) {
    console.error('获取专业列表失败:', error);
    ElMessage.error('获取专业列表失败');
    majors.value = [];
  }
};

// 获取班级列表
const fetchClasses = async (majorId: number) => {
  if (!majorId) {
    classes.value = [];
    return;
  }
  try {
    console.log('获取班级列表URL:', `${API_ENDPOINTS.getClasses}?majorId=${majorId}`);
    const response = await get(API_ENDPOINTS.getClasses, { majorId });
    console.log('班级API原始响应:', response);
    
    // 使用适配器提取班级数据
    const classesData = extractDataArray<Class>(response, '班级');
    
    console.log('提取的所有班级数据（过滤前）:', classesData);
    
    // 过滤只属于当前专业的班级
    if (classesData.length > 0) {
      // 检查班级数据结构，看是否有majorId字段
      const hasMajorIdField = classesData.some((cls: any) => 'majorId' in cls);
      console.log('班级数据包含majorId字段:', hasMajorIdField);
      
      if (hasMajorIdField) {
        // 如果有majorId字段，按专业过滤
        const filteredByMajor = classesData.filter((cls: Class) => 
          !cls.majorId || cls.majorId === majorId
        );
        console.log('按专业ID过滤后的班级数据:', filteredByMajor);
        
        // 如果过滤后为空，使用所有班级数据
        if (filteredByMajor.length === 0) {
          console.warn('按专业过滤后班级列表为空，使用所有班级数据');
          classes.value = classesData;
        } else {
          classes.value = filteredByMajor;
        }
      } else {
        // 没有majorId字段，使用所有数据
        console.warn('班级数据没有majorId字段，使用所有班级数据');
        classes.value = classesData;
      }
    } else {
      // 没有获取到任何班级数据
      classes.value = [];
      console.error('未获取到任何班级数据');
    }
    
    console.log('最终班级列表:', classes.value);
  } catch (error) {
    console.error('获取班级列表失败:', error);
    ElMessage.error('获取班级列表失败');
    classes.value = [];
  }
};

// 显示添加对话框
const showAddDialog = async () => {
  // 显示加载中提示
  loading.value = true;
  dialogType.value = 'add';
  
  try {
    // 清空表单
    studentForm.value = {
      studentId: '',
      realName: '',
      departmentId: 0,
      departmentName: '',
      majorId: 0,
      majorName: '',
      classId: 0,
      className: '',
      enrollYear: new Date().getFullYear(),
      phone: '',
      email: '',
      status: '在读'
    };
    
    // 清空列表数据
    majors.value = [];
    classes.value = [];
    
    // 加载院系列表
    try {
      await fetchDepartments();
      console.log('添加对话框院系列表加载成功:', departments.value.length + '个院系');
    } catch (error) {
      console.error('添加对话框加载院系失败:', error);
      ElMessage.warning('无法加载院系列表，可能影响添加功能');
    }
    
    // 显示对话框
    dialogVisible.value = true;
  } catch (error) {
    console.error('准备添加表单失败:', error);
    ElMessage.error('加载添加表单数据失败');
  } finally {
    loading.value = false;
  }
};

// 显示编辑对话框
const showEditDialog = async (row: Student) => {
  // 显示加载中提示
  loading.value = true;
  dialogType.value = 'edit';
  
  try {
    // 1. 先加载院系列表（不管成功与否，都继续执行）
    try {
      await fetchDepartments();
    } catch (error) {
      console.error('加载院系列表失败:', error);
      ElMessage.warning('无法加载完整院系列表，可能影响编辑功能');
    }
    
    // 2. 转换状态为中文（如果是英文）
    const normalizedStatus = translateStatus(row.status);
    
    // 3. 设置表单基本信息
    studentForm.value = {
      id: row.id,
      studentId: row.studentId,
      realName: row.realName,
      departmentId: row.departmentId || 0,
      departmentName: row.departmentName || '',
      majorId: row.majorId || 0,
      majorName: row.majorName || '',
      classId: row.classId || 0,
      className: row.className || '',
      enrollYear: row.enrollYear,
      phone: row.phone || '',
      email: row.email || '',
      status: normalizedStatus || '在读'
    };
    
    console.log('初始学生数据:', studentForm.value);
    
    // 4. 如果有院系ID，加载对应的专业列表
    if (row.departmentId) {
      try {
        await fetchMajors(row.departmentId);
        
        // 特殊处理：确保专业选项包含当前选中专业
        handleSpecialCases(row);
      } catch (error) {
        console.error('加载专业列表失败:', error);
        ElMessage.warning('无法加载完整专业列表，可能影响编辑功能');
      }
    }
    
    // 5. 如果有专业ID，加载对应的班级列表
    if (row.majorId) {
      try {
        await fetchClasses(row.majorId);
      } catch (error) {
        console.error('加载班级列表失败:', error);
        ElMessage.warning('无法加载完整班级列表，可能影响编辑功能');
      }
    }
    
    console.log('数据加载完成:', {
      departments: departments.value.length + '个院系',
      majors: majors.value.length + '个专业',
      classes: classes.value.length + '个班级'
    });
    
    // 全部数据加载完成后，再显示对话框
    dialogVisible.value = true;
  } catch (error) {
    console.error('准备编辑表单失败:', error);
    ElMessage.error('加载编辑表单数据失败');
  } finally {
    loading.value = false;
  }
};

// 显示详情对话框
const showDetailsDialog = (student: Student) => {
  currentStudent.value = student
  detailsDialogVisible.value = true
}

// 处理提交
const handleSubmit = async () => {
  if (!studentFormRef.value) return;
  
  if (!checkAdminPermission()) {
    return;
  }

  await studentFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        // 确保名称字段正确设置（即使前面的选择处理已经设置，这里再确认一次）
        if (studentForm.value.departmentId) {
          const department = departments.value.find(d => d.id === studentForm.value.departmentId);
          if (department) {
            studentForm.value.departmentName = department.name;
            console.log('提交前确认院系名称:', department.name);
          }
        }
        
        if (studentForm.value.majorId) {
          const major = majors.value.find(m => m.id === studentForm.value.majorId);
          if (major) {
            studentForm.value.majorName = major.name;
            console.log('提交前确认专业名称:', major.name);
          }
        }
        
        if (studentForm.value.classId) {
          const cls = classes.value.find(c => c.id === studentForm.value.classId);
          if (cls) {
            studentForm.value.className = cls.name;
            console.log('提交前确认班级名称:', cls.name);
          }
        }
        
        // 确保所有必要字段都已设置
        if (!studentForm.value.departmentName && studentForm.value.departmentId) {
          ElMessage.warning('院系名称未设置，请重新选择院系');
          return;
        }
        
        if (!studentForm.value.majorName && studentForm.value.majorId) {
          ElMessage.warning('专业名称未设置，请重新选择专业');
          return;
        }
        
        if (!studentForm.value.className && studentForm.value.classId) {
          ElMessage.warning('班级名称未设置，请重新选择班级');
          return;
        }
        
        // 提交所有数据，包括名称字段
        // 根据后端需要转换状态值（中文 -> 英文或保持原状）
        const submitData = {
          ...studentForm.value,
          status: translateStatusReverse(studentForm.value.status)
        };
        
        console.log('提交数据:', submitData);
        
        if (dialogType.value === 'add') {
          // 确保API端点已设置
          if (!API_ENDPOINTS.createStudent) {
            ElMessage.error('API端点未初始化，无法添加学生');
            return;
          }
          
          console.log('尝试创建学生:', API_ENDPOINTS.createStudent);
          await post(API_ENDPOINTS.createStudent, submitData);
          ElMessage.success('添加成功');
        } else {
          // 如果主要端点未初始化或失败，尝试备用端点
          const id = submitData.id!;
          const possibleUpdateEndpoints = [
            API_ENDPOINTS.updateStudent(id), // 已自动识别的API路径
            `${getApiBaseUrl()}/student/${id}`, // 直接尝试学生路径
            `${getApiBaseUrl()}/admin/student/${id}`, // 尝试管理员路径(单数)
            `${getApiBaseUrl()}/admin/students/${id}`, // 尝试管理员路径(复数)
            `${getApiBaseUrl()}/students/${id}`, // 尝试直接路径
            `/api/student/${id}`, // 直接API路径
            `/api/students/${id}`, // 直接API路径
            `/api/admin/students/${id}` // 直接API路径
          ];
          
          let updateSuccess = false;
          for (const endpoint of possibleUpdateEndpoints) {
            if (!endpoint) continue; // 跳过空端点
            
            try {
              console.log(`尝试更新学生: ${endpoint}`, submitData);
              await put(endpoint, submitData);
              console.log(`更新成功: ${endpoint}`);
              updateSuccess = true;
              
              // 更新API端点以供后续使用
              if (endpoint.includes('/student/')) {
                API_ENDPOINTS.updateStudent = (id: number) => `${getApiBaseUrl()}/student/${id}`;
              } else if (endpoint.includes('/admin/student/')) {
                API_ENDPOINTS.updateStudent = (id: number) => `${getApiBaseUrl()}/admin/student/${id}`;
              } else if (endpoint.includes('/admin/students/')) {
                API_ENDPOINTS.updateStudent = (id: number) => `${getApiBaseUrl()}/admin/students/${id}`;
              } else if (endpoint.includes('/students/')) {
                API_ENDPOINTS.updateStudent = (id: number) => `${getApiBaseUrl()}/students/${id}`;
              } else if (endpoint.includes('/api/')) {
                const basePath = endpoint.substring(0, endpoint.lastIndexOf('/'));
                API_ENDPOINTS.updateStudent = (id: number) => `${basePath}/${id}`;
              }
              
              break; // 成功后退出循环
            } catch (error: any) {
              console.error(`端点 ${endpoint} 更新失败:`, error);
              // 继续尝试下一个端点
            }
          }
          
          if (updateSuccess) {
            ElMessage.success('更新成功');
          } else {
            ElMessage.error('更新失败：所有API端点都无法连接');
          }
        }
        dialogVisible.value = false;
        // 更新学生列表
        fetchStudents();
      } catch (error: any) {
        console.error('提交失败:', error);
        console.error('错误详情:', {
          status: error.response?.status,
          statusText: error.response?.statusText,
          data: error.response?.data,
          headers: error.response?.headers,
          config: {
            url: error.config?.url,
            method: error.config?.method,
            headers: error.config?.headers
          }
        });
        
        if (error.response?.status === 403) {
          handlePermissionError();
        } else if (error.response?.status === 400) {
          // 处理验证错误
          const message = error.response?.data?.message || '输入数据验证失败';
          ElMessage.error(`提交失败: ${message}`);
        } else if (error.response?.status === 404) {
          ElMessage.error('找不到要编辑的学生记录');
        } else if (error.response?.status === 409) {
          ElMessage.error('学号已存在，请使用其他学号');
        } else {
          ElMessage.error('提交失败: ' + (error.response?.data?.message || error.message || '未知错误'));
        }
      }
    }
  });
};

// 处理删除
const handleDelete = async (id: number) => {
  if (!checkAdminPermission()) {
    return;
  }
  
  try {
    await ElMessageBox.confirm('确定要删除该学生吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    // 如果API端点未初始化，报错退出
    if (!API_ENDPOINTS.deleteStudent) {
      ElMessage.error('API端点未初始化，无法删除学生');
      return;
    }
    
    // 如果主要端点未初始化或失败，尝试备用端点
    const possibleDeleteEndpoints = [
      API_ENDPOINTS.deleteStudent(id), // 已自动识别的API路径
      `${getApiBaseUrl()}/student/${id}`, // 直接尝试学生路径
      `${getApiBaseUrl()}/admin/student/${id}`, // 尝试管理员路径(单数)
      `${getApiBaseUrl()}/admin/students/${id}`, // 尝试管理员路径(复数)
      `${getApiBaseUrl()}/students/${id}`, // 尝试直接路径
      `/api/student/${id}`, // 直接API路径
      `/api/students/${id}`, // 直接API路径
      `/api/admin/students/${id}` // 直接API路径
    ];
    
    let deleteSuccess = false;
    for (const endpoint of possibleDeleteEndpoints) {
      if (!endpoint) continue; // 跳过空端点
      
      try {
        console.log(`尝试删除学生: ${endpoint}`);
        await del(endpoint);
        console.log(`删除成功: ${endpoint}`);
        deleteSuccess = true;
        
        // 更新API端点以供后续使用
        if (endpoint.includes('/student/')) {
          API_ENDPOINTS.deleteStudent = (id: number) => `${getApiBaseUrl()}/student/${id}`;
        } else if (endpoint.includes('/admin/student/')) {
          API_ENDPOINTS.deleteStudent = (id: number) => `${getApiBaseUrl()}/admin/student/${id}`;
        } else if (endpoint.includes('/admin/students/')) {
          API_ENDPOINTS.deleteStudent = (id: number) => `${getApiBaseUrl()}/admin/students/${id}`;
        } else if (endpoint.includes('/students/')) {
          API_ENDPOINTS.deleteStudent = (id: number) => `${getApiBaseUrl()}/students/${id}`;
        } else if (endpoint.includes('/api/')) {
          const basePath = endpoint.substring(0, endpoint.lastIndexOf('/'));
          API_ENDPOINTS.deleteStudent = (id: number) => `${basePath}/${id}`;
        }
        
        break; // 成功后退出循环
      } catch (error: any) {
        console.error(`端点 ${endpoint} 删除失败:`, error);
        // 继续尝试下一个端点
      }
    }
    
    if (deleteSuccess) {
      ElMessage.success('删除成功');
      // 从当前列表中移除该学生，不重新获取
      const index = students.value.findIndex(s => s.id === id);
      if (index !== -1) {
        students.value.splice(index, 1);
        totalCount.value--;
      }
    } else {
      ElMessage.error('删除失败：所有API端点都无法连接');
    }
  } catch (error: any) {
    if (error === 'cancel') {
      // 用户取消，不做任何操作
      return;
    }
    
    if (error.message === '所有删除端点均失败') {
      ElMessage.error('删除学生失败: 所有API端点均不可用');
      return;
    }
    
    console.error('删除学生失败:', error);
    console.error('错误详情:', {
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data,
      headers: error.response?.headers,
      config: {
        url: error.config?.url,
        method: error.config?.method
      }
    });
    
    if (error.response?.status === 403) {
      handlePermissionError();
    } else if (error.response?.status === 404) {
      ElMessage.error('找不到要删除的学生记录');
    } else {
      ElMessage.error('删除学生失败: ' + (error.response?.data?.message || error.message || '未知错误'));
    }
  }
};

// 格式化学号
const formatStudentId = (studentId: string) => {
  // 如果已经是10位，直接返回
  if (studentId.length === 10) {
    return studentId;
  }
  
  // 否则，根据年级、院系、班级和个人号生成10位学号
  // 假设学号格式为：年级(4位) + 院系编号(2位) + 班级(2位) + 个人号(2位)
  const year = studentForm.value.enrollYear.toString();
  
  // 获取院系编号
  let departmentCode = "00";
  if (studentForm.value.departmentId) {
    const department = departments.value.find(d => d.id === studentForm.value.departmentId);
    if (department) {
      departmentCode = department.code;
    }
  }
  
  const classCode = studentForm.value.classId.toString().padStart(2, '0');
  const personalCode = studentId.slice(-2).padStart(2, '0');
  
  return `${year}${departmentCode}${classCode}${personalCode}`;
};

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchStudents();
};

// 处理页码变化
const handleCurrentChange = (val: number) => {
  fetchStudents();
};

// 处理每页数量变化
const handleSizeChange = (val: number) => {
  currentPage.value = 1;
  fetchStudents();
};

// 添加状态类型判断函数
const getStatusType = (status: string) => {
  // 处理英文状态
  status = translateStatus(status);
  const statusMap: Record<string, 'success' | 'warning' | 'danger' | 'info'> = {
    '在读': 'success',
    '休学': 'warning',
    '退学': 'danger',
    '毕业': 'info'
  };
  return statusMap[status] || 'info';
};

// 状态转换函数：英文 -> 中文
const translateStatus = (status: string): string => {
  const statusMap: Record<string, string> = {
    'ACTIVE': '在读',
    'SUSPENDED': '休学',
    'WITHDRAWN': '退学',
    'GRADUATED': '毕业',
  };
  return statusMap[status] || status;
};

// 状态转换函数：中文 -> 英文
const translateStatusReverse = (status: string): string => {
  const statusMap: Record<string, string> = {
    '在读': 'ACTIVE',
    '休学': 'SUSPENDED',
    '退学': 'WITHDRAWN',
    '毕业': 'GRADUATED',
  };
  return statusMap[status] || status;
};

// 处理院系变化
const handleDepartmentChange = (value: number) => {
  studentForm.value.majorId = 0;
  studentForm.value.majorName = '';
  studentForm.value.classId = 0;
  studentForm.value.className = '';
  
  // 根据ID查找院系对象，并设置departmentName
  const department = departments.value.find(dept => dept.id === value);
  if (department) {
    studentForm.value.departmentName = department.name;
    console.log('设置院系名称:', department.name);
  } else {
    // 如果在列表中找不到，保留现有名称
    console.warn('在院系列表中未找到ID:', value, '保留原始名称:', studentForm.value.departmentName);
  }
  
  if (value) {
    // 院系变化时重新加载专业列表
    fetchMajors(value).then(() => {
      console.log('院系变更后重新加载的专业列表:', majors.value.length, '个专业');
    }).catch(error => {
      console.error('院系变更后加载专业列表失败:', error);
    });
  } else {
    majors.value = [];
    classes.value = [];
  }
};

// 处理专业变化
const handleMajorChange = (value: number) => {
  studentForm.value.classId = 0;
  studentForm.value.className = '';
  
  // 根据ID查找专业对象，并设置majorName
  const major = majors.value.find(major => major.id === value);
  if (major) {
    studentForm.value.majorName = major.name;
    console.log('设置专业名称:', major.name);
  } else {
    // 如果在列表中找不到，保留现有名称
    console.warn('在专业列表中未找到ID:', value, '保留原始名称:', studentForm.value.majorName);
  }
  
  if (value) {
    fetchClasses(value);
  } else {
    classes.value = [];
  }
};

// 添加班级变化处理函数
const handleClassChange = (value: number) => {
  // 根据ID查找班级对象，并设置className
  const cls = classes.value.find(cls => cls.id === value);
  if (cls) {
    studentForm.value.className = cls.name;
    console.log('设置班级名称:', cls.name);
  } else {
    studentForm.value.className = '';
  }
};

// 帮助函数：检查departmentId是否在列表中
const hasDepartmentIdInList = (id: number): boolean => {
  return departments.value.some(d => d.id === id);
};

// 帮助函数：检查majorId是否在列表中
const hasMajorIdInList = (id: number): boolean => {
  return majors.value.some(m => m.id === id);
};

// 特殊处理专业数据缺失情况
const handleSpecialCases = (row: Student) => {
  console.log('处理特殊情况，当前行数据:', row);
  
  // 如果院系的ID已知，但在departments.value中找不到
  if (row.departmentId && row.departmentName && !hasDepartmentIdInList(row.departmentId)) {
    // 手动添加一个院系选项
    console.log('手动添加院系选项:', {id: row.departmentId, name: row.departmentName});
    departments.value.push({
      id: row.departmentId,
      name: row.departmentName,
      code: ''
    });
  }
  
  // 如果专业的ID已知，但在majors.value中找不到
  if (row.majorId && row.majorName && !hasMajorIdInList(row.majorId)) {
    // 手动添加一个专业选项
    console.log('手动添加专业选项:', {id: row.majorId, name: row.majorName});
    majors.value.push({
      id: row.majorId,
      name: row.majorName,
      code: '',
      departmentId: row.departmentId
    });
  }
};

onMounted(async () => {
  if (!checkAdminPermission()) {
    return;
  }
  
  try {
    console.log('开始初始化学生管理页面');
    
    // 首先尝试获取院系列表
    await fetchDepartments();
    
    // 尝试顺序：
    // 1. 使用常规方式从StudentController获取
    let success = await fetchStudentsFromController();
    
    // 2. 如果直接获取失败，尝试其他路径
    if (!success) {
      success = await fetchStudents();
    }
  } catch (error) {
    console.error('初始化数据失败:', error);
    ElMessage.error('加载数据失败，请刷新页面重试');
  }
});
</script>

<style scoped>
.student-management {
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
</style>