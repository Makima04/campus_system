<template>
  <div class="redis-key-table">
    <div class="table-header">
      <h3>缓存键列表</h3>
      <div class="actions">
        <el-button type="primary" size="small" @click="refreshKeys">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </div>
    </div>
    
    <el-table
      :data="keys"
      border
      stripe
      style="width: 100%"
      v-loading="loading"
      @row-click="onSelectKey"
    >
      <el-table-column prop="key" label="键名" min-width="180" show-overflow-tooltip />
      <el-table-column prop="type" label="类型" width="100" />
      <el-table-column label="过期时间" width="150">
        <template #default="scope">
          <span v-if="scope.row.ttl === -1">永不过期</span>
          <span v-else-if="scope.row.ttl >= 0">{{ formatTTL(scope.row.ttl) }}</span>
          <span v-else>已过期</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="scope">
          <el-button 
            type="primary" 
            size="small" 
            circle 
            @click.stop="viewKeyDetails(scope.row)"
            title="查看详情"
          >
            <el-icon><View /></el-icon>
          </el-button>
          <el-button 
            type="danger" 
            size="small" 
            circle 
            @click.stop="confirmDeleteKey(scope.row.key)"
            title="删除键"
          >
            <el-icon><Delete /></el-icon>
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="empty-block" v-if="keys.length === 0 && !loading">
      <el-empty description="暂无缓存键" />
    </div>
    
    <div class="pagination" v-if="keys.length > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        :total="keys.length"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, defineEmits } from 'vue';
import { ElMessageBox, ElMessage } from 'element-plus';
import { Refresh, View, Delete } from '@element-plus/icons-vue';
import redisService from '../api/redis-service';

// 定义事件
const emit = defineEmits(['select-key', 'keys-refreshed']);

// 状态
const keys = ref<any[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);

// 获取所有键
const getAllKeys = async () => {
  loading.value = true;
  try {
    const response = await redisService.getAllKeys();
    if (response.data.success) {
      keys.value = response.data.data || [];
      emit('keys-refreshed', keys.value);
    } else {
      ElMessage.error(response.data.message || '获取缓存键失败');
    }
  } catch (error: any) {
    ElMessage.error('获取缓存键失败: ' + (error.message || '未知错误'));
  } finally {
    loading.value = false;
  }
};

// 刷新键列表
const refreshKeys = () => {
  getAllKeys();
};

// 选择键
const onSelectKey = (row: any) => {
  emit('select-key', row.key);
};

// 查看键详情
const viewKeyDetails = (row: any) => {
  emit('select-key', row.key);
};

// 确认删除键
const confirmDeleteKey = (key: string) => {
  ElMessageBox.confirm(
    `确定要删除键 "${key}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    deleteKey(key);
  }).catch(() => {
    // 取消删除
  });
};

// 删除键
const deleteKey = async (key: string) => {
  try {
    const response = await redisService.deleteKey(key);
    if (response.data.success) {
      ElMessage.success('删除成功');
      refreshKeys();
    } else {
      ElMessage.error(response.data.message || '删除失败');
    }
  } catch (error: any) {
    ElMessage.error('删除失败: ' + (error.message || '未知错误'));
  }
};

// 格式化TTL显示
const formatTTL = (seconds: number) => {
  if (seconds < 60) {
    return `${seconds}秒`;
  } else if (seconds < 3600) {
    return `${Math.floor(seconds / 60)}分${seconds % 60}秒`;
  } else {
    return `${Math.floor(seconds / 3600)}时${Math.floor((seconds % 3600) / 60)}分`;
  }
};

// 组件挂载时获取键列表
onMounted(() => {
  getAllKeys();
});

// 暴露方法给父组件
defineExpose({
  refreshKeys
});
</script>

<style scoped>
.redis-key-table {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.table-header h3 {
  margin: 0;
}

.actions {
  display: flex;
  gap: 10px;
}

.pagination {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
}

.empty-block {
  margin: 20px 0;
}
</style> 