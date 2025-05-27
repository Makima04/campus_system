<template>
  <div class="user-settings">
    <el-card class="settings-card">
      <template #header>
        <div class="card-header">
          <h2>账号设置</h2>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="个人偏好" name="preferences">
          <el-form :model="preferencesForm" label-width="120px">
            <el-form-item label="界面语言">
              <el-select v-model="preferencesForm.language">
                <el-option label="简体中文" value="zh-CN" />
                <el-option label="English" value="en-US" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="界面主题">
              <el-radio-group v-model="preferencesForm.theme">
                <el-radio label="light">浅色主题</el-radio>
                <el-radio label="dark">深色主题</el-radio>
                <el-radio label="system">跟随系统</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="字体大小">
              <el-slider v-model="preferencesForm.fontSize" :min="12" :max="20" show-stops />
            </el-form-item>
            
            <el-form-item label="通知设置">
              <el-checkbox-group v-model="preferencesForm.notifications">
                <el-checkbox label="email">邮件通知</el-checkbox>
                <el-checkbox label="sms">短信通知</el-checkbox>
                <el-checkbox label="system">系统通知</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="savePreferences">保存偏好设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="隐私设置" name="privacy">
          <el-form :model="privacyForm" label-width="140px">
            <el-form-item label="个人资料可见性">
              <el-radio-group v-model="privacyForm.profileVisibility">
                <el-radio label="public">所有人可见</el-radio>
                <el-radio label="friends">仅好友可见</el-radio>
                <el-radio label="private">仅自己可见</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="活动状态">
              <el-switch
                v-model="privacyForm.showOnlineStatus"
                active-text="显示在线状态"
                inactive-text="隐藏在线状态"
              />
            </el-form-item>
            
            <el-form-item label="搜索索引">
              <el-switch
                v-model="privacyForm.allowSearchIndex"
                active-text="允许在搜索中显示"
                inactive-text="不允许在搜索中显示"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="savePrivacySettings">保存隐私设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="账号安全" name="security">
          <el-form label-width="120px">
            <el-form-item label="登录设备">
              <el-table :data="securityData.loginDevices" style="width: 100%">
                <el-table-column prop="device" label="设备" />
                <el-table-column prop="location" label="地点" />
                <el-table-column prop="lastLogin" label="最近登录时间" />
                <el-table-column label="操作" width="120">
                  <template #default="scope">
                    <el-button
                      size="small"
                      type="danger"
                      @click="logoutDevice(scope.row)"
                      :disabled="scope.row.current"
                    >
                      {{ scope.row.current ? '当前设备' : '退出登录' }}
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-form-item>
            
            <el-form-item label="两步验证">
              <el-switch
                v-model="securityData.twoFactorEnabled"
                @change="toggleTwoFactor"
              />
              <span class="setting-desc">{{ securityData.twoFactorEnabled ? '已启用' : '未启用' }}</span>
            </el-form-item>
            
            <el-form-item v-if="securityData.twoFactorEnabled" label="验证应用">
              <el-image :src="securityData.qrCodeUrl" style="width: 200px; height: 200px" />
              <p class="setting-desc">请使用身份验证器应用扫描二维码</p>
            </el-form-item>
            
            <el-form-item label="账号删除">
              <el-button type="danger" @click="showDeleteAccountDialog">注销账号</el-button>
              <p class="setting-desc">注意：账号删除后，所有数据将无法恢复</p>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 删除账号对话框 -->
    <el-dialog
      v-model="deleteAccountDialog.visible"
      title="账号注销确认"
      width="500px"
    >
      <p>注销账号会导致以下情况：</p>
      <ul class="delete-account-list">
        <li>您的所有个人资料将被永久删除</li>
        <li>您的所有操作记录将被删除</li>
        <li>您将无法再访问任何系统资源</li>
      </ul>
      
      <p>请输入您的密码以确认注销账号：</p>
      <el-input 
        v-model="deleteAccountDialog.password" 
        type="password" 
        placeholder="请输入密码"
        show-password
      />
      
      <template #footer>
        <el-button @click="deleteAccountDialog.visible = false">取消</el-button>
        <el-button type="danger" @click="deleteAccount" :loading="deleteAccountDialog.loading">
          确认注销账号
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/modules/user/store'

const userStore = useUserStore()
const activeTab = ref('preferences')

// 个人偏好设置
const preferencesForm = reactive({
  language: 'zh-CN',
  theme: 'light',
  fontSize: 14,
  notifications: ['email', 'system']
})

// 隐私设置
const privacyForm = reactive({
  profileVisibility: 'friends',
  showOnlineStatus: true,
  allowSearchIndex: true
})

// 安全设置
const securityData = reactive({
  loginDevices: [
    { 
      id: 1, 
      device: '当前浏览器 (Chrome/Windows)', 
      location: '中国, 北京', 
      lastLogin: '2024-04-05 10:30:45',
      current: true
    },
    { 
      id: 2, 
      device: 'Mozilla Firefox/Windows', 
      location: '中国, 上海', 
      lastLogin: '2024-04-01 14:22:10',
      current: false
    },
    { 
      id: 3, 
      device: 'Mobile Safari/iOS', 
      location: '中国, 广州', 
      lastLogin: '2024-03-28 08:15:32',
      current: false
    }
  ],
  twoFactorEnabled: false,
  qrCodeUrl: 'https://example.com/qrcode.png' // 示例URL，实际使用时需要后端生成
})

