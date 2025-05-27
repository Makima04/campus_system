<template>
  <div class="user-management">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="dialogFormVisible = true">
        <el-icon><Plus /></el-icon> 添加用户
      </el-button>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="请选择角色" clearable>
            <el-option label="管理员" value="ROLE_ADMIN" />
            <el-option label="教师" value="ROLE_TEACHER" />
            <el-option label="学生" value="ROLE_STUDENT" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card class="table-card">
      <el-table :data="users" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="name" label="姓名" width="150" />
        <el-table-column prop="email" label="邮箱" width="220" />
        <el-table-column prop="phone" label="电话" width="150" />
        <el-table-column prop="role" label="角色">
          <template #default="scope">
            <el-tag :type="getRoleType(scope.row.role)">{{ getRoleName(scope.row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button 
              size="small" 
              :type="scope.row.status === 1 ? 'danger' : 'success'"
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 添加/编辑用户表单 -->
    <el-dialog
      v-model="dialogFormVisible"
      :title="isEdit ? '编辑用户' : '添加用户'"
      width="500px"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="isEdit" />
        </el-form-item>
        
        <el-form-item label="姓名" prop="name">
          <el-input v-model="userForm.name" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        
        <el-form-item label="电话" prop="phone">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="选择角色">
            <el-option label="管理员" value="ROLE_ADMIN" />
            <el-option label="教师" value="ROLE_TEACHER" />
            <el-option label="学生" value="ROLE_STUDENT" />
          </el-select>
        </el-form-item>
        
        <!-- 修改用户时显示修改密码复选框 -->
        <el-form-item v-if="isEdit" label="修改密码">
          <el-switch v-model="changePassword" />
        </el-form-item>
        
        <!-- 新用户或者选择修改密码时显示密码输入框 -->
        <el-form-item v-if="!isEdit || changePassword" label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogFormVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { get, post, put } from '@/utils/http'

interface User {
  id: number
  username: string
  name: string
  email: string
  phone: string
  role: string
  status: number
  realName?: string
}

interface ErrorWithMessage {
  message?: string;
  response?: {
    data?: {
      message?: string;
    };
    status?: number;
  };
}

// 表格数据
const users = ref<User[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 表单数据
const userFormRef = ref()
const dialogFormVisible = ref(false)
const isEdit = ref(false)
const changePassword = ref(false)
const userForm = reactive({
  id: 0,
  username: '',
  name: '',
  email: '',
  phone: '',
  role: '',
  password: '',
  status: 1
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  password: [
    { required: (): boolean => !isEdit.value || changePassword.value, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为 6 个字符', trigger: 'blur' }
  ]
}

// 根据角色获取标签类型
const getRoleType = (role: string) => {
  const map: Record<string, string> = {
    ROLE_ADMIN: 'danger',
    ROLE_TEACHER: 'warning',
    ROLE_STUDENT: 'success'
  }
  return map[role] || 'info'
}

// 获取角色名称
const getRoleName = (role: string) => {
  const map: Record<string, string> = {
    ROLE_ADMIN: '管理员',
    ROLE_TEACHER: '教师',
    ROLE_STUDENT: '学生'
  }
  return map[role] || role
}

// 添加API端点常量
const API_ENDPOINTS = {
  getUsers: '/api/admin/user/list',
  getUser: (id: number | string) => `/api/admin/user/${id}`,
  createUser: '/api/admin/user',
  updateUser: (id: number | string) => `/api/admin/user/${id}`,
  deleteUser: (id: number | string) => `/api/admin/user/${id}`
};

// 数据适配器 - 尝试从各种后端响应格式中提取数据数组
const extractDataArray = <T>(response: unknown, entityName: string): T[] => {
  console.log(`尝试为${entityName}提取数据数组，原始响应:`, response);
  
  try {
    // 检查HTML响应（服务器返回的页面而不是API数据）
    if (typeof response === 'string' && response.includes('<!DOCTYPE html>')) {
      console.error(`${entityName}响应返回了HTML而不是JSON数据，API端点可能不正确`);
      return [];
    }
    
    // 情况1: 直接是数组
    if (Array.isArray(response)) {
      console.log(`${entityName}响应直接是数组，长度:`, response.length);
      return response.filter((item: unknown) => item && typeof item === 'object' && 'id' in item && item.id !== undefined);
    }
    
    // 情况2: Axios响应包装 + 后端自定义响应结构
    if (response && typeof response === 'object' && 'data' in response && response.data && typeof response.data === 'object') {
      console.log(`${entityName}响应中的data属性:`, response.data);
      
      // 检查嵌套的后端响应结构
      if ('code' in response.data && response.data.code === 200 && 'data' in response.data && response.data.data) {
        if (Array.isArray(response.data.data)) {
          console.log(`找到后端标准响应格式中的data数组，长度:`, response.data.data.length);
          return response.data.data.filter((item: unknown) => item && typeof item === 'object' && 'id' in item && item.id !== undefined) as T[];
        } else if (typeof response.data.data === 'object' && 'content' in response.data.data && Array.isArray(response.data.data.content)) {
          // 处理分页数据
          console.log(`找到后端分页响应格式中的content数组，长度:`, response.data.data.content.length);
          return response.data.data.content.filter((item: unknown) => item && typeof item === 'object' && 'id' in item && item.id !== undefined) as T[];
        }
      }
      
      // 直接检查response.data (Axios包装的数据体)
      if (Array.isArray(response.data)) {
        console.log(`${entityName}响应的data属性直接是数组，长度:`, response.data.length);
        return response.data.filter((item: unknown) => item && typeof item === 'object' && 'id' in item && item.id !== undefined) as T[];
      }
    }
    
    // 情况3: 标准REST响应格式，检查常见数据字段名
    if (response && typeof response === 'object') {
      const commonDataFields = ['content', 'list', 'items', 'rows', 'records', entityName.toLowerCase() + 's', 'users'];
      
      for (const field of commonDataFields) {
        if (field in (response as Record<string, unknown>) && Array.isArray((response as Record<string, unknown>)[field])) {
          console.log(`${entityName}响应中找到 ${field} 数组字段，长度:`, ((response as Record<string, unknown>)[field] as unknown[]).length);
          return ((response as Record<string, unknown>)[field] as unknown[]).filter((item: unknown) => item && typeof item === 'object' && 'id' in item && item.id !== undefined) as T[];
        }
        
        // 检查response.data中的字段
        if (typeof response === 'object' && 'data' in response && response.data && typeof response.data === 'object') {
          if (field in (response.data as Record<string, unknown>) && Array.isArray((response.data as Record<string, unknown>)[field])) {
            console.log(`${entityName}响应的data属性中找到 ${field} 数组字段，长度:`, ((response.data as Record<string, unknown>)[field] as unknown[]).length);
            return ((response.data as Record<string, unknown>)[field] as unknown[]).filter((item: unknown) => item && typeof item === 'object' && 'id' in item && item.id !== undefined) as T[];
          }
        }
      }
      
      // 情况4: Spring Data分页响应
      if ('content' in (response as Record<string, unknown>) && Array.isArray((response as Record<string, unknown>).content)) {
        console.log(`${entityName}响应是Spring Data分页格式，数据长度:`, ((response as Record<string, unknown>).content as unknown[]).length);
        return ((response as Record<string, unknown>).content as unknown[]).filter((item: unknown) => item && typeof item === 'object' && 'id' in item && item.id !== undefined) as T[];
      }
      
      // 检查response.data中的分页格式
      if (typeof response === 'object' && 'data' in response && response.data && typeof response.data === 'object' && 
          'content' in (response.data as Record<string, unknown>) && Array.isArray((response.data as Record<string, unknown>).content)) {
        console.log(`${entityName}响应的data属性是Spring Data分页格式，数据长度:`, ((response.data as Record<string, unknown>).content as unknown[]).length);
        return ((response.data as Record<string, unknown>).content as unknown[]).filter((item: unknown) => item && typeof item === 'object' && 'id' in item && item.id !== undefined) as T[];
      }
      
      // 情况5: 查找任何嵌套的数组
      const arrayProps = Object.keys(response as Record<string, unknown>).filter(key => Array.isArray((response as Record<string, unknown>)[key]));
      if (arrayProps.length > 0) {
        // 使用第一个找到的数组
        const firstArrayProp = arrayProps[0];
        console.log(`${entityName}响应中找到数组属性 ${firstArrayProp}，长度:`, ((response as Record<string, unknown>)[firstArrayProp] as unknown[]).length);
        return ((response as Record<string, unknown>)[firstArrayProp] as unknown[]).filter((item: unknown) => item && typeof item === 'object' && 'id' in item && item.id !== undefined) as T[];
      }
      
      // 检查response.data中的任何数组属性
      if (typeof response === 'object' && 'data' in response && response.data && typeof response.data === 'object') {
        const dataArrayProps = Object.keys(response.data as Record<string, unknown>).filter(key => Array.isArray((response.data as Record<string, unknown>)[key]));
        if (dataArrayProps.length > 0) {
          // 使用第一个找到的数组
          const firstDataArrayProp = dataArrayProps[0];
          console.log(`${entityName}响应的data属性中找到数组 ${firstDataArrayProp}，长度:`, ((response.data as Record<string, unknown>)[firstDataArrayProp] as unknown[]).length);
          return ((response.data as Record<string, unknown>)[firstDataArrayProp] as unknown[]).filter((item: unknown) => item && typeof item === 'object' && 'id' in item && item.id !== undefined) as T[];
        }
      }
      
      // 输出所有顶级属性，帮助调试
      console.log(`${entityName}响应是对象，但未找到数组。所有顶级属性:`, Object.keys(response));
      if (typeof response === 'object' && 'data' in response && response.data && typeof response.data === 'object') {
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

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true;
  try {
    console.log('尝试获取用户数据: 用户管理API -', API_ENDPOINTS.getUsers);
    
    // 构建查询参数
    const params = new URLSearchParams();
    if (currentPage.value) {
      params.append('page', String(currentPage.value - 1)); // 转换为从0开始的页码
    }
    if (pageSize.value) {
      params.append('size', String(pageSize.value));
    }
    if (searchForm.username) {
      params.append('username', searchForm.username);
    }
    if (searchForm.role) {
      params.append('role', searchForm.role);
    }
    
    const response = await get(`${API_ENDPOINTS.getUsers}?${params.toString()}`);
    
    console.log('API路径 用户管理API 响应:', response);
    const data = extractDataArray<User>(response, '用户');
    console.log('用户响应中的data属性:', data);
    
    if (Array.isArray(data)) {
      users.value = data;
      total.value = data.length; // 由于后端没有返回总数，暂时使用数组长度
    } else {
      console.error('用户数据格式不正确');
      ElMessage.error('获取用户数据失败：数据格式不正确');
    }
  } catch (error: Error | unknown) {
    console.error('API路径 用户管理API 获取用户失败:', (error as ErrorWithMessage).message);
    ElMessage.error((error as ErrorWithMessage).message || '获取用户数据失败');
  } finally {
    loading.value = false;
  }
};

// 搜索表单
const searchForm = reactive({
  username: '',
  role: ''
});

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchUsers();
};

// 重置搜索
const resetSearch = () => {
  searchForm.username = '';
  searchForm.role = '';
  currentPage.value = 1;
  fetchUsers();
};

// 处理分页大小变化
const handleSizeChange = (val: number) => {
  pageSize.value = val;
  fetchUsers();
};

// 处理页码变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val;
  fetchUsers();
};

// 编辑用户
const handleEdit = (row: User) => {
  isEdit.value = true;
  Object.assign(userForm, {
    id: row.id,
    username: row.username,
    name: row.realName || row.name,
    email: row.email,
    phone: row.phone,
    role: row.role.toLowerCase(),
    status: row.status
  });
  dialogFormVisible.value = true;
};

// 切换用户状态
const handleToggleStatus = (row: User) => {
  const action = row.status === 1 ? '禁用' : '启用'
  ElMessageBox.confirm(
    `确定要${action}用户 "${row.username}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      console.log(`尝试${action}用户:`, row.id);
      
      // 调用API切换用户状态
      const newStatus = row.status === 1 ? 0 : 1;
      await put(API_ENDPOINTS.updateUser(row.id), { status: newStatus });
      
      // 更新当前显示的用户数据
      const index = users.value.findIndex((u: User) => u.id === row.id);
      if (index !== -1) {
        users.value[index].status = newStatus;
      }
      
      ElMessage.success(`${action}成功`);
    } catch (error: Error | unknown) {
      console.error(`${action}用户失败:`, error);
      ElMessage.error(`${action}用户失败: ` + ((error as ErrorWithMessage).response?.data?.message || (error as ErrorWithMessage).message || '未知错误'));
    }
  }).catch(() => {})
}

// 提交表单
const submitForm = async () => {
  if (!userFormRef.value) return;
  
  await userFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        const formData = {
          ...userForm,
          realName: userForm.name // 将name映射到realName
        };
        
        if (isEdit.value) {
          await put(API_ENDPOINTS.updateUser(userForm.id), formData);
          ElMessage.success('更新用户成功');
        } else {
          await post(API_ENDPOINTS.createUser, formData);
          ElMessage.success('创建用户成功');
        }
        
        dialogFormVisible.value = false;
        fetchUsers();
      } catch (error: Error | unknown) {
        ElMessage.error((error as ErrorWithMessage).message || '操作失败');
      }
    }
  });
};

// 初始化
onMounted(() => {
  fetchUsers();
});
</script>

<style scoped>
.user-management {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 