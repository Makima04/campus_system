<template>
  <div class="student-dashboard">
    <h2 class="dashboard-title">
      学生仪表盘
      <div class="dashboard-actions">
        <el-button type="primary" plain size="small" @click="goToHome">
          <el-icon><Back /></el-icon>
          返回首页
        </el-button>
        <el-button type="danger" plain size="small" @click="logout">
          <el-icon><SwitchButton /></el-icon>
          退出登录
        </el-button>
      </div>
    </h2>
    
    <!-- 欢迎栏 -->
    <el-card class="welcome-card">
      <div class="welcome-content">
        <div class="welcome-text">
          <h3>{{ getGreeting() }}，{{ userInfo.username || '同学' }}</h3>
          <p>欢迎使用校园管理系统，祝您学习愉快！</p>
          <div class="date-info">
            <span>{{ todayDate }}</span>
            <el-tag size="small" effect="plain">{{ semesterInfo }}</el-tag>
          </div>
        </div>
        <div class="welcome-image">
          <el-avatar :size="80" src="https://campus-system1122.oss-cn-chengdu.aliyuncs.com/images/avatar/default-avatar.jpg"></el-avatar>
        </div>
      </div>
    </el-card>

    <div class="dashboard-row">
      <!-- 左侧内容 -->
      <div class="dashboard-main">
        <!-- 数据概览 -->
        <div class="stat-cards">
          <el-card class="stat-card" shadow="hover">
            <template #header>
              <div class="stat-header">
                <el-icon><Calendar /></el-icon>
                <span>今日课程</span>
              </div>
            </template>
            <div class="stat-value">{{ todayCourseCount }}</div>
            <div class="stat-desc">门课程</div>
          </el-card>

          <el-card class="stat-card" shadow="hover">
            <template #header>
              <div class="stat-header">
                <el-icon><Collection /></el-icon>
                <span>已修学分</span>
              </div>
            </template>
            <div class="stat-value">{{ completedCredits }}</div>
            <div class="stat-desc">学分</div>
          </el-card>

          <el-card class="stat-card" shadow="hover">
            <template #header>
              <div class="stat-header">
                <el-icon><Reading /></el-icon>
                <span>选课数量</span>
              </div>
            </template>
            <div class="stat-value">{{ selectedCourseCount }}</div>
            <div class="stat-desc">门课程</div>
          </el-card>

          <el-card class="stat-card" shadow="hover">
            <template #header>
              <div class="stat-header">
                <el-icon><Document /></el-icon>
                <span>平均成绩</span>
              </div>
            </template>
            <div class="stat-value">{{ averageGrade }}</div>
            <div class="stat-desc">分</div>
          </el-card>
        </div>

        <!-- 今日课程 -->
        <el-card class="today-courses">
          <template #header>
            <div class="card-header">
              <span>今日课程安排</span>
              <el-button type="primary" link @click="goToSchedule">查看完整课表</el-button>
            </div>
          </template>
          <div v-if="todayCourses.length === 0" class="empty-courses">
            <el-empty description="今天没有课程安排"></el-empty>
          </div>
          <div v-else class="course-timeline">
            <div v-for="(course, index) in todayCourses" :key="index" class="timeline-item">
              <div class="timeline-time">{{ course.time }}</div>
              <el-card class="timeline-content" shadow="hover">
                <div class="course-info">
                  <div class="course-name">{{ course.name }}</div>
                  <div class="course-details">
                    <span><el-icon><User /></el-icon> {{ course.teacher }}</span>
                    <span><el-icon><OfficeBuilding /></el-icon> {{ course.location }}</span>
                  </div>
                </div>
              </el-card>
            </div>
          </div>
        </el-card>

        <!-- 成绩展示 -->
        <el-card class="recent-grades">
          <template #header>
            <div class="card-header">
              <span>最近成绩</span>
              <el-button type="primary" link @click="goToGrades">查看所有成绩</el-button>
            </div>
          </template>
          <div v-if="recentGrades.length === 0" class="empty-grades">
            <el-empty description="暂无成绩数据"></el-empty>
          </div>
          <div v-else class="grades-table">
            <el-table :data="recentGrades" stripe style="width: 100%">
              <el-table-column prop="courseName" label="课程名称" />
              <el-table-column prop="score" label="成绩">
                <template #default="scope">
                  <span :class="getScoreClass(scope.row.score)">{{ scope.row.score }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="credits" label="学分" width="80" />
              <el-table-column prop="semester" label="学期" width="150" />
            </el-table>
          </div>
        </el-card>
      </div>

      <!-- 右侧边栏 -->
      <div class="dashboard-sidebar">
        <!-- 通知公告 -->
        <el-card class="notifications">
          <template #header>
            <div class="card-header">
              <span>通知公告</span>
              <el-button type="primary" link>查看全部</el-button>
            </div>
          </template>
          <div v-if="notifications.length === 0" class="empty-notifications">
            <el-empty description="暂无通知"></el-empty>
          </div>
          <div v-else class="notification-list">
            <div v-for="(notification, index) in notifications" :key="index" class="notification-item">
              <div class="notification-title">
                <span :class="{'is-new': notification.isNew}">{{ notification.title }}</span>
                <el-tag v-if="notification.isNew" size="small" type="danger">新</el-tag>
              </div>
              <div class="notification-date">{{ notification.date }}</div>
            </div>
          </div>
        </el-card>

        <!-- 个人信息卡片 -->
        <el-card class="user-profile">
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
              <el-button type="primary" link @click="goToProfile">完善资料</el-button>
            </div>
          </template>
          <div class="profile-list">
            <div class="profile-item">
              <span class="label">学号:</span>
              <span class="value">{{ userInfo.studentId || 'S12345678' }}</span>
            </div>
            <div class="profile-item">
              <span class="label">学院:</span>
              <span class="value">{{ userInfo.department || '计算机学院' }}</span>
            </div>
            <div class="profile-item">
              <span class="label">专业:</span>
              <span class="value">{{ userInfo.major || '软件工程' }}</span>
            </div>
            <div class="profile-item">
              <span class="label">班级:</span>
              <span class="value">{{ userInfo.className || '软工2班' }}</span>
            </div>
            <div class="profile-item">
              <span class="label">邮箱:</span>
              <span class="value">{{ userInfo.email || 'student@example.com' }}</span>
            </div>
          </div>
        </el-card>

        <!-- 学习日历 -->
        <el-card class="calendar-card">
          <template #header>
            <div class="card-header">
              <span>学习日历</span>
            </div>
          </template>
          <el-calendar v-model="currentDate">
            <template #dateCell="{ data }">
              <div class="calendar-cell">
                <div>{{ data.day.split('-').slice(1).join('-') }}</div>
                <div v-if="hasEventOnDay(data.day)" class="calendar-indicator"></div>
              </div>
            </template>
          </el-calendar>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Calendar, Collection, Reading, Document, User, OfficeBuilding, Back, SwitchButton } from '@element-plus/icons-vue'
