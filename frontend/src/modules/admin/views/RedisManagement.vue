<template>
  <div class="redis-management">
    <div class="header">
      <h1>Redis缓存管理</h1>
      <div class="action-buttons">
        <el-button type="primary" @click="refreshKeys">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="danger" @click="confirmFlushAll">
          <el-icon><Delete /></el-icon>
          清空缓存
        </el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab">
      <!-- 基本管理 -->
      <el-tab-pane label="基本管理" name="basic">
        <el-row :gutter="16">
          <el-col :span="16">
            <!-- 键列表 -->
            <el-card class="keys-card">
              <template #header>
                <div class="card-header">
                  <span>缓存键列表</span>
                  <el-input 
                    v-model="keyFilter" 
                    placeholder="搜索键名" 
                    class="key-search"
                    clearable
                    style="width: 200px"
                  ></el-input>
                </div>
              </template>
              <el-table
                :data="filteredKeys"
                style="width: 100%"
                v-loading="loading"
                @row-click="onSelectKey"
              >
                <el-table-column prop="key" label="键名" show-overflow-tooltip />
                <el-table-column prop="type" label="类型" width="100" />
                <el-table-column label="过期时间" width="150">
                  <template #default="scope">
                    <span v-if="scope.row.ttl === -1">永不过期</span>
                    <span v-else-if="scope.row.ttl >= 0">{{ formatTTL(scope.row.ttl) }}</span>
                    <span v-else>已过期</span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="120">
                  <template #default="scope">
                    <el-button type="primary" link @click.stop="getKeyValue(scope.row.key)">
                      <el-icon><View /></el-icon>
                    </el-button>
                    <el-button type="danger" link @click.stop="confirmDeleteKey(scope.row.key)">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <div v-if="filteredKeys.length === 0 && !loading" class="no-keys">
                <el-empty description="暂无缓存数据"></el-empty>
              </div>
            </el-card>
          </el-col>

          <el-col :span="8">
            <!-- 新增/编辑键值 -->
            <el-card class="edit-card">
              <template #header>
                <div class="card-header">
                  <span>添加/修改缓存</span>
                </div>
              </template>
              <el-form :model="formState" :rules="rules" ref="formRef" label-position="top">
                <el-form-item label="键名" prop="key">
                  <el-input v-model="formState.key" placeholder="请输入键名" />
                </el-form-item>
                <el-form-item label="值" prop="value">
                  <el-input
                    v-model="formState.value"
                    type="textarea"
                    placeholder="请输入键值"
                    :rows="4"
                  />
                </el-form-item>
                <el-form-item label="过期时间(秒)" prop="ttl">
                  <el-input-number
                    v-model="formState.ttl"
                    :min="0"
                    placeholder="0表示永不过期"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="submitForm" :loading="submitting">
                    保存
                  </el-button>
                  <el-button style="margin-left: 10px" @click="resetForm">
                    重置
                  </el-button>
                </el-form-item>
              </el-form>
            </el-card>

            <!-- 键值详情 -->
            <el-card class="value-card" v-if="selectedKey">
              <template #header>
                <div class="card-header">
                  <span>键值详情</span>
                </div>
              </template>
              <div class="key-details">
                <p><strong>键名:</strong> {{ selectedKey }}</p>
                <p v-if="selectedKeyTTL !== null">
                  <strong>过期时间:</strong>
                  <span v-if="selectedKeyTTL === -1">永不过期</span>
                  <span v-else-if="selectedKeyTTL >= 0">{{ formatTTL(selectedKeyTTL) }}</span>
                  <span v-else>键不存在</span>
                </p>
              </div>
              <div class="key-value">
                <strong>值:</strong>
                <pre>{{ displayValue }}</pre>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- 缓存击穿测试 -->
      <el-tab-pane label="缓存击穿测试" name="breakdown">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>缓存击穿测试</span>
            </div>
          </template>
          <div class="test-section">
            <p>缓存击穿是指<strong>热点数据的key在失效的瞬间</strong>，大量并发请求涌入，导致所有请求都直接访问数据库。</p>
            <p>本测试通过高并发请求同一个用户ID，测试系统是否能够防止缓存击穿问题。</p>
            <el-alert
              title="已实现防御机制：分布式锁 + 双重检查 + 随机过期时间"
              type="success"
              :closable="false"
              style="margin-bottom: 15px"
            />
            <el-form label-width="100px">
              <el-form-item label="用户ID">
                <el-input-number v-model="breakdownId" :min="1" placeholder="输入用户ID"></el-input-number>
              </el-form-item>
              <el-form-item label="并发数">
                <el-input-number v-model="breakdownCount" :min="1" :max="20" placeholder="并发请求数"></el-input-number>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="testCacheBreakdown">测试</el-button>
              </el-form-item>
            </el-form>
            <div v-if="breakdownResults.length">
              <h4>测试结果:</h4>
              <pre>{{ breakdownResults.join('\n') }}</pre>
            </div>
          </div>
        </el-card>
      </el-tab-pane>
      
      <!-- 缓存穿透测试 - 准备中 -->
      <el-tab-pane label="缓存穿透测试 (未实现)" name="penetration">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>缓存穿透测试（后端未实现）</span>
            </div>
          </template>
          <div class="test-section">
            <p>缓存穿透是指查询一个<strong>不存在的数据</strong>，导致每次请求都要查询数据库，失去了缓存的意义。</p>
            <p>本测试尚未实现。推荐防御措施：布隆过滤器、空值缓存。</p>
            <el-alert
              title="此功能正在开发中，敬请期待"
              type="info"
              :closable="false"
              style="margin-bottom: 15px"
            />
          </div>
        </el-card>
      </el-tab-pane>
      
      <!-- 缓存雪崩测试 - 准备中 -->
      <el-tab-pane label="缓存雪崩测试 (未实现)" name="avalanche">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>缓存雪崩测试（后端未实现）</span>
            </div>
          </template>
          <div class="test-section">
            <p>缓存雪崩是指<strong>大量缓存同时过期</strong>，导致瞬时大量请求都访问数据库。</p>
            <p>本测试尚未实现。推荐防御措施：随机过期时间、缓存永不过期、服务熔断。</p>
            <el-alert
              title="此功能正在开发中，敬请期待"
              type="info"
              :closable="false"
              style="margin-bottom: 15px"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Refresh, Delete, View } from '@element-plus/icons-vue';