// 删除账号对话框
const deleteAccountDialog = reactive({
  visible: false,
  password: '',
  loading: false
})

// 保存个人偏好设置
async function savePreferences() {
  try {
    // 在实际应用中，这里会调用API保存用户偏好设置
    // const res = await userService.savePreferences(preferencesForm)
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 800))
    
    // 应用设置
    applyTheme(preferencesForm.theme)
    document.documentElement.style.fontSize = `${preferencesForm.fontSize}px`
    
    ElMessage.success('偏好设置保存成功')
  } catch (error) {
    console.error('保存偏好设置失败:', error)
    ElMessage.error('保存偏好设置失败')
  }
}

// 保存隐私设置
async function savePrivacySettings() {
  try {
    // 在实际应用中，这里会调用API保存隐私设置
    // const res = await userService.savePrivacySettings(privacyForm)
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 600))
    
    ElMessage.success('隐私设置保存成功')
  } catch (error) {
    console.error('保存隐私设置失败:', error)
    ElMessage.error('保存隐私设置失败')
  }
}

// 切换两步验证
async function toggleTwoFactor(enabled: boolean) {
  try {
    // 在实际应用中，这里会调用API启用或禁用两步验证
    // const res = await userService.toggleTwoFactor(enabled)
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    if (enabled) {
      ElMessage.success('两步验证已启用')
      // 获取新的二维码URL
      // securityData.qrCodeUrl = res.data.qrCodeUrl
    } else {
      ElMessage.warning('两步验证已禁用')
    }
  } catch (error) {
    // 恢复开关状态
    securityData.twoFactorEnabled = !enabled
    console.error('切换两步验证失败:', error)
    ElMessage.error('操作失败')
  }
}

// 退出设备登录
async function logoutDevice(device: any) {
  try {
    // 在实际应用中，这里会调用API退出指定设备的登录
    // const res = await userService.logoutDevice(device.id)
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 从列表中移除
    const index = securityData.loginDevices.findIndex(item => item.id === device.id)
    if (index !== -1) {
      securityData.loginDevices.splice(index, 1)
    }
    
    ElMessage.success('设备已退出登录')
  } catch (error) {
    console.error('退出设备登录失败:', error)
    ElMessage.error('操作失败')
  }
}

// 显示删除账号对话框
function showDeleteAccountDialog() {
  ElMessageBox.confirm(
    '注销账号是不可逆操作，账号中的所有数据将被永久删除。确定要继续吗？',
    '警告',
    {
      confirmButtonText: '继续',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    deleteAccountDialog.visible = true
  }).catch(() => {})
}

// 注销账号
async function deleteAccount() {
  if (!deleteAccountDialog.password) {
    ElMessage.error('请输入密码')
    return
  }
  
  try {
    deleteAccountDialog.loading = true
    
    // 在实际应用中，这里会调用API注销账号
    // const res = await userService.deleteAccount(deleteAccountDialog.password)
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    deleteAccountDialog.loading = false
    deleteAccountDialog.visible = false
    
    ElMessage.success('账号已注销')
    
    // 退出登录
    userStore.logout()
    
    // 重定向到登录页
    window.location.href = '/login'
  } catch (error) {
    deleteAccountDialog.loading = false
    console.error('注销账号失败:', error)
    ElMessage.error('注销账号失败，请确认密码是否正确')
  }
}

// 应用主题
function applyTheme(theme: string) {
  if (theme === 'system') {
    // 检测系统主题
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    document.documentElement.setAttribute('data-theme', prefersDark ? 'dark' : 'light')
  } else {
    document.documentElement.setAttribute('data-theme', theme)
  }
}

// 获取用户设置
async function fetchUserSettings() {
  try {
    // 在实际应用中，这里会调用API获取用户设置
    // const res = await userService.getSettings()
    // 模拟数据
    const settings = {
      preferences: {
        language: 'zh-CN',
        theme: 'light',
        fontSize: 14,
        notifications: ['email', 'system']
      },
      privacy: {
        profileVisibility: 'friends',
        showOnlineStatus: true,
        allowSearchIndex: true
      },
      security: {
        twoFactorEnabled: false
      }
    }
    
    // 更新表单数据
    Object.assign(preferencesForm, settings.preferences)
    Object.assign(privacyForm, settings.privacy)
    securityData.twoFactorEnabled = settings.security.twoFactorEnabled
    
    // 应用设置
    applyTheme(settings.preferences.theme)
    document.documentElement.style.fontSize = `${settings.preferences.fontSize}px`
  } catch (error) {
    console.error('获取用户设置失败:', error)
    ElMessage.error('获取用户设置失败')
  }
}

onMounted(() => {
  fetchUserSettings()
})
</script>

<style scoped>
.user-settings {
  max-width: 900px;
  margin: 20px auto;
}

.settings-card {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.setting-desc {
  color: #909399;
  font-size: 12px;
  margin-left: 10px;
}

.delete-account-list {
  color: #f56c6c;
  margin: 15px 0;
  padding-left: 20px;
}

h2 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}
</style> 