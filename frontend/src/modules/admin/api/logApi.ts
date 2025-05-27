import axios from 'axios'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../../utils/http' // 导入配置好的http实例

// 基础URL
const API_URL = '/api/system/logs'

/**
 * 日志API服务
 */
export default {
  /**
   * 分页查询日志
   * @param page 页码
   * @param size 每页大小
   * @param params 查询参数
   */
  getLogPage(page = 0, size = 10, params = {}) {
    const loading = ref(true)
    const logs = ref([])
    const total = ref(0)
    const error = ref(null)

    const fetchLogs = async () => {
      loading.value = true
      try {
        const queryParams = {
          page,
          size,
          ...params
        }
        
        const response = await http.get(API_URL, { params: queryParams })
        logs.value = response.data.data.content
        total.value = response.data.data.totalElements
        return response.data.data
      } catch (err) {
        error.value = err
        ElMessage.error('获取日志列表失败')
        console.error('获取日志列表失败:', err)
        return null
      } finally {
        loading.value = false
      }
    }

    return {
      loading,
      logs,
      total,
      error,
      fetchLogs
    }
  },

  /**
   * 获取日志详情
   * @param id 日志ID
   */
  getLogDetail(id) {
    const loading = ref(true)
    const log = ref(null)
    const error = ref(null)

    const fetchLogDetail = async () => {
      loading.value = true
      try {
        const response = await http.get(`${API_URL}/${id}`)
        log.value = response.data.data
        return response.data.data
      } catch (err) {
        error.value = err
        ElMessage.error('获取日志详情失败')
        console.error('获取日志详情失败:', err)
        return null
      } finally {
        loading.value = false
      }
    }

    return {
      loading,
      log,
      error,
      fetchLogDetail
    }
  },

  /**
   * 查询归档日志
   * @param page 页码
   * @param size 每页大小
   */
  getArchivedLogs(page = 0, size = 10) {
    const loading = ref(true)
    const logs = ref([])
    const total = ref(0)
    const error = ref(null)

    const fetchArchivedLogs = async () => {
      loading.value = true
      try {
        const response = await http.get(`${API_URL}/archived`, {
          params: { page, size }
        })
        logs.value = response.data.data.content
        total.value = response.data.data.totalElements
        return response.data.data
      } catch (err) {
        error.value = err
        ElMessage.error('获取归档日志失败')
        console.error('获取归档日志失败:', err)
        return null
      } finally {
        loading.value = false
      }
    }

    return {
      loading,
      logs,
      total,
      error,
      fetchArchivedLogs
    }
  }
} 