import axios from 'axios';
import { getApiBaseUrl } from '@/config';

// API端点
const API_ENDPOINTS = {
  getAllKeys: `${getApiBaseUrl()}/redis/keys`,
  getValue: `${getApiBaseUrl()}/redis/get`,
  setValue: `${getApiBaseUrl()}/redis/set`,
  deleteKey: `${getApiBaseUrl()}/redis/delete`,
  getTTL: `${getApiBaseUrl()}/redis/ttl`,
  flushAll: `${getApiBaseUrl()}/redis/flush`,
  testPenetration: `${getApiBaseUrl()}/redis/test/penetration`
};

// 状态定义
const keys = ref([]);
const loading = ref(false);
const submitting = ref(false);
const formRef = ref(null);
const selectedKey = ref(null);
const selectedKeyTTL = ref(null);
const displayValue = ref('');
const activeTab = ref('basic');
const keyFilter = ref('');

// 表单状态
const formState = reactive({
  key: '',
  value: '',
  ttl: 0
});

// 表单验证规则
const rules = {
  key: [{ required: true, message: '请输入键名', trigger: 'blur' }],
  value: [{ required: true, message: '请输入键值', trigger: 'blur' }]
};

// 缓存测试变量
const breakdownId = ref(1);
const breakdownCount = ref(10);
const breakdownResults = ref([]);

const penetrationId = ref(999999);
const penetrationResult = ref('');

const avalancheResults = ref([]);

// 过滤后的键列表
const filteredKeys = computed(() => {
  if (!keyFilter.value) {
    return keys.value;
  }
  
  const search = keyFilter.value.toLowerCase();
  return keys.value.filter((item) => 
    item.key.toLowerCase().includes(search)
  );
});

// 生命周期挂载
onMounted(() => {
  console.log('API基本URL:', getApiBaseUrl());
  console.log('缓存击穿测试完整URL:', API_ENDPOINTS.testPenetration);
  refreshKeys();
});

// 刷新键列表
const refreshKeys = async () => {
  loading.value = true;
  try {
    console.log('请求地址:', API_ENDPOINTS.getAllKeys);
    const response = await axios.get(API_ENDPOINTS.getAllKeys);
    if (response.data.success) {
      keys.value = response.data.data || [];
    } else {
      ElMessage.error(response.data.message || '获取缓存键失败');
    }
  } catch (error) {
    ElMessage.warning('获取缓存键失败，显示模拟数据');
    console.warn('获取缓存键API调用失败:', error);
    // 使用模拟数据
    keys.value = [
      { key: 'user:1', type: 'hash', ttl: 3600 },
      { key: 'session:123456', type: 'string', ttl: 1800 },
      { key: 'config:app', type: 'hash', ttl: -1 }
    ];
  } finally {
    loading.value = false;
  }
};

