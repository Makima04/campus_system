<template>
  <div class="login-container">
    <div class="login-box">
      <div class="logo-container">
        <img 
          src="@/assets/logo.png" 
          alt="校园管理系统" 
          class="logo"
          onerror="this.src='https://campus-system1122.oss-cn-chengdu.aliyuncs.com/images/logo/logo.png'; this.onerror=null;"
        >
        <h1>校园管理系统</h1>
      </div>
      <div class="form-container">
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
        >
          <el-form-item prop="username">
            <el-input 
              v-model="loginForm.username" 
              placeholder="用户名"
              prefix-icon="el-icon-user"
              autocomplete="username"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="密码"
              prefix-icon="el-icon-lock"
              show-password
              autocomplete="current-password"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
            <el-link class="forget-password" type="primary">忘记密码?</el-link>
          </el-form-item>
          <el-form-item>
            <el-button 
              :loading="loading" 
              type="primary" 
              class="login-btn" 
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { encryptPassword } from '@/utils/crypto'
import store from '@/store'  // 直接导入store实例，而不是使用useStore
import axios from 'axios'
import { escapeHtml } from '@/utils/security'
import { config } from '@/config/env-config' // 导入环境配置

const router = useRouter()
const loading = ref(false)
const loginFormRef = ref(null)

const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 根据角色跳转到不同页面
const redirectByRole = (role) => {
  console.log('根据角色重定向，角色:', role);
  
  const routes = {
    'ROLE_ADMIN': '/admin',
    'ROLE_TEACHER': '/teacher/dashboard',
    'ROLE_STUDENT': '/student/dashboard'
  }
  
  const redirectPath = routes[role] || '/dashboard';
  console.log('重定向路径:', redirectPath);
  
  return redirectPath;
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    
    try {
      // 对用户输入进行安全处理，防止XSS攻击
      const safeUsername = escapeHtml(loginForm.username)
      
      // 使用加密密码
      const encryptedPassword = encryptPassword(loginForm.password)
      
      console.log('登录信息:', {
        username: safeUsername,
        password: '******'
      })
      
      // 使用相对路径，通过Nginx代理发送请求
      console.log('通过Nginx代理发送请求...');
      const response = await axios.post('/api/auth/login', {
        username: safeUsername,
        password: encryptedPassword
      });
      
      console.log('登录响应:', response.data);
      
      if (response.data && response.data.token) {
        // 保存token到localStorage和store
        localStorage.setItem('token', response.data.token);
        
        // 获取用户角色，保持原始格式
        const userRole = response.data.user ? response.data.user.role : 'ROLE_STUDENT';
        
        // 保存角色
        localStorage.setItem('role', userRole);
        
        // 如果勾选了"记住我"，保存用户名
        if (loginForm.rememberMe) {
          localStorage.setItem('savedUsername', safeUsername);
        } else {
          localStorage.removeItem('savedUsername');
        }
        
        // 创建用户响应对象
        const authResponse = {
          token: response.data.token,
          userInfo: response.data.user || {
            id: 1,
            username: safeUsername,
            role: userRole
          },
          role: userRole
        };
        
        // 调用store的action
        store.dispatch('login', authResponse);
        
        // 登录成功
        ElMessage.success('登录成功');
        
        // 根据角色跳转
        const redirectPath = redirectByRole(userRole);
        router.push(redirectPath);
      } else {
        throw new Error('登录失败：未获取到有效的Token');
      }
    } catch (error) {
      console.error('登录失败:', error);
      ElMessage.error(error.message || '登录失败，请检查用户名和密码');
      
      // 清空密码
      loginForm.password = '';
    } finally {
      loading.value = false;
    }
  });
};

// onMounted钩子函数，在组件挂载后执行
onMounted(() => {
  // 预加载备用logo图片
  const preloadLogo = new Image()
  preloadLogo.src = 'https://campus-system1122.oss-cn-chengdu.aliyuncs.com/logo/school-logo.png'
  
  // 如果localStorage中有保存的用户名，则自动填充
  const savedUsername = localStorage.getItem('savedUsername')
  if (savedUsername && loginForm.rememberMe) {
    loginForm.username = savedUsername
  }
})
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
}

.logo-container {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  width: 80px;
  height: 80px;
  margin-bottom: 16px;
}

.logo-container h1 {
  font-size: 24px;
  color: #303133;
  margin: 0;
}

.form-container {
  margin-top: 30px;
}

.login-form {
  width: 100%;
}

.login-btn {
  width: 100%;
  border-radius: 4px;
  height: 40px;
  font-size: 16px;
}

.forget-password {
  float: right;
}

/* 响应式调整 */
@media (max-width: 576px) {
  .login-box {
    width: 90%;
    padding: 20px;
  }
  
  .logo {
    width: 60px;
    height: 60px;
  }
  
  .logo-container h1 {
    font-size: 20px;
  }
}
</style> 