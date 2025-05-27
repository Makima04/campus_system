<template>
  <div class="log-management-container">
    <el-card class="log-card">
      <template #header>
        <div class="card-header">
          <h2>系统日志管理</h2>
          <div class="header-actions">
            <el-button type="primary" @click="refreshLogs">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索条件 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchParams.username" placeholder="请输入用户名" clearable @clear="handleSearch" />
        </el-form-item>
        <el-form-item label="模块">
          <el-input v-model="searchParams.module" placeholder="请输入模块名称" clearable @clear="handleSearch" />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
            clearable
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchParams.status" placeholder="请选择状态" clearable @clear="handleSearch">
            <el-option :value="0" label="正常" />
            <el-option :value="1" label="异常" />
            <el-option :value="2" label="已归档" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 日志表格 -->
      <el-table
        v-loading="loading"
        :data="logs"
        border
        stripe
        style="width: 100%"
        height="calc(100vh - 300px)"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="type" label="操作类型" width="100">
          <template #default="scope">
            <el-tag :type="getOperationTypeTag(scope.row.type)">{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="操作描述" width="180" show-overflow-tooltip />
        <el-table-column prop="username" label="操作人" width="120" />
        <el-table-column prop="requestMethod" label="请求方式" width="100" />
        <el-table-column prop="requestUrl" label="请求URL" width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时(ms)" width="100" />
        <el-table-column prop="createTime" label="操作时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button type="text" @click="viewLogDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="日志详情"
      width="70%"
      append-to-body
      destroy-on-close
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="ID">{{ currentLog.id }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ currentLog.module }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="getOperationTypeTag(currentLog.type)">{{ currentLog.type }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作描述">{{ currentLog.description }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ currentLog.method }}</el-descriptions-item>
        <el-descriptions-item label="请求URL">{{ currentLog.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ currentLog.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="请求IP">{{ currentLog.requestIp }}</el-descriptions-item>
        <el-descriptions-item label="请求参数">
          <pre>{{ formatJson(currentLog.requestParam) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="请求结果">
          <pre>{{ formatJson(currentLog.result) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="操作人ID">{{ currentLog.userId }}</el-descriptions-item>
        <el-descriptions-item label="操作人用户名">{{ currentLog.username }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTag(currentLog.status)">
            {{ getStatusText(currentLog.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentLog.status === 1" label="错误消息">
          <pre>{{ currentLog.errorMsg }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="操作耗时">{{ currentLog.costTime }} ms</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ currentLog.createTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import logApi from '../api/logApi'

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 日志列表
const logs = ref([])
const loading = ref(false)

// 搜索参数
const searchParams = reactive({
  username: '',
  module: '',
  status: null,
  startTime: null,
  endTime: null
})

// 日期范围
const dateRange = ref([])

// 日志详情
const dialogVisible = ref(false)
const currentLog = ref({})

// 初始化
onMounted(() => {
  fetchLogs()
})

// 获取日志列表
const fetchLogs = async () => {
  const params = {
    ...searchParams
  }
  
  if (dateRange.value && dateRange.value.length === 2) {
    params.startTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
  }
  
  loading.value = true
  const { fetchLogs } = logApi.getLogPage(currentPage.value - 1, pageSize.value, params)
  const result = await fetchLogs()
  
  if (result) {
    logs.value = result.content
    total.value = result.totalElements
  }
  
  loading.value = false
}

// 刷新日志
const refreshLogs = () => {
  fetchLogs()
}

// 查询
const handleSearch = () => {
  currentPage.value = 1
  fetchLogs()
}

// 重置搜索条件
const resetSearch = () => {
  searchParams.username = ''
  searchParams.module = ''
  searchParams.status = null
  dateRange.value = []
  currentPage.value = 1
  fetchLogs()
}

// 页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchLogs()
}

// 每页条数变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchLogs()
}

// 查看日志详情
const viewLogDetail = async (log) => {
  currentLog.value = log
  dialogVisible.value = true
  
  // 如果需要获取更详细的信息，可以调用详情API
  if (log.id) {
    const { fetchLogDetail } = logApi.getLogDetail(log.id)
    const detail = await fetchLogDetail()
    if (detail) {
      currentLog.value = detail
    }
  }
}

// 格式化JSON
const formatJson = (jsonStr) => {
  if (!jsonStr) return ''
  try {
    const obj = typeof jsonStr === 'string' ? JSON.parse(jsonStr) : jsonStr
    return JSON.stringify(obj, null, 2)
  } catch (e) {
    return jsonStr
  }
}

// 获取操作类型标签样式
const getOperationTypeTag = (type) => {
  const typeMap = {
    'QUERY': 'info',
    'CREATE': 'success',
    'UPDATE': 'warning',
    'DELETE': 'danger',
    'EXPORT': 'info',
    'IMPORT': 'info',
    'LOGIN': 'success',
    'LOGOUT': 'info'
  }
  return typeMap[type] || 'info'
}

// 获取状态标签样式
const getStatusTag = (status) => {
  const statusMap = {
    0: 'success',
    1: 'danger',
    2: 'info'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '正常',
    1: '异常',
    2: '已归档'
  }
  return statusMap[status] || '未知'
}
</script>

<style scoped>
.log-management-container {
  padding: 20px;
}

.log-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

pre {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 300px;
  overflow-y: auto;
}
</style> 