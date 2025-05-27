<template>
  <div class="user-management">
    <div class="header">
      <h2>用户管理</h2>
      <button class="add-user-btn" @click="showAddDialog = true">
        <i class="fas fa-plus"></i> 添加用户
      </button>
    </div>

    <div class="search-bar">
      <input 
        type="text" 
        v-model="searchQuery" 
        placeholder="搜索用户..."
        @input="handleSearch"
      >
    </div>

    <div class="user-list">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>角色</th>
            <th>邮箱</th>
            <th>电话</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in filteredUsers" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.username }}</td>
            <td>
              <span :class="['role-badge', user.role.toLowerCase()]">
                {{ user.role }}
              </span>
            </td>
            <td>{{ user.email }}</td>
            <td>{{ user.phone }}</td>
            <td>
              <span :class="['status-badge', user.enabled ? 'active' : 'inactive']">
                {{ user.enabled ? '启用' : '禁用' }}
              </span>
            </td>
            <td>
              <button class="edit-btn" @click="handleEditUser(user)">
                <i class="fas fa-edit"></i>
              </button>
              <button class="delete-btn" @click="handleDeleteUser(user.id)">
                <i class="fas fa-trash"></i>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 添加/编辑用户对话框 -->
    <div v-if="showAddDialog || showEditDialog" class="dialog-overlay">
      <div class="dialog">
        <h3>{{ showEditDialog ? '编辑用户' : '添加用户' }}</h3>
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label>用户名</label>
            <input type="text" v-model="currentUser.username" required>
          </div>
          <div class="form-group">
            <label>密码</label>
            <input type="password" v-model="currentUser.password" :required="!showEditDialog">
          </div>
          <div class="form-group">
            <label>角色</label>
            <select v-model="currentUser.role" required>
              <option value="ADMIN">管理员</option>
              <option value="TEACHER">教师</option>
              <option value="STUDENT">学生</option>
            </select>
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <input type="email" v-model="currentUser.email" required>
          </div>
          <div class="form-group">
            <label>电话</label>
            <input type="tel" v-model="currentUser.phone" required>
          </div>
          <div class="form-group">
            <label>状态</label>
            <select v-model="currentUser.enabled" required>
              <option :value="true">启用</option>
              <option :value="false">禁用</option>
            </select>
          </div>
          <div class="dialog-buttons">
            <button type="button" class="cancel-btn" @click="closeDialog">取消</button>
            <button type="submit" class="submit-btn">确定</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getUsers, createUser, updateUser, deleteUser } from '../api'
import { ElMessage } from 'element-plus'

interface User {
  id: number
  username: string
  password?: string
  role: string
  email: string
  phone: string
  enabled: boolean
}

interface UserRequestData {
  id?: number
  username: string
  password?: string
  role: string
  email: string
  phone: string
  enabled: boolean
}

interface ApiError {
  response?: {
    data?: string;
    status?: number;
  };
  message?: string;
}

const users = ref<User[]>([])
const searchQuery = ref('')
const showAddDialog = ref(false)
const showEditDialog = ref(false)
const currentUser = ref<User>({
  id: 0,
  username: '',
  password: '',
  role: 'STUDENT',
  email: '',
  phone: '',
  enabled: true
})

// 过滤用户列表
const filteredUsers = computed(() => {
  if (!searchQuery.value) return users.value
  const query = searchQuery.value.toLowerCase()
  return users.value.filter(user => 
    user.username.toLowerCase().includes(query) ||
    user.email.toLowerCase().includes(query) ||
    user.phone.includes(query)
  )
})

// 获取用户列表
const fetchUsers = async () => {
  try {
    console.log('正在获取用户列表...');
    const response = await getUsers()
    users.value = response.data
    console.log('获取用户列表成功:', users.value)
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  }
}

// 处理搜索
const handleSearch = () => {
  // 实时搜索，不需要额外处理
}

// 处理编辑用户
const handleEditUser = (user: User) => {
  currentUser.value = { ...user, password: '' }
  showEditDialog.value = true
}

// 处理删除用户
const handleDeleteUser = async (id: number) => {
  if (!confirm('确定要删除该用户吗？')) return
  
  try {
    await deleteUser(id)
    ElMessage.success('删除用户成功')
    await fetchUsers()
  } catch (error) {
    console.error('删除用户失败:', error)
    ElMessage.error('删除用户失败')
  }
}

// 处理表单提交
const handleSubmit = async () => {
  try {
    // 验证表单
    if (!currentUser.value.username) {
      ElMessage.warning('用户名不能为空')
      return
    }

    if (!showEditDialog.value && (!currentUser.value.password || currentUser.value.password.trim() === '')) {
      ElMessage.warning('创建用户时密码不能为空')
      return
    }

    // 构建请求数据对象
    const userData: UserRequestData = {
      username: currentUser.value.username,
      role: currentUser.value.role,
      email: currentUser.value.email || '',
      phone: currentUser.value.phone || '', 
      enabled: currentUser.value.enabled
    }

    // 手动构建完整的请求数据
    if (showEditDialog.value) {
      // 编辑模式 - 只在有修改密码时添加密码
      if (currentUser.value.password && currentUser.value.password.trim() !== '') {
        // 使用用户实际输入的密码，不再使用测试密码
        userData.password = currentUser.value.password
        console.log('更新用户密码，长度:', userData.password.length)
      }
      
      // 添加ID字段
      userData.id = currentUser.value.id
    } else {
      // 创建模式 - 必须添加密码
      // 使用用户实际输入的密码，不再使用测试密码
      userData.password = currentUser.value.password || ''
      console.log('创建用户密码，长度:', userData.password?.length || 0)
    }
    
    console.log('发送的用户数据：', { ...userData, password: userData.password ? '******' : '无密码' })

    // 使用API发送请求
    try {
      if (showEditDialog.value) {
        await updateUser(currentUser.value.id, userData)
        ElMessage.success('用户编辑成功')
      } else {
        // 记录完整请求数据
        console.log('创建用户请求URL:', '/user')
        console.log('创建用户请求方法:', 'POST')
        console.log('创建用户请求数据:', JSON.stringify(userData))
        
        // 发送创建用户请求
        await createUser(userData)
        ElMessage.success('用户创建成功')
      }
      
      closeDialog()
      await fetchUsers()
    } catch (error: Error | unknown) {
      const typedError = error as ApiError
      const errorMsg = typedError.response?.data || '操作失败'
      console.error('用户操作失败:', error, errorMsg)
      ElMessage.error(errorMsg)
    }
  } catch (error: Error | unknown) {
    console.error('表单处理失败:', error)
    ElMessage.error('操作失败')
  }
}

// 关闭对话框
const closeDialog = () => {
  showAddDialog.value = false
  showEditDialog.value = false
  currentUser.value = {
    id: 0,
    username: '',
    password: '',
    role: 'STUDENT',
    email: '',
    phone: '',
    enabled: true
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.add-user-btn {
  padding: 8px 16px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}

.add-user-btn:hover {
  background-color: #66b1ff;
}

.search-bar {
  margin-bottom: 20px;
}

.search-bar input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

table {
  width: 100%;
  border-collapse: collapse;
  background-color: white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

th, td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

th {
  background-color: #f5f7fa;
  font-weight: 500;
}

.role-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.role-badge.admin {
  background-color: #f56c6c;
  color: white;
}

.role-badge.teacher {
  background-color: #67c23a;
  color: white;
}

.role-badge.student {
  background-color: #409eff;
  color: white;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-badge.active {
  background-color: #67c23a;
  color: white;
}

.status-badge.inactive {
  background-color: #909399;
  color: white;
}

.edit-btn, .delete-btn {
  padding: 4px 8px;
  margin-right: 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  color: white;
}

.edit-btn {
  background-color: #67c23a;
}

.delete-btn {
  background-color: #f56c6c;
}

.edit-btn:hover {
  background-color: #85ce61;
}

.delete-btn:hover {
  background-color: #f78989;
}

.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.dialog {
  background: white;
  padding: 24px;
  border-radius: 8px;
  width: 100%;
  max-width: 500px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #606266;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.dialog-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.cancel-btn,
.submit-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.cancel-btn {
  background-color: #909399;
  color: white;
}

.submit-btn {
  background-color: #409eff;
  color: white;
}

.cancel-btn:hover {
  background-color: #a6a9ad;
}

.submit-btn:hover {
  background-color: #66b1ff;
}
</style> 