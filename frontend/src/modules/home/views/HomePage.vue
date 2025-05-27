<template>
  <div class="home-page">
    <header class="header">
      <div class="header-container">
        <div class="logo">cf军校</div>
        <div class="nav">
          <span v-for="(item, index) in navItems" :key="index" class="nav-item">{{ item }}</span>
        </div>
        <div v-if="isLoggedIn" class="user-info">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="avatar-container">
              <el-avatar :size="36" :src="userAvatar">{{ userRole.charAt(0) }}</el-avatar>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="dashboard">控制台</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <div v-else class="login-btn" @click="goToLogin">登录</div>
      </div>
    </header>

    <div class="banner">
      <el-carousel height="400px" :interval="5000" arrow="always">
        <el-carousel-item v-for="(item, index) in bannerItems" :key="index">
          <div class="banner-item" :style="{ backgroundImage: `url(${item.image})` }">
            <div class="banner-content">
              <h2>{{ item.title }}</h2>
              <p>{{ item.desc }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <div class="content">
      <div class="section news">
        <div class="section-header">
          <h2>军校新闻</h2>
          <a href="#" class="more">更多</a>
        </div>
        <div class="news-list">
          <div v-for="(item, index) in news" :key="index" class="news-item">
            <div class="news-date">
              <div class="month">{{ item.month }}</div>
              <div class="day">{{ item.day }}</div>
            </div>
            <div class="news-content">
              <h3>{{ item.title }}</h3>
              <p>{{ item.desc }}</p>
            </div>
            <div class="news-image" v-if="item.image">
              <img :src="item.image" :alt="item.title" />
            </div>
          </div>
        </div>
      </div>

      <div class="section-row">
        <div class="section events">
          <div class="section-header">
            <h2>军校活动</h2>
            <a href="#" class="more">更多</a>
          </div>
          <ul class="event-list">
            <li v-for="(item, index) in events" :key="index" class="event-item">
              <span class="event-date">{{ item.date }}</span>
              <span class="event-title">{{ item.title }}</span>
            </li>
          </ul>
        </div>

        <div class="section notices">
          <div class="section-header">
            <h2>通知公告</h2>
            <a href="#" class="more">更多</a>
          </div>
          <ul class="notice-list">
            <li v-for="(item, index) in notices" :key="index" class="notice-item">
              <span class="notice-title">{{ item.title }}</span>
              <span class="notice-date">{{ item.date }}</span>
            </li>
          </ul>
        </div>
      </div>

      <div class="section info">
        <div class="section-header">
          <h2>军校概况</h2>
        </div>
        <div class="info-cards">
          <div v-for="(item, index) in infoCards" :key="index" class="info-card">
            <div class="info-icon">
              <el-icon :size="30">
                <component :is="item.icon"></component>
              </el-icon>
            </div>
            <div class="info-title">{{ item.title }}</div>
            <div class="info-value">{{ item.value }}</div>
          </div>
        </div>
      </div>

      <div class="section military-equipment">
        <div class="section-header">
          <h2>先进装备</h2>
          <a href="#" class="more">更多</a>
        </div>
        <div class="equipment-list">
          <div v-for="(item, index) in militaryEquipment" :key="index" class="equipment-item">
            <div class="equipment-image">
              <img :src="item.image" :alt="item.name" />
            </div>
            <div class="equipment-info">
              <h3>{{ item.name }}</h3>
              <p>{{ item.desc }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <footer class="footer">
      <div class="footer-container">
        <div class="footer-info">
          <p>{{ footerText.copyright }}</p>
          <p>{{ footerText.address }}</p>
          <p>{{ footerText.phone }}</p>
          <p>{{ footerText.email }}</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
// import api from '@/api/index'  // 如果需要使用API，用正确的路径导入
// import { ossApi } from '@/modules/common/api'
// @ts-ignore
import store from '@/store'
// import { ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()

// 检查用户是否已登录
const isLoggedIn = computed(() => {
  return !!localStorage.getItem('token')
})

// 获取用户角色
const userRole = computed(() => {
  return localStorage.getItem('role') || '未知角色'
})

// 获取用户头像URL
const userAvatar = computed(() => {
  // 如果有用户头像，从localStorage或store获取
  const avatar = localStorage.getItem('userAvatar')
  if (avatar) return avatar
  
  // 根据角色返回默认头像
  switch (userRole.value) {
    case 'ADMIN':
      return 'https://campus-system1122.oss-cn-chengdu.aliyuncs.com/avatar/admin-avatar.png'
    case 'TEACHER':
      return 'https://campus-system1122.oss-cn-chengdu.aliyuncs.com/avatar/teacher-avatar.png'
    case 'STUDENT':
      return 'https://campus-system1122.oss-cn-chengdu.aliyuncs.com/avatar/student-avatar.png'
    default:
      return '' // 空字符串将使用Element Plus的默认头像
  }
})

// 更新页面标题
document.title = 'cf军校'

// 注册所有Element Plus图标组件
const components = ref({})

// 所有图片（存储原始路径和签名URL）
const imageUrls = ref(new Map());

// 获取OSS图片的签名URL
const getSignedUrl = async (objectPath: string) => {
  try {
    if (imageUrls.value.has(objectPath)) {
      return imageUrls.value.get(objectPath);
    }

    console.log(`正在获取 ${objectPath} 的签名URL...`);
    
    // 确定正确的API基础URL
    let API_BASE_URL;
    if (window.location.hostname === 'localhost') {
      API_BASE_URL = 'http://localhost:80';
    } else {
      // 使用当前IP访问后端
      API_BASE_URL = `http://${window.location.hostname}:80`;
    }
    
    // 直接请求后端API，而不是依赖Vite代理
    const response = await axios.get(`${API_BASE_URL}/api/oss/get-signed-url`, {
      params: { objectName: objectPath },
      headers: {
        'Content-Type': 'application/json'
      },
      withCredentials: true // 允许跨域请求携带凭证
    });
    
    console.log('获取签名URL响应:', response.data);
    
    if (response.data && (response.data as any).signedUrl) {
      const signedUrl = (response.data as any).signedUrl;
      imageUrls.value.set(objectPath, signedUrl);
      return signedUrl;
    } else {
      console.error('获取签名URL失败: 返回数据不完整', response.data);
      return useDefaultOssUrl(objectPath);
    }
  } catch (error: any) {
    console.error('获取签名URL失败:', error);
    
    // 如果是401错误且包含过期信息，清除本地令牌
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token');
      console.log('令牌已过期，已清除本地存储');
    }
    
    return useDefaultOssUrl(objectPath);
  }
};

// 添加一个辅助函数来处理默认URL
const useDefaultOssUrl = (objectPath: string) => {
  const defaultUrl = `https://campus-system1122.oss-cn-chengdu.aliyuncs.com/${objectPath}`;
  console.log(`使用默认URL: ${defaultUrl}`);
  return defaultUrl;
};

// 加载所有Banner图片的签名URL
const loadBannerImages = async () => {
  for (const item of bannerItems.value) {
    if (item.objectPath) {
      const signedUrl = await getSignedUrl(item.objectPath);
      if (signedUrl) {
        item.image = signedUrl;
      }
    }
  }
};

// 加载所有新闻图片的签名URL
const loadNewsImages = async () => {
  for (const item of news.value) {
    if (item.objectPath) {
      const signedUrl = await getSignedUrl(item.objectPath);
      if (signedUrl) {
        item.image = signedUrl;
      }
    }
  }
};

// 加载所有军事装备图片的签名URL
const loadEquipmentImages = async () => {
  for (const item of militaryEquipment.value) {
    if (item.objectPath) {
      const signedUrl = await getSignedUrl(item.objectPath);
      if (signedUrl) {
        item.image = signedUrl;
      }
    }
  }
};

// 加载通知数据
const loadNotices = async () => {
  try {
    // 确定正确的API基础URL
    let apiBaseUrl;
    if (window.location.hostname === 'localhost') {
      apiBaseUrl = 'http://localhost:80';
    } else {
      // 使用当前IP访问后端
      apiBaseUrl = `http://${window.location.hostname}:80`;
    }
    
    // 获取通知列表（限制5条，仅获取已发布的通知）
    const response = await axios.get(`${apiBaseUrl}/api/notification/public`, {
      params: {
        size: 5,
        status: '已发布'
      }
    });
    
    if (response.data && (response.data as any).content) {
      notices.value = (response.data as any).content.map((notice: any) => ({
        title: notice.title,
        date: formatDate(notice.publishTime),
        id: notice.id,
        type: notice.type
      }));
    }
  } catch (error) {
    console.error('获取通知列表失败:', error);
    // 保留默认通知数据
  }
};

// 日期格式化
const formatDate = (dateString: string): string => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return `${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
};

onMounted(async () => {
  // 确保ElementPlusIconsVue已正确导入
  console.log('Element Plus图标组件:', Object.keys(ElementPlusIconsVue).length)
  // 确保页面标题更新
  document.title = 'cf军校'
  
  // 加载所有图片
  await Promise.all([
    loadBannerImages(),
    loadNewsImages(),
    loadEquipmentImages()
  ]);
  
  // 加载通知数据
  await loadNotices();
})

// 处理下拉菜单命令
const handleCommand = (command: string) => {
  console.log('执行命令:', command)
  
  switch (command) {
    case 'profile':
      router.push('/user/profile')
      break
    case 'dashboard':
      const dashboardPath = getDashboardPath()
      router.push(dashboardPath)
      break
    case 'logout':
      logout()
      break
  }
}

// 根据角色获取对应的控制台路径
const getDashboardPath = () => {
  const role = localStorage.getItem('role')
  switch (role) {
    case 'ADMIN':
      return '/admin'
    case 'TEACHER':
      return '/teacher/dashboard'
    case 'STUDENT':
      return '/student/dashboard'
    default:
      return '/dashboard'
  }
}

// 登出方法
const logout = () => {
  // 清除存储的Token和角色
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  
  // 使用Vuex清除用户状态
  store.dispatch('logout')
  
  // 提示用户
  ElMessage.success('已成功退出登录')
  
  // 刷新页面让变化立即生效
  window.location.reload()
}

// 登录方法保持不变
const goToLogin = () => {
  console.log('跳转到登录页面')
  try {
    // 添加更多诊断信息
    console.log('当前路由:', router.currentRoute.value)
    console.log('可用路由:', router.getRoutes().map(route => route.path))
    
    // 使用直接URL跳转如果路由不工作
    if (router.hasRoute('Login')) {
      router.push('/login')
    } else {
      console.warn('找不到登录路由，使用window.location跳转')
      window.location.href = '/login'
    }
  } catch (error) {
    console.error('路由跳转失败:', error)
    // 使用备用方法
    window.location.href = '/login'
  }
}

const navItems = ref([
  '首页',
  '学校概况',
  '学院部门',
  '师资队伍',
  '人才培养',
  '科学研究',
  '招生就业',
  '公共服务'
])

// 更新bannerItems数据
const bannerItems = ref([
  {
    title: 'cf军校',
    desc: '打造智能、高效、便捷的军事教育平台',
    image: 'https://campus-system1122.oss-cn-chengdu.aliyuncs.com/images/banner/banner1.jpg',
    objectPath: 'images/banner/banner1.jpg'
  },
  {
    title: '军事训练',
    desc: '培养过硬军事素质，锻造坚强战斗意志',
    image: 'https://campus-system1122.oss-cn-chengdu.aliyuncs.com/images/banner/banner2.jpg',
    objectPath: 'images/banner/banner2.jpg'
  },
  {
    title: '科技创新',
    desc: '推动军事科研，促进国防建设',
    image: 'https://campus-system1122.oss-cn-chengdu.aliyuncs.com/images/banner/7078d0f8a8394415a63a9579ea3664ab.png',
    objectPath: 'images/banner/7078d0f8a8394415a63a9579ea3664ab.png'
  }
])

// 更新news数据
const news = ref([
  {
    month: '三月',
    day: '30',
    title: '2025年度军事训练计划正式启动',
    desc: '根据总部指示，2025年度军事训练计划已正式启动，重点加强实战化训练和信息化建设。',
    image: 'https://campus-system1122.oss-cn-chengdu.aliyuncs.com/images/news/news1.jpg',
    objectPath: 'images/news/news1.jpg'
  },
  {
    month: '三月',
    day: '27',
    title: 'cf军校系统更新公告',
    desc: '为提高作战指挥效率，cf军校信息管理系统将于本周六晚间进行版本更新，请各单位做好准备。',
    image: 'https://campus-system1122.oss-cn-chengdu.aliyuncs.com/images/news/news2.jpg',
    objectPath: 'images/news/news2.jpg'
  },
  {
    month: '三月',
    day: '25',
    title: '2025年春季军官晋升评定工作开始',
    desc: '各部门请于4月10日前完成初审工作，并将相关材料报送人事处审核。',
    image: 'https://campus-system1122.oss-cn-chengdu.aliyuncs.com/images/news/news3.jpg',
    objectPath: 'images/news/news3.jpg'
  }
])

const events = ref([
  { date: '04-05', title: '清明节纪念活动' },
  { date: '04-10', title: '军校歌手大赛初赛' },
  { date: '04-15', title: '学术讲座：人工智能与教育创新' },
  { date: '04-20', title: '春季运动会' },
  { date: '04-25', title: '创新创业大赛报名开始' }
])

// 通知公告数据
const notices = ref([
  { title: '关于开展2025年教师资格认定工作的通知', date: '03-30' },
  { title: '关于组织开展心理健康月活动的通知', date: '03-28' },
  { title: '关于军校网络系统升级维护的通知', date: '03-25' },
  { title: '关于申报2025年度科研项目的通知', date: '03-20' },
  { title: '关于开展学生宿舍安全检查的通知', date: '03-15' }
])

const infoCards = ref([
  { title: '建校年份', value: '1956', icon: 'Calendar' },
  { title: '教学单位', value: '26', icon: 'School' },
  { title: '专任教师', value: '2200+', icon: 'User' },
  { title: '在校学生', value: '40000+', icon: 'Avatar' },
  { title: '科研平台', value: '10+', icon: 'Monitor' },
  { title: '军校面积', value: '139万m²', icon: 'OfficeBuilding' }
])

// 更新底部信息
const footerText = ref({
  copyright: 'cf军校 © 2025',
  address: '地址：成都市高新区（西区）西源大道2006号',
  phone: '联系电话：028-61830000',
  email: '电子邮箱：contact@cfmilitary.edu.cn'
})

// 更新军事装备展示部分
const militaryEquipment = ref([
  {
    name: '先进战机',
    desc: '具备超强空中作战能力',
    image: 'https://campus-system1122.oss-cn-chengdu.aliyuncs.com/images/equipment/aircraft.jpg',
    objectPath: 'images/equipment/aircraft.jpg'
  },
  {
    name: '陆战装甲',
    desc: '全地形高机动性作战平台',
    image: 'https://campus-system1122.oss-cn-chengdu.aliyuncs.com/images/equipment/tank.jpg',
    objectPath: 'images/equipment/tank.jpg'
  },
  {
    name: '海军舰艇',
    desc: '现代化海上作战力量',
    image: 'https://campus-system1122.oss-cn-chengdu.aliyuncs.com/images/equipment/ship.jpg',
    objectPath: 'images/equipment/ship.jpg'
  }
])
</script>

<style scoped>
.home-page {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.header {
  background-color: #003366;
  color: white;
  padding: 0 20px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
}

.logo {
  font-size: 22px;
  font-weight: bold;
}

.nav {
  display: flex;
  gap: 20px;
}

.nav-item {
  cursor: pointer;
  padding: 5px 0;
  position: relative;
}

.nav-item:hover::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: #ffffff;
}

.login-btn {
  background-color: #f50057;
  color: white;
  padding: 5px 15px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.login-btn:hover {
  background-color: #c51162;
}

.banner {
  width: 100%;
}

.banner-item {
  height: 400px;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  text-align: center;
}

.banner-content {
  background-color: rgba(0, 0, 0, 0.5);
  padding: 20px;
  border-radius: 8px;
  max-width: 600px;
}

.banner-content h2 {
  font-size: 2.5rem;
  margin-bottom: 10px;
}

.content {
  max-width: 1200px;
  margin: 30px auto;
  padding: 0 20px;
}

.section {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.section-header h2 {
  font-size: 1.5rem;
  color: #003366;
  margin: 0;
}

.more {
  color: #1976d2;
  text-decoration: none;
}

.news-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.news-item {
  display: flex;
  gap: 15px;
  align-items: flex-start;
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: 8px;
  transition: transform 0.3s;
}

.news-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.news-date {
  width: 60px;
  height: 60px;
  background-color: #003366;
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  border-radius: 4px;
  flex-shrink: 0;
}

.month {
  font-size: 0.8rem;
}

.day {
  font-size: 1.5rem;
  font-weight: bold;
}

.news-content h3 {
  margin-top: 0;
  margin-bottom: 5px;
  font-size: 1.1rem;
  color: #333;
}

.news-content p {
  margin: 0;
  color: #666;
  font-size: 0.9rem;
}

.news-image {
  width: 120px;
  height: 80px;
  overflow: hidden;
  border-radius: 4px;
  flex-shrink: 0;
}

.news-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.section-row {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
}

.section-row .section {
  flex: 1;
  margin-bottom: 0;
}

.event-list, .notice-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.event-item, .notice-item {
  padding: 10px 0;
  border-bottom: 1px dashed #eee;
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.event-item:last-child, .notice-item:last-child {
  border-bottom: none;
}

.event-date, .notice-date {
  color: #888;
  font-size: 0.9rem;
  flex-shrink: 0;
}

.event-title, .notice-title {
  flex-grow: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.info-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 20px;
}

.info-card {
  text-align: center;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  transition: transform 0.3s;
}

.info-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.info-icon {
  font-size: 2rem;
  color: #003366;
  margin-bottom: 10px;
}

.info-title {
  font-size: 1rem;
  color: #666;
  margin-bottom: 5px;
}

.info-value {
  font-size: 1.5rem;
  font-weight: bold;
  color: #003366;
}

.footer {
  background-color: #003366;
  color: white;
  padding: 30px 0;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  text-align: center;
}

.footer-info p {
  margin: 5px 0;
}

.military-equipment {
  margin-top: 30px;
}

.equipment-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.equipment-item {
  background-color: #f9f9f9;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s;
}

.equipment-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.equipment-image {
  height: 200px;
  overflow: hidden;
}

.equipment-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.equipment-info {
  padding: 15px;
}

.equipment-info h3 {
  margin-top: 0;
  margin-bottom: 5px;
  font-size: 1.1rem;
  color: #003366;
}

.equipment-info p {
  margin: 0;
  color: #666;
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .section-row {
    flex-direction: column;
  }
  
  .info-cards {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }
  
  .nav {
    display: none;
  }
  
  .news-item {
    flex-direction: column;
  }
  
  .news-image {
    width: 100%;
    height: 160px;
    margin-top: 10px;
  }
  
  .equipment-list {
    grid-template-columns: 1fr;
  }
}

/* 添加头像容器样式 */
.avatar-container {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.1);
  transition: background-color 0.3s;
}

.avatar-container:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

/* 保持其他用户信息样式不变 */
.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
}
</style> 