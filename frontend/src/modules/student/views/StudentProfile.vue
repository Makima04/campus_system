<template>
  <div class="student-profile">
    <div class="page-header">
      <h2>个人信息</h2>
      <div class="action-buttons">
        <el-button type="primary" @click="editMode = true" v-if="!editMode">
          编辑信息
        </el-button>
        <template v-else>
          <el-button type="success" @click="saveProfile">保存</el-button>
          <el-button @click="cancelEdit">取消</el-button>
        </template>
      </div>
    </div>
    
    <div class="profile-container">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="avatar-container">
            <el-avatar 
              :size="150" 
              :src="profile.avatar || defaultAvatar" 
              @error="avatarLoadError"
            />
            <div class="upload-avatar" v-if="editMode">
              <el-upload
                class="avatar-uploader"
                action="#"
                :auto-upload="false"
                :show-file-list="false"
                :on-change="handleAvatarChange"
              >
                <el-button size="small" type="primary">更换头像</el-button>
              </el-upload>
            </div>
            <h3>{{ profile.name }}</h3>
            <p class="student-id">学号: {{ profile.studentId }}</p>
            <el-tag type="success">{{ profile.department }}</el-tag>
          </div>
        </el-col>
        
        <el-col :span="18">
          <el-tabs>
            <el-tab-pane label="基本信息">
              <el-card shadow="never">
                <el-form 
                  :model="profile" 
                  :disabled="!editMode" 
                  label-width="120px"
                  label-position="right"
                >
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="姓名">
                        <el-input v-model="profile.name" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="学号">
                        <el-input v-model="profile.studentId" disabled />
                      </el-form-item>
                    </el-col>
                  </el-row>
                  
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="性别">
                        <el-select v-model="profile.gender">
                          <el-option label="男" value="男" />
                          <el-option label="女" value="女" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="出生日期">
                        <el-date-picker 
                          v-model="profile.birthDate" 
                          type="date" 
                          placeholder="选择日期"
                          value-format="YYYY-MM-DD"
                        />
                      </el-form-item>
                    </el-col>
                  </el-row>
                  
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="院系">
                        <el-input v-model="profile.department" disabled />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="专业">
                        <el-input v-model="profile.major" disabled />
                      </el-form-item>
                    </el-col>
                  </el-row>
                  
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="班级">
                        <el-input v-model="profile.className" disabled />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="入学年份">
                        <el-input v-model="profile.enrollmentYear" disabled />
                      </el-form-item>
                    </el-col>
                  </el-row>
                  
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="手机号码">
                        <el-input v-model="profile.phone" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="邮箱">
                        <el-input v-model="profile.email" />
                      </el-form-item>
                    </el-col>
                  </el-row>
                  
                  <el-form-item label="家庭住址">
                    <el-input v-model="profile.address" />
                  </el-form-item>
                </el-form>
              </el-card>
            </el-tab-pane>
            
            <el-tab-pane label="其他信息">
              <el-card shadow="never">
                <el-descriptions title="账户信息" :column="2" border>
                  <el-descriptions-item label="账户状态">
                    <el-tag type="success">正常</el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="上次登录">
                    {{ profile.lastLogin || '未记录' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="学生类型">
                    {{ profile.studentType || '本科生' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="预计毕业时间">
                    {{ profile.expectedGraduation || '未设置' }}
                  </el-descriptions-item>
                </el-descriptions>
                
                <el-divider />
                
                <el-descriptions title="宿舍信息" :column="2" border>
                  <el-descriptions-item label="宿舍楼">
                    {{ profile.dormitoryBuilding || '未分配' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="宿舍号">
                    {{ profile.dormitoryNumber || '未分配' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="床位号">
                    {{ profile.bedNumber || '未分配' }}
                  </el-descriptions-item>
                </el-descriptions>
              </el-card>
            </el-tab-pane>
            
            <el-tab-pane label="账户安全">
              <el-card shadow="never">
                <div class="security-item">
                  <div class="security-info">
                    <h4>修改密码</h4>
                    <p>定期修改密码可以提高账户安全性</p>
                  </div>
                  <el-button @click="showPasswordDialog = true">修改密码</el-button>
                </div>
                
                <el-divider />
                
                <div class="security-item">
                  <div class="security-info">
                    <h4>手机绑定</h4>
                    <p>已绑定手机：{{ maskPhoneNumber(profile.phone) }}</p>
                  </div>
                  <el-button @click="showPhoneDialog = true">更换手机</el-button>
                </div>
                
                <el-divider />
                
                <div class="security-item">
                  <div class="security-info">
                    <h4>邮箱绑定</h4>
                    <p>已绑定邮箱：{{ maskEmail(profile.email) }}</p>
                  </div>
                  <el-button @click="showEmailDialog = true">更换邮箱</el-button>
                </div>
              </el-card>
            </el-tab-pane>
          </el-tabs>
        </el-col>
      </el-row>
    </div>
    
    <!-- 修改密码对话框 -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="500px">
      <el-form :model="passwordForm" label-width="120px" :rules="passwordRules" ref="passwordFormRef">
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input v-model="passwordForm.currentPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showPasswordDialog = false">取消</el-button>
          <el-button type="primary" @click="changePassword">确认修改</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 修改手机号对话框 -->
    <el-dialog v-model="showPhoneDialog" title="修改手机号" width="500px">
      <el-form :model="phoneForm" label-width="120px" :rules="phoneRules" ref="phoneFormRef">
        <el-form-item label="新手机号" prop="newPhone">
          <el-input v-model="phoneForm.newPhone" />
        </el-form-item>
        <el-form-item label="验证码" prop="verificationCode">
          <div class="verification-code">
            <el-input v-model="phoneForm.verificationCode" />
            <el-button type="primary" :disabled="countdown > 0">
              {{ countdown > 0 ? `${countdown}秒后重新获取` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showPhoneDialog = false">取消</el-button>
          <el-button type="primary" @click="changePhone">确认修改</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 修改邮箱对话框 -->
    <el-dialog v-model="showEmailDialog" title="修改邮箱" width="500px">
      <el-form :model="emailForm" label-width="120px" :rules="emailRules" ref="emailFormRef">
        <el-form-item label="新邮箱" prop="newEmail">
          <el-input v-model="emailForm.newEmail" />
        </el-form-item>
        <el-form-item label="验证码" prop="verificationCode">
          <div class="verification-code">
            <el-input v-model="emailForm.verificationCode" />
            <el-button type="primary" :disabled="emailCountdown > 0">
              {{ emailCountdown > 0 ? `${emailCountdown}秒后重新获取` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEmailDialog = false">取消</el-button>
          <el-button type="primary" @click="changeEmail">确认修改</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import axios from 'axios'

const store = useStore()
const loading = ref(false)
const userId = computed(() => store.state.user.userInfo?.id)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 表单引用
const passwordFormRef = ref<FormInstance | null>(null)
const phoneFormRef = ref<FormInstance | null>(null)
const emailFormRef = ref<FormInstance | null>(null)

// 个人资料
const originalProfile = ref({})
const profile = ref({
  studentId: '',
  name: '',
  gender: '',
  birthDate: '',
  department: '',
  major: '',
  className: '',
  enrollmentYear: '',
  phone: '',
  email: '',
  address: '',
  avatar: '',
  lastLogin: '',
  studentType: '本科生',
  expectedGraduation: '',
  dormitoryBuilding: '',
  dormitoryNumber: '',
  bedNumber: ''
})

// 编辑模式
const editMode = ref(false)

// 对话框显示状态
const showPasswordDialog = ref(false)
const showPhoneDialog = ref(false)
const showEmailDialog = ref(false)

// 修改密码表单
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 修改手机号表单
const phoneForm = ref({
  newPhone: '',
  verificationCode: ''
})

// 修改邮箱表单
const emailForm = ref({
  newEmail: '',
  verificationCode: ''
})

// 验证码倒计时
const countdown = ref(0)
const emailCountdown = ref(0)

// 密码表单验证规则
const passwordRules = {
  currentPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, message: '密码长度不能少于8个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { 
      validator: (_: unknown, value: string, callback: (error?: Error) => void) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

// 手机号表单验证规则
const phoneRules = {
  newPhone: [
    { required: true, message: '请输入新手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

// 邮箱表单验证规则
const emailRules = {
  newEmail: [
    { required: true, message: '请输入新邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

// 上传文件接口
interface UploadFile {
  raw: File;
  name: string;
  size: number;
  type: string;
}

// 个人资料接口
interface ProfileData {
  studentId: string;
  name: string;
  gender: string;
  birthDate: string;
  department: string;
  major: string;
  className: string;
  enrollmentYear: string;
  phone: string;
  email: string;
  address: string;
  avatar: string;
  lastLogin: string;
  studentType: string;
  expectedGraduation: string;
  dormitoryBuilding: string;
  dormitoryNumber: string;
  bedNumber: string;
}

// 请求错误接口
interface ApiError {
  response?: {
    data?: string;
    status?: number;
  };
  message?: string;
}

// 获取学生个人资料
const fetchStudentProfile = async () => {
  try {
    loading.value = true
    const response = await axios.get<ProfileData>('/api/student/profile')
    
    if (response.data) {
      profile.value = response.data as ProfileData
      originalProfile.value = JSON.parse(JSON.stringify(response.data))
    }
  } catch (error) {
    console.error('获取学生个人资料失败:', error)
    ElMessage.error('获取个人资料失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 保存个人资料
const saveProfile = async () => {
  try {
    const response = await axios.put(`/api/students/${userId.value}/profile`, profile.value)
    if (response.status === 200) {
      ElMessage.success('个人资料更新成功')
      editMode.value = false
      originalProfile.value = JSON.parse(JSON.stringify(profile.value))
    }
  } catch (error) {
    console.error('更新个人资料失败:', error)
    ElMessage.error('更新个人资料失败')
  }
}

// 取消编辑
const cancelEdit = () => {
  profile.value = JSON.parse(JSON.stringify(originalProfile.value))
  editMode.value = false
}

// 头像加载失败时的处理
const avatarLoadError = () => {
  profile.value.avatar = defaultAvatar
}

// 处理头像变更
const handleAvatarChange = (file: UploadFile) => {
  const reader = new FileReader()
  reader.readAsDataURL(file.raw)
  reader.onload = () => {
    profile.value.avatar = reader.result as string || ''
  }
}

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        const response = await axios.post(`/api/user/${userId.value}/change-password`, {
          currentPassword: passwordForm.value.currentPassword,
          newPassword: passwordForm.value.newPassword
        })
        
        if (response.status === 200) {
          ElMessage.success('密码修改成功')
          showPasswordDialog.value = false
          passwordForm.value = {
            currentPassword: '',
            newPassword: '',
            confirmPassword: ''
          }
        }
      } catch (error: unknown) {
        console.error('修改密码失败:', error)
        const typedError = error as ApiError
        ElMessage.error(typedError.response?.data || '修改密码失败')
      }
    }
  })
}

// 修改手机号
const changePhone = async () => {
  if (!phoneFormRef.value) return
  
  await phoneFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        const response = await axios.post(`/api/user/${userId.value}/change-phone`, {
          phone: phoneForm.value.newPhone,
          verificationCode: phoneForm.value.verificationCode
        })
        
        if (response.status === 200) {
          ElMessage.success('手机号修改成功')
          profile.value.phone = phoneForm.value.newPhone
          showPhoneDialog.value = false
          phoneForm.value = {
            newPhone: '',
            verificationCode: ''
          }
        }
      } catch (error: unknown) {
        console.error('修改手机号失败:', error)
        const typedError = error as ApiError
        ElMessage.error(typedError.response?.data || '修改手机号失败')
      }
    }
  })
}

// 修改邮箱
const changeEmail = async () => {
  if (!emailFormRef.value) return
  
  await emailFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        const response = await axios.post(`/api/user/${userId.value}/change-email`, {
          email: emailForm.value.newEmail,
          verificationCode: emailForm.value.verificationCode
        })
        
        if (response.status === 200) {
          ElMessage.success('邮箱修改成功')
          profile.value.email = emailForm.value.newEmail
          showEmailDialog.value = false
          emailForm.value = {
            newEmail: '',
            verificationCode: ''
          }
        }
      } catch (error: unknown) {
        console.error('修改邮箱失败:', error)
        const typedError = error as ApiError
        ElMessage.error(typedError.response?.data || '修改邮箱失败')
      }
    }
  })
}

// 遮盖手机号
const maskPhoneNumber = (phone: string) => {
  if (!phone) return '未绑定'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 遮盖邮箱
const maskEmail = (email: string) => {
  if (!email) return '未绑定'
  const parts = email.split('@')
  if (parts.length !== 2) return email
  
  let username = parts[0]
  if (username.length <= 3) {
    username = username.charAt(0) + '****'
  } else {
    username = username.substr(0, 3) + '****'
  }
  
  return username + '@' + parts[1]
}

onMounted(() => {
  // 调用API获取实际数据
  fetchStudentProfile()
})
</script>

<style scoped>
.student-profile {
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

.action-buttons {
  display: flex;
  gap: 12px;
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.student-id {
  margin: 0;
  color: #666;
}

.profile-container {
  margin-bottom: 20px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
}

.security-info h4 {
  margin: 0 0 8px 0;
}

.security-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.verification-code {
  display: flex;
  gap: 12px;
}

.verification-code .el-button {
  white-space: nowrap;
}

.upload-avatar {
  margin-top: 12px;
}
</style> 