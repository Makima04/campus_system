<template>
  <div class="notification-list-container">
    <div class="page-header">
      <h2>通知管理</h2>
      <el-button type="primary" @click="goToPublish">
        <el-icon><Plus /></el-icon>
        发布新通知
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="通知标题">
          <el-input v-model="searchForm.title" placeholder="请输入标题关键词" clearable></el-input>
        </el-form-item>
        <el-form-item label="通知类型">
          <el-select v-model="searchForm.type" placeholder="请选择类型" clearable>
            <el-option v-for="item in noticeTypes" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="通知状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><RefreshRight /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 通知列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="notificationList"
        style="width: 100%"
        stripe
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.type)">{{ getTypeLabel(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="范围" width="100">
          <template #default="scope">
            {{ getLevelLabel(scope.row.level) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isPinned" label="置顶" width="80" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.isPinned" type="danger" effect="dark">置顶</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="160">
          <template #default="scope">
            {{ formatDateTime(scope.row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-button type="primary" size="small" @click="viewNotification(scope.row)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button 
                v-if="scope.row.status === '草稿'"
                type="success" 
                size="small" 
                @click="editNotification(scope.row)"
              >
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button
                v-if="scope.row.status === '草稿'"
                type="warning"
                size="small"
                @click="publishNotification(scope.row.id)"
              >
                <el-icon><Promotion /></el-icon>
                发布
              </el-button>
              <el-button
                v-if="scope.row.status === '已发布'"
                type="warning"
                size="small"
                @click="withdrawNotification(scope.row.id)"
              >
                <el-icon><TurnOff /></el-icon>
                撤回
              </el-button>
              <el-button
                v-if="scope.row.status === '已发布'"
                type="info"
                size="small"
                @click="togglePinStatus(scope.row)"
              >
                <el-icon><Top /></el-icon>
                {{ scope.row.isPinned ? '取消置顶' : '置顶' }}
              </el-button>
              <el-button
                v-if="scope.row.status !== '已发布'"
                type="danger"
                size="small"
                @click="deleteNotification(scope.row.id)"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </el-button-group>
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

    <!-- 通知详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="60%"
      destroy-on-close
    >
      <div v-if="currentNotification" class="notification-detail">
        <div class="notification-header">
          <h3>{{ currentNotification.title }}</h3>
          <div class="notification-meta">
            <span class="notification-type">
              <el-tag :type="getTypeTag(currentNotification.type)">{{ getTypeLabel(currentNotification.type) }}</el-tag>
            </span>
            <span class="notification-level">
              范围: {{ getLevelLabel(currentNotification.level) }}
            </span>
            <span v-if="currentNotification.targetDepartment">
              目标院系: {{ currentNotification.targetDepartment }}
            </span>
            <span class="notification-date">
              发布时间: {{ formatDateTime(currentNotification.publishTime) }}
            </span>
          </div>
        </div>
        <el-divider />
        <div class="notification-content" v-html="currentNotification.content"></div>
        <div v-if="currentNotification.attachmentUrl" class="notification-attachment">
          <el-divider />
          <div class="attachment-title">附件:</div>
          <el-link :href="currentNotification.attachmentUrl" target="_blank" type="primary">
            <el-icon><Document /></el-icon>
            {{ getAttachmentName(currentNotification.attachmentUrl) }}
          </el-link>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, 
  Search, 
  Edit, 
  Delete, 
  View, 
  RefreshRight, 
  Promotion, 
  TurnOff, 
  Top, 
  Document 
} from '@element-plus/icons-vue'
import api from '@/api/index'

const router = useRouter()

// 通知类型选项
const noticeTypes = [
  { label: '普通通知', value: '普通' },
  { label: '紧急通知', value: '紧急' },
  { label: '活动通知', value: '活动' }
]

// 通知状态选项
const statusOptions = [
  { label: '草稿', value: '草稿' },
  { label: '已发布', value: '已发布' },
  { label: '已撤回', value: '已撤回' }
]

// 通知范围选项
const levelOptions = [
  { label: '全校通知', value: '全校' },
  { label: '院系通知', value: '院系' },
  { label: '班级通知', value: '班级' }
]

// 搜索表单
const searchForm = reactive({
  title: '',
  type: '',
  status: ''
})

// 列表数据和分页
const loading = ref(false)
const notificationList = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 详情对话框
const dialogVisible = ref(false)
const dialogTitle = ref('通知详情')
const currentNotification = ref<any>(null)

// 获取通知列表
const fetchNotifications = async () => {
  try {
    loading.value = true
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      title: searchForm.title || undefined,
      type: searchForm.type || undefined,
      status: searchForm.status || undefined
    }
    
    const response = await api.get('/notification/admin/list', { params })
    notificationList.value = response.data.content || []
    total.value = response.data.totalElements || 0
  } catch (error) {
    console.error('获取通知列表失败:', error)
    ElMessage.error('获取通知列表失败')
    // 使用模拟数据用于演示
    notificationList.value = generateMockData()
    total.value = notificationList.value.length
  } finally {
    loading.value = false
  }
}

// 生成模拟数据
const generateMockData = () => {
  return [
    {
      id: 1,
      title: '关于2025学年第一学期选课的通知',
      content: '<p>各位同学：</p><p>2025学年第一学期选课将于4月15日开始，请各位同学及时登录选课系统进行选课操作。</p>',
      type: '普通',
      level: '全校',
      targetDepartment: null,
      publishTime: '2025-03-30T10:00:00',
      status: '已发布',
      isPinned: true,
      attachmentUrl: 'https://example.com/files/选课指南.pdf'
    },
    {
      id: 2,
      title: '计算机学院学生会招新通知',
      content: '<p>计算机学院学生会现面向2024级新生招募新成员，有意向者请于4月20日前提交申请。</p>',
      type: '活动',
      level: '院系',
      targetDepartment: '计算机学院',
      publishTime: '2025-03-28T14:30:00',
      status: '已发布',
      isPinned: false,
      attachmentUrl: null
    },
    {
      id: 3,
      title: '关于校园网络系统升级的紧急通知',
      content: '<p>紧急通知：校园网络系统将于4月2日凌晨2:00-5:00进行升级维护，期间网络服务将不可用。</p>',
      type: '紧急',
      level: '全校',
      targetDepartment: null,
      publishTime: '2025-03-31T16:00:00',
      status: '已发布',
      isPinned: true,
      attachmentUrl: null
    },
    {
      id: 4,
      title: '军训通知（草稿）',
      content: '<p>2025级新生军训将于9月1日-9月15日举行，请做好准备。</p>',
      type: '普通',
      level: '全校',
      targetDepartment: null,
      publishTime: null,
      status: '草稿',
      isPinned: false,
      attachmentUrl: null
    }
  ]
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchNotifications()
}

// 重置搜索
const resetSearch = () => {
  searchForm.title = ''
  searchForm.type = ''
  searchForm.status = ''
  currentPage.value = 1
  fetchNotifications()
}

// 格式化日期时间
const formatDateTime = (dateTime: string): string => {
  if (!dateTime) return '-'
  
  const date = new Date(dateTime)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取通知类型标签样式
const getTypeTag = (type: string): string => {
  switch (type) {
    case '紧急': return 'danger'
    case '活动': return 'success'
    default: return 'info'
  }
}

// 获取通知类型标签文本
const getTypeLabel = (type: string): string => {
  return type || '普通'
}

// 获取通知范围标签文本
const getLevelLabel = (level: string): string => {
  return level || '全校'
}

// 获取状态标签样式
const getStatusTag = (status: string): string => {
  switch (status) {
    case '已发布': return 'success'
    case '草稿': return 'info'
    case '已撤回': return 'warning'
    default: return ''
  }
}

// 获取状态标签文本
const getStatusLabel = (status: string): string => {
  return status || '草稿'
}

// 获取附件名称
const getAttachmentName = (url: string): string => {
  if (!url) return ''
  
  // 从URL中提取文件名
  const parts = url.split('/')
  return decodeURIComponent(parts[parts.length - 1])
}

// 查看通知详情
const viewNotification = (notification) => {
  currentNotification.value = notification
  dialogTitle.value = '通知详情'
  dialogVisible.value = true
}

// 编辑通知
const editNotification = (notification) => {
  router.push(`/notification/edit/${notification.id}`)
}

// 发布通知
const publishNotification = async (id) => {
  try {
    await ElMessageBox.confirm('确定要发布此通知吗？发布后将无法修改内容', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await api.post(`/notification/${id}/publish`)
    ElMessage.success('通知发布成功')
    fetchNotifications()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布通知失败:', error)
      ElMessage.error('发布通知失败')
    }
  }
}

// 撤回通知
const withdrawNotification = async (id) => {
  try {
    await ElMessageBox.confirm('确定要撤回此通知吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await api.post(`/notification/${id}/withdraw`)
    ElMessage.success('通知撤回成功')
    fetchNotifications()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤回通知失败:', error)
      ElMessage.error('撤回通知失败')
    }
  }
}

// 切换置顶状态
const togglePinStatus = async (notification) => {
  try {
    const message = notification.isPinned ? '确定要取消置顶此通知吗？' : '确定要置顶此通知吗？'
    await ElMessageBox.confirm(message, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await api.post(`/notification/${notification.id}/toggle-pin`)
    ElMessage.success(notification.isPinned ? '取消置顶成功' : '置顶成功')
    fetchNotifications()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('切换置顶状态失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 删除通知
const deleteNotification = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除此通知吗？此操作不可恢复', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    await api.delete(`/notification/${id}`)
    ElMessage.success('通知删除成功')
    fetchNotifications()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除通知失败:', error)
      ElMessage.error('删除通知失败')
    }
  }
}

// 跳转到发布通知页面
const goToPublish = () => {
  router.push('/notification/publish')
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchNotifications()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchNotifications()
}

onMounted(() => {
  fetchNotifications()
})
</script>

<style scoped>
.notification-list-container {
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
  color: #303133;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.notification-detail {
  padding: 10px;
}

.notification-header {
  margin-bottom: 15px;
}

.notification-header h3 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #303133;
}

.notification-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  font-size: 14px;
  color: #606266;
}

.notification-content {
  line-height: 1.6;
  color: #303133;
  min-height: 200px;
}

.notification-attachment {
  padding-top: 10px;
}

.attachment-title {
  font-weight: bold;
  margin-bottom: 5px;
}
</style> 