// 格式化TTL显示
const formatTTL = (seconds) => {
  if (seconds < 60) {
    return `${seconds}秒`;
  } else if (seconds < 3600) {
    return `${Math.floor(seconds / 60)}分${seconds % 60}秒`;
  } else {
    return `${Math.floor(seconds / 3600)}时${Math.floor((seconds % 3600) / 60)}分`;
  }
};

// 选择一个键
const onSelectKey = (row) => {
  getKeyValue(row.key);
};

// 获取键值
const getKeyValue = async (key) => {
  selectedKey.value = key;
  try {
    // 获取值
    const valueResponse = await axios.get(`${API_ENDPOINTS.getValue}?key=${encodeURIComponent(key)}`);
    if (valueResponse.data.success) {
      const value = valueResponse.data.data;
      displayValue.value = typeof value === 'object' ? JSON.stringify(value, null, 2) : value;
    } else {
      displayValue.value = '无法获取值: ' + valueResponse.data.message;
    }

    // 获取TTL
    const ttlResponse = await axios.get(`${API_ENDPOINTS.getTTL}?key=${encodeURIComponent(key)}`);
    if (ttlResponse.data.success) {
      selectedKeyTTL.value = ttlResponse.data.data;
    } else {
      selectedKeyTTL.value = null;
    }
  } catch (error) {
    ElMessage.error('获取键值失败: ' + (error.message || '未知错误'));
    displayValue.value = '获取失败';
    selectedKeyTTL.value = null;
  }
};

// 确认删除键
const confirmDeleteKey = (key) => {
  ElMessageBox.confirm(
    `您确定要删除键 "${key}" 吗？`,
    '确认删除',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    deleteKey(key);
  }).catch(() => {});
};

// 删除键
const deleteKey = async (key) => {
  try {
    const response = await axios.delete(`${API_ENDPOINTS.deleteKey}?key=${encodeURIComponent(key)}`);
    if (response.data.success) {
      ElMessage.success('删除成功');
      if (selectedKey.value === key) {
        selectedKey.value = null;
        displayValue.value = '';
        selectedKeyTTL.value = null;
      }
      refreshKeys();
    } else {
      ElMessage.error(response.data.message || '删除失败');
    }
  } catch (error) {
    ElMessage.error('删除失败: ' + (error.message || '未知错误'));
  }
};

// 确认清空所有缓存
const confirmFlushAll = () => {
  ElMessageBox.confirm(
    '您确定要清空所有缓存吗？此操作不可恢复！',
    '危险操作',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    flushAll();
  }).catch(() => {});
};

// 清空所有缓存
const flushAll = async () => {
  try {
    const response = await axios.delete(API_ENDPOINTS.flushAll);
    if (response.data.success) {
      ElMessage.success('缓存已清空');
      refreshKeys();
      selectedKey.value = null;
      displayValue.value = '';
      selectedKeyTTL.value = null;
    } else {
      ElMessage.error(response.data.message || '清空缓存失败');
    }
  } catch (error) {
    ElMessage.error('清空缓存失败: ' + (error.message || '未知错误'));
  }
};

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        const response = await axios.post(API_ENDPOINTS.setValue, {
          key: formState.key,
          value: formState.value,
          ttl: formState.ttl || 0
        });
        
        if (response.data.success) {
          ElMessage.success('保存成功');
          refreshKeys();
          resetForm();
        } else {
          ElMessage.error(response.data.message || '保存失败');
        }
      } catch (error) {
        ElMessage.error('保存失败: ' + (error.message || '未知错误'));
      } finally {
        submitting.value = false;
      }
    }
  });
};

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields();
  }
};

// 测试缓存击穿
const testCacheBreakdown = async () => {
  breakdownResults.value = [];
  breakdownResults.value.push(`开始缓存击穿测试，用户ID: ${breakdownId.value}，并发数: ${breakdownCount.value}`);
  ElMessage.info('正在发送并发请求...');
  
  const startTime = Date.now();
  breakdownResults.value.push(`测试开始时间: ${new Date(startTime).toLocaleTimeString()}`);
  
  try {
    // 调用后端API进行测试
    const apiUrl = API_ENDPOINTS.testPenetration;
    console.log('请求缓存击穿测试API:', apiUrl, { userId: breakdownId.value, concurrency: breakdownCount.value });
    breakdownResults.value.push(`请求API: ${apiUrl}`);
    
    const response = await axios.get(apiUrl, {
      params: {
        userId: breakdownId.value,
        concurrency: breakdownCount.value
      }
    });
    
    console.log('缓存击穿测试响应:', response.data);
    
    const endTime = Date.now();
    const duration = endTime - startTime;
    
    breakdownResults.value.push(`测试完成时间: ${new Date(endTime).toLocaleTimeString()}`);
    breakdownResults.value.push(`总耗时: ${duration}ms`);
    
    if (response.data.success) {
      const resultData = response.data.data;
      
      // 防护机制有效性评估
      breakdownResults.value.push(`防护机制评估: ${resultData.cacheEffectiveness}`);
      breakdownResults.value.push(`缓存命中率: ${resultData.cacheHitRate}`);
      breakdownResults.value.push(`数据库访问次数: ${resultData.estimatedDbHits}/${breakdownCount.value}`);
      
      // 请求时间分析
      if (resultData.requestTimeAnalysis) {
        breakdownResults.value.push(`请求时间分析: ${resultData.requestTimeAnalysis}`);
      } else {
        breakdownResults.value.push(`最长请求耗时: ${resultData.maxRequestTime}ms`);
        breakdownResults.value.push(`最短请求耗时: ${resultData.minRequestTime}ms`);
        breakdownResults.value.push(`平均请求耗时: ${resultData.avgRequestTime.toFixed(2)}ms`);
      }
      
      // Redis连接延迟分析
      if (resultData.redisLatencyAnalysis) {
        breakdownResults.value.push(`Redis连接延迟: ${resultData.redisLatencyAnalysis}`);
      }
      
      // 额外信息
      if (resultData.redisHost) {
        breakdownResults.value.push(`Redis服务器: ${resultData.redisHost}`);
      }
      
      breakdownResults.value.push(resultData.message);
    } else {
      breakdownResults.value.push(`测试失败: ${response.data.message}`);
    }
    
    ElMessage.success('缓存击穿测试完成');
  } catch (error) {
    console.error('缓存击穿测试异常:', error);
    const endTime = Date.now();
    const duration = endTime - startTime;
    
    breakdownResults.value.push(`测试完成时间: ${new Date(endTime).toLocaleTimeString()}`);
    breakdownResults.value.push(`总耗时: ${duration}ms`);
    breakdownResults.value.push(`测试失败: ${error.response?.data?.message || error.message || '未知错误'}`);
    ElMessage.error('测试执行失败: ' + (error.message || '未知错误'));
  }
};

// 测试缓存穿透
const testCachePenetration = () => {
  penetrationResult = '';
  ElMessage.info(`正在测试缓存穿透，用户ID: ${penetrationId.value}`);
  
  try {
    const startTime = Date.now();
    
    setTimeout(() => {
      const endTime = Date.now();
      const duration = endTime - startTime;
      
      penetrationResult = 
        `测试开始时间: ${new Date(startTime).toLocaleTimeString()}\n` +
        `测试完成时间: ${new Date(endTime).toLocaleTimeString()}\n` +
        `总耗时: ${duration}ms\n` +
        '缓存穿透测试完成，测试接口尚未实现，请等待后端支持';
      
      ElMessage.success('缓存穿透测试完成');
    }, 1000);
  } catch (error) {
    ElMessage.error('测试执行失败: ' + (error.message || '未知错误'));
  }
};

// 测试缓存雪崩
const testCacheAvalanche = () => {
  avalancheResults.value = [];
  ElMessage.info('正在测试缓存雪崩...');
  
  try {
    const startTime = Date.now();
    avalancheResults.value.push(`测试开始时间: ${new Date(startTime).toLocaleTimeString()}`);
    
    setTimeout(() => {
      const endTime = Date.now();
      const duration = endTime - startTime;
      
      avalancheResults.value.push(`测试完成时间: ${new Date(endTime).toLocaleTimeString()}`);
      avalancheResults.value.push(`总耗时: ${duration}ms`);
      avalancheResults.value.push('缓存雪崩测试完成，测试接口尚未实现，请等待后端支持');
      ElMessage.success('缓存雪崩测试完成');
    }, 2000);
  } catch (error) {
    ElMessage.error('测试执行失败: ' + (error.message || '未知错误'));
  }
};
</script>

<style scoped>
.redis-management {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.keys-card, .edit-card, .value-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.key-details {
  margin-bottom: 20px;
}

.key-value {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
}

.key-value pre {
  margin: 10px 0 0;
  white-space: pre-wrap;
  word-break: break-all;
}

.key-search {
  margin-left: 10px;
}

.test-section {
  margin-bottom: 20px;
}

.test-section pre {
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  max-height: 300px;
  overflow-y: auto;
  margin-top: 10px;
}

.no-keys {
  padding: 30px 0;
}
</style> 