import { getCourses, getGrades } from '../api'
import http from '@/utils/http'

const router = useRouter()
const currentDate = ref(new Date())

// 用户信息
interface UserInfo {
  id?: number;
  username: string;
  studentId: string;
  department: string;
  major: string;
  className: string;
  email: string;
}

// API返回的用户数据接口
interface UserResponse {
  id?: number;
  username?: string;
  studentId?: string;
  department?: string;
  major?: string;
  className?: string;
  email?: string;
}

interface CourseSchedule {
  day: number;
  startTime: string;
  endTime: string;
  classroom: string;
}

interface Course {
  id: number;
  courseId: string;
  courseName: string;
  teacherName: string;
  credits: number;
  completed: boolean;
  schedules: CourseSchedule[];
}

interface Grade {
  id: number;
  courseName: string;
  score: number;
  credits: number;
  semester: string;
  updateTime: string;
}

interface Notification {
  id: number;
  title: string;
  publishTime: string;
  read: boolean;
}

const userInfo = ref<UserInfo>({
  username: '李小明',
  studentId: 'S20240001',
  department: '计算机学院',
  major: '软件工程',
  className: '软工2班',
  email: 'xiaoming@example.com'
})

// 统计数据
const todayCourseCount = ref(2)
const completedCredits = ref(48)
const selectedCourseCount = ref(6)
const averageGrade = ref(89.5)

// 今日课程
interface TodayCourse {
  name: string;
  teacher: string;
  location: string;
  time: string;
}

const todayCourses = ref<TodayCourse[]>([
  {
    name: '高等数学',
    teacher: '王教授',
    location: '教学楼A-101',
    time: '08:00 - 09:40'
  },
  {
    name: '数据结构',
    teacher: '李教授',
    location: '教学楼B-203',
    time: '14:00 - 15:40'
  }
])

// 最近成绩
const recentGrades = ref([
  {
    courseName: '计算机网络',
    score: 92,
    credits: 3,
    semester: '2023-2024学年第二学期'
  },
  {
    courseName: '操作系统',
    score: 87,
    credits: 4,
    semester: '2023-2024学年第二学期'
  },
  {
    courseName: '软件工程',
    score: 95,
    credits: 3,
    semester: '2023-2024学年第二学期'
  }
])

// 通知公告
const notifications = ref([
  {
    title: '关于2024-2025学年第一学期选课的通知',
    date: '2024-03-28',
    isNew: true
  },
  {
    title: '学生证办理通知',
    date: '2024-03-25',
    isNew: false
  },
  {
    title: '关于组织参加科技创新大赛的通知',
    date: '2024-03-20',
    isNew: false
  },
  {
    title: '期中考试安排',
    date: '2024-03-15',
    isNew: false
  }
])

// 计算日期
const todayDate = computed(() => {
  const date = new Date()
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日 ${getWeekday(date.getDay())}`
})

// 学期信息
const semesterInfo = ref('2023-2024学年第二学期')

// 问候语
const getGreeting = () => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 17) return '下午好'
  if (hour < 19) return '傍晚好'
  return '晚上好'
}

// 获取星期
const getWeekday = (day: number) => {
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return weekdays[day]
}

// 成绩样式
const getScoreClass = (score: number) => {
  if (score >= 90) return 'score-excellent'
  if (score >= 80) return 'score-good'
  if (score >= 60) return 'score-pass'
  return 'score-fail'
}

// 检查日期是否有事件
const hasEventOnDay = (day: string) => {
  // 模拟一些有事件的日期
  const eventDays = [
    '2024-04-01',
    '2024-04-05',
    '2024-04-10',
    '2024-04-15',
    '2024-04-20'
  ]
  return eventDays.includes(day)
}

// 页面跳转
const goToSchedule = () => {
  router.push('/student/schedule')
}

const goToGrades = () => {
  router.push('/student/grades')
}

const goToProfile = () => {
  router.push('/profile')
}

const goToHome = () => {
  router.push('/')
}

// 获取用户数据
const fetchUserData = async () => {
  try {
    // 获取用户信息
    const response = await http.get('/user/current')
    if (response && response.data) {
      const userData = response.data as UserResponse;
      userInfo.value = {
        username: userData.username || '未知用户',
        studentId: userData.studentId || '未知学号',
        department: userData.department || '未知学院',
        major: userData.major || '未知专业',
        className: userData.className || '未知班级',
        email: userData.email || '未知邮箱',
        id: userData.id
      }
      console.log('获取到用户数据:', userInfo.value)
      
      // 获取相关课程信息
      fetchCoursesData()
      
      // 获取相关成绩信息
      fetchGradesData()
    }
  } catch (error) {
    console.error('获取用户数据失败:', error)
    ElMessage.error('获取用户数据失败，使用默认数据')
    // 使用默认数据
    userInfo.value = {
      username: '李小明',
      studentId: 'S20240001',
      department: '计算机学院',
      major: '软件工程',
      className: '软工2班',
      email: 'xiaoming@example.com'
    }
  }
}

// 获取课程数据
const fetchCoursesData = async () => {
  try {
    const userId = userInfo.value.id
    if (!userId) return
    
    const response = await getCourses({ userId })
    if (response) {
      // 更新课程相关统计数据
      selectedCourseCount.value = Array.isArray(response) ? response.length : 0
      
      // 计算学分
      if (Array.isArray(response)) {
        completedCredits.value = response.reduce((total: number, course: Course) => {
          return total + (course.completed ? (course.credits || 0) : 0)
        }, 0)
        
        // 获取今日课程
        const today = new Date().getDay() || 7 // 转换为1-7表示周一到周日
        const todayCourseList = response.filter((course: Course) => {
          return course.schedules && course.schedules.some(schedule => schedule.day === today)
        })
        
        todayCourseCount.value = todayCourseList.length
        
        // 格式化今日课程数据
        todayCourses.value = todayCourseList.map((course: Course) => {
          const schedule = course.schedules.find((s: CourseSchedule) => s.day === today)
          return {
            name: course.courseName,
            teacher: course.teacherName,
            location: schedule?.classroom || '未设置',
            time: formatTimeSlot(schedule?.startTime, schedule?.endTime) || '未设置时间'
          }
        }).sort((a: TodayCourse, b: TodayCourse) => {
          // 按时间排序
          return a.time.localeCompare(b.time)
        })
      }
    }
  } catch (error) {
    console.error('获取课程数据失败:', error)
    // 使用默认数据
    selectedCourseCount.value = 6
    completedCredits.value = 48
    todayCourseCount.value = 2
  }
}

// 格式化时间段
const formatTimeSlot = (start: string | undefined, end: string | undefined): string => {
  if (!start || !end) return ''
  return `${start} - ${end}`
}

// 获取成绩数据
const fetchGradesData = async () => {
  try {
    const userId = userInfo.value.id
    if (!userId) return
    
    const response = await getGrades({ userId })
    if (response) {
      // 计算平均分
      if (Array.isArray(response)) {
        const grades = response.filter((grade: Grade) => grade.score !== null && grade.score !== undefined)
        
        if (grades.length > 0) {
          const sum = grades.reduce((total: number, grade: Grade) => total + grade.score, 0)
          averageGrade.value = Number((sum / grades.length).toFixed(1))
        }
        
        // 最近成绩
        recentGrades.value = response
          .sort((a: Grade, b: Grade) => new Date(b.updateTime).getTime() - new Date(a.updateTime).getTime())
          .slice(0, 5)
          .map((grade: Grade) => ({
            courseName: grade.courseName,
            score: grade.score,
            credits: grade.credits,
            semester: grade.semester
          }))
      }
    }
  } catch (error) {
    console.error('获取成绩数据失败:', error)
    // 使用默认数据
    averageGrade.value = 89.5
  }
}

// 获取通知数据
const fetchNotifications = async () => {
  try {
    const response = await http.get('/notification/list')
    if (response && response.data) {
      const notificationsData = Array.isArray(response.data) ? response.data : []
      notifications.value = notificationsData.slice(0, 5).map((notice: Notification) => ({
        title: notice.title,
        date: formatDate(notice.publishTime),
        isNew: !notice.read
      }))
    }
  } catch (error) {
    console.error('获取通知失败:', error)
    // 使用默认通知数据
    notifications.value = [
      {
        title: '关于2024-2025学年第一学期选课的通知',
        date: '2024-03-28',
        isNew: true
      },
      {
        title: '学生证办理通知',
        date: '2024-03-25',
        isNew: false
      },
      {
        title: '关于组织参加科技创新大赛的通知',
        date: '2024-03-20',
        isNew: false
      },
      {
        title: '期中考试安排',
        date: '2024-03-15',
        isNew: false
      }
    ]
  }
}

// 格式化日期
const formatDate = (dateString: string): string => {
  if (!dateString) return ''
  
  const date = new Date(dateString)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

// 登出功能
const logout = () => {
  ElMessage.success('退出登录成功')
  // 清除本地存储的令牌和用户信息
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  // 跳转到登录页面
  router.push('/login')
}

onMounted(() => {
  fetchUserData()
  fetchNotifications()
})
</script>

<style scoped>
.student-dashboard {
  padding: 20px;
}

.dashboard-title {
  margin-bottom: 20px;
  color: #303133;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dashboard-actions {
  display: flex;
  gap: 10px;
}

.welcome-card {
  margin-bottom: 20px;
  background: linear-gradient(to right, #3a8ee6, #67c23a);
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: white;
}

.welcome-text h3 {
  margin-top: 0;
  margin-bottom: 8px;
  font-size: 24px;
}

.welcome-text p {
  margin-bottom: 16px;
  opacity: 0.9;
}

.date-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.dashboard-row {
  display: flex;
  gap: 20px;
}

.dashboard-main {
  flex: 3;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.dashboard-sidebar {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
}

.stat-card {
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  margin-top: 5px;
}

.stat-desc {
  color: #909399;
  font-size: 14px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.today-courses {
  margin-bottom: 20px;
}

.course-timeline {
  padding: 10px 0;
}

.timeline-item {
  display: flex;
  margin-bottom: 15px;
  align-items: flex-start;
}

.timeline-time {
  min-width: 100px;
  text-align: right;
  padding-right: 20px;
  color: #606266;
  padding-top: 12px;
}

.timeline-content {
  flex: 1;
  position: relative;
  transition: transform 0.2s;
}

.timeline-content:hover {
  transform: translateX(5px);
}

.course-info {
  padding: 5px 0;
}

.course-name {
  font-weight: bold;
  margin-bottom: 5px;
  color: #303133;
}

.course-details {
  display: flex;
  gap: 20px;
  color: #606266;
  font-size: 14px;
}

.course-details span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.empty-courses, .empty-grades, .empty-notifications {
  padding: 20px 0;
}

.recent-grades {
  margin-bottom: 20px;
}

.score-excellent {
  color: #67C23A;
  font-weight: bold;
}

.score-good {
  color: #409EFF;
  font-weight: bold;
}

.score-pass {
  color: #E6A23C;
}

.score-fail {
  color: #F56C6C;
  font-weight: bold;
}

.notifications {
  margin-bottom: 20px;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item {
  padding: 10px;
  border-bottom: 1px solid #EBEEF5;
  cursor: pointer;
  transition: background-color 0.2s;
}

.notification-item:hover {
  background-color: #F5F7FA;
}

.notification-title {
  margin-bottom: 5px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.is-new {
  font-weight: bold;
  color: #303133;
}

.notification-date {
  font-size: 12px;
  color: #909399;
}

.user-profile {
  margin-bottom: 20px;
}

.profile-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.profile-item {
  display: flex;
  align-items: center;
}

.profile-item .label {
  width: 60px;
  color: #909399;
}

.profile-item .value {
  color: #303133;
}

.calendar-card {
  margin-bottom: 20px;
}

.calendar-cell {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
}

.calendar-indicator {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #409EFF;
  margin-top: 2px;
}

@media (max-width: 1200px) {
  .dashboard-row {
    flex-direction: column;
  }
  
  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stat-cards {
    grid-template-columns: 1fr;
  }
  
  .timeline-item {
    flex-direction: column;
  }
  
  .timeline-time {
    text-align: left;
    padding-right: 0;
    padding-bottom: 5px;
  }
}
</style> 