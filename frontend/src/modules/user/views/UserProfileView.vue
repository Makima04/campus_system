<template>
  <div class="user-profile">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h2>个人资料</h2>
          <el-button type="primary" @click="editMode = true" v-if="!editMode">编辑资料</el-button>
          <div v-else>
            <el-button type="success" @click="saveProfile">保存</el-button>
            <el-button @click="cancelEdit">取消</el-button>
          </div>
        </div>
      </template>
      
      <div class="profile-content">
        <div class="avatar-container">
          <el-avatar :size="100" :src="userForm.avatar || defaultAvatar"></el-avatar>
          <el-upload
            v-if="editMode"
            class="avatar-uploader"
            action="#"
            :http-request="uploadAvatar"
            :show-file-list="false"
            accept="image/*"
          >
            <el-button size="small" type="primary">更换头像</el-button>
          </el-upload>
        </div>
        
        <div class="profile-info">
          <template v-if="!editMode">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
              <el-descriptions-item label="姓名">{{ userInfo.name }}</el-descriptions-item>
              <el-descriptions-item label="学号/工号">{{ userInfo.studentId || userInfo.employeeId || '无' }}</el-descriptions-item>
              <el-descriptions-item label="角色">{{ formatRole(userInfo.role) }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ userInfo.email }}</el-descriptions-item>
              <el-descriptions-item label="手机">{{ userInfo.phone || '未设置' }}</el-descriptions-item>
              <el-descriptions-item label="地址">{{ userInfo.address || '未设置' }}</el-descriptions-item>
              <el-descriptions-item label="注册时间">{{ formatDate(userInfo.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="简介">{{ userInfo.bio || '这个人很懒，什么都没写...' }}</el-descriptions-item>
            </el-descriptions>
          </template>
          
          <el-form v-else :model="userForm" label-width="80px">
            <el-form-item label="姓名">
              <el-input v-model="userForm.name"></el-input>
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="userForm.email"></el-input>
            </el-form-item>
            <el-form-item label="手机">
              <el-input v-model="userForm.phone"></el-input>
            </el-form-item>
            <el-form-item label="地址">
              <el-input v-model="userForm.address"></el-input>
            </el-form-item>
            <el-form-item label="简介">
              <el-input type="textarea" v-model="userForm.bio" rows="3"></el-input>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>
    
    <!-- 修改密码卡片 -->
    <el-card class="password-card">
      <template #header>
        <div class="card-header">
          <h2>修改密码</h2>
        </div>
      </template>
      
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="changePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from '@vue/runtime-dom'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/modules/user/store'
import type { User } from '@/modules/user/types'

const userStore = useUserStore()
const defaultAvatar = '/src/assets/default-avatar.png'

// 用户信息
const userInfo = computed(() => userStore.userInfo)

// 编辑模式
const editMode = ref(false)

// 用户表单
const userForm = reactive({
  name: '',
  email: '',
  phone: '',
  address: '',
  bio: '',
  avatar: ''
})

// 密码表单
const passwordFormRef = ref()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 格式化角色显示
function formatRole(role: string): string {
  const roleMap: Record<string, string> = {
    'ADMIN': '管理员',
    'STUDENT': '学生',
    'TEACHER': '教师',
    'LIBRARIAN': '图书管理员'
  }
  return roleMap[role] || role
}

// 格式化日期
function formatDate(dateString: string): string {
  if (!dateString) return '未知'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 取消编辑
function cancelEdit() {
  editMode.value = false
  initUserForm()
}

// 保存个人资料
async function saveProfile() {
  try {
    // 在实际应用中，这里会调用API保存用户资料
    // const res = await userService.updateProfile(userForm)
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 更新本地存储的用户信息
    userStore.updateUserInfo({
      ...userInfo.value,
      name: userForm.name,
      email: userForm.email,
      phone: userForm.phone,
      address: userForm.address,
      bio: userForm.bio
    })
    
    ElMessage.success('个人资料更新成功')
    editMode.value = false
  } catch (error) {
    console.error('更新个人资料失败:', error)
    ElMessage.error('更新个人资料失败')
  }
}

// 上传头像
async function uploadAvatar(options: any) {
  const file = options.file
  
  // 在实际应用中，这里会调用API上传头像
  // const res = await userService.uploadAvatar(file)
  // 模拟API调用
  await new Promise(resolve => setTimeout(resolve, 1000))
  
  // 转换为base64以供预览
  const reader = new FileReader()
  reader.readAsDataURL(file)
  reader.onload = () => {
    userForm.avatar = reader.result as string
    // 更新用户信息
    userStore.updateUserInfo({
      ...userInfo.value,
      avatar: userForm.avatar
    })
    ElMessage.success('头像上传成功')
  }
}

// 修改密码
async function changePassword() {
  if (!passwordFormRef.value) return
  
  passwordFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    try {
      // 在实际应用中，这里会调用API修改密码
      // const res = await userService.changePassword(passwordForm)
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 800))
      
      ElMessage.success('密码修改成功')
      
      // 重置表单
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      passwordFormRef.value.resetFields()
    } catch (error) {
      console.error('修改密码失败:', error)
      ElMessage.error('修改密码失败')
    }
  })
}

// 初始化用户表单
function initUserForm() {
  userForm.name = userInfo.value.name || ''
  userForm.email = userInfo.value.email || ''
  userForm.phone = userInfo.value.phone || ''
  userForm.address = userInfo.value.address || ''
  userForm.bio = userInfo.value.bio || ''
  userForm.avatar = userInfo.value.avatar || ''
}

onMounted(() => {
  initUserForm()
})
</script>

<style scoped>
.user-profile {
  max-width: 900px;
  margin: 20px auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-card, .password-card {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-content {
  display: flex;
  gap: 30px;
  padding: 10px 0;
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.profile-info {
  flex: 1;
}

@media (max-width: 768px) {
  .profile-content {
    flex-direction: column;
    align-items: center;
  }
  
  .profile-info {
    width: 100%;
  }
}

h2 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}
</style> 