<template>
  <div class="meal-card-management">
    <h2 class="page-title">饭卡管理</h2>

    <!-- 搜索和操作栏 -->
    <div class="operation-bar">
      <el-input
        v-model="searchQuery"
        placeholder="搜索学生姓名或饭卡号"
        class="search-input"
        clearable
        @clear="handleSearch"
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        添加饭卡
      </el-button>
    </div>

    <!-- 饭卡列表 -->
    <el-table :data="filteredCards" style="width: 100%" v-loading="loading">
      <el-table-column prop="cardNumber" label="饭卡号" width="120" />
      <el-table-column prop="studentName" label="学生姓名" width="120" />
      <el-table-column prop="balance" label="余额" width="120">
        <template #default="scope">
          ¥{{ scope.row.balance.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="250">
        <template #default="scope">
          <el-button type="primary" link @click="showRechargeDialog(scope.row)">
            充值
          </el-button>
          <el-button type="primary" link @click="showEditDialog(scope.row)">
            编辑
          </el-button>
          <el-button type="danger" link @click="handleDelete(scope.row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/编辑饭卡对话框 -->
    <el-dialog
      :title="dialogType === 'add' ? '添加饭卡' : '编辑饭卡'"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form :model="cardForm" :rules="rules" ref="cardFormRef" label-width="100px">
        <el-form-item label="学生" prop="studentId">
          <el-select v-model="cardForm.studentId" placeholder="请选择学生">
            <el-option
              v-for="student in students"
              :key="student.id"
              :label="student.realName"
              :value="student.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="初始余额" prop="balance">
          <el-input-number v-model="cardForm.balance" :min="0" :precision="2" :step="10" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="cardForm.status" placeholder="请选择状态">
            <el-option label="正常" value="NORMAL" />
            <el-option label="冻结" value="FROZEN" />
            <el-option label="注销" value="CANCELLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 充值对话框 -->
    <el-dialog title="饭卡充值" v-model="rechargeDialogVisible" width="400px">
      <el-form :model="rechargeForm" :rules="rechargeRules" ref="rechargeFormRef" label-width="100px">
        <el-form-item label="当前余额">
          <span>¥{{ currentCard?.balance.toFixed(2) }}</span>
        </el-form-item>
        <el-form-item label="充值金额" prop="amount">
          <el-input-number v-model="rechargeForm.amount" :min="0" :precision="2" :step="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rechargeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleRecharge">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from '@vue/runtime-dom'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import http from '@/utils/http'

interface MealCard {
  id: number
  cardNumber: string
  studentId: number
  studentName: string
  balance: number
  status: string
  createdAt: string
}

interface Student {
  id: number
  realName: string
}

interface ApiResponse<T> {
  data: T
  code: number
  message: string
}

const loading = ref(false)
const searchQuery = ref('')
const cards = ref<MealCard[]>([])
const students = ref<Student[]>([])
const dialogVisible = ref(false)
const rechargeDialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const cardFormRef = ref<FormInstance>()
const rechargeFormRef = ref<FormInstance>()
const currentCard = ref<MealCard | null>(null)

const cardForm = ref({
  id: 0,
  studentId: 0,
  balance: 0,
  status: 'NORMAL'
})

const rechargeForm = ref({
  amount: 0
})

const rules = {
  studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
  balance: [{ required: true, message: '请输入初始余额', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const rechargeRules = {
  amount: [{ required: true, message: '请输入充值金额', trigger: 'blur' }]
}

const filteredCards = computed(() => {
  if (!searchQuery.value) return cards.value
  const query = searchQuery.value.toLowerCase()
  return cards.value.filter(
    (card: MealCard) =>
      card.studentName.toLowerCase().includes(query) ||
      card.cardNumber.toLowerCase().includes(query)
  )
})

// 获取饭卡列表
const fetchCards = async () => {
  loading.value = true
  try {
    const response = await http.get<ApiResponse<MealCard[]>>('/meal-card/list')
    cards.value = response.data.data
  } catch (error) {
    console.error('获取饭卡列表失败:', error)
    ElMessage.error('获取饭卡列表失败')
  } finally {
    loading.value = false
  }
}

// 获取学生列表
const fetchStudents = async () => {
  try {
    const response = await http.get<ApiResponse<Student[]>>('/user/students')
    students.value = response.data.data
  } catch (error) {
    console.error('获取学生列表失败:', error)
    ElMessage.error('获取学生列表失败')
  }
}

// 显示添加对话框
const showAddDialog = () => {
  dialogType.value = 'add'
  cardForm.value = {
    id: 0,
    studentId: 0,
    balance: 0,
    status: 'NORMAL'
  }
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (card: MealCard) => {
  dialogType.value = 'edit'
  cardForm.value = {
    id: card.id,
    studentId: card.studentId,
    balance: card.balance,
    status: card.status
  }
  dialogVisible.value = true
}

// 显示充值对话框
const showRechargeDialog = (card: MealCard) => {
  currentCard.value = card
  rechargeForm.value = {
    amount: 0
  }
  rechargeDialogVisible.value = true
}

// 处理删除
const handleDelete = async (card: MealCard) => {
  try {
    await ElMessageBox.confirm('确定要删除该饭卡吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await http.delete(`/meal-card/${card.id}`)
    ElMessage.success('删除成功')
    fetchCards()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除饭卡失败:', error)
      ElMessage.error('删除饭卡失败')
    }
  }
}

// 处理提交
const handleSubmit = async () => {
  if (!cardFormRef.value) return
  
  await cardFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (dialogType.value === 'add') {
          await http.post('/meal-card', cardForm.value)
          ElMessage.success('添加成功')
        } else {
          await http.put(`/meal-card/${cardForm.value.id}`, cardForm.value)
          ElMessage.success('更新成功')
        }
        dialogVisible.value = false
        fetchCards()
      } catch (error) {
        console.error('保存饭卡失败:', error)
        ElMessage.error('保存饭卡失败')
      }
    }
  })
}

// 处理充值
const handleRecharge = async () => {
  if (!rechargeFormRef.value || !currentCard.value) return
  
  await rechargeFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        await http.post(`/meal-card/${currentCard.value.id}/recharge`, {
          amount: rechargeForm.value.amount
        })
        ElMessage.success('充值成功')
        rechargeDialogVisible.value = false
        fetchCards()
      } catch (error) {
        console.error('充值失败:', error)
        ElMessage.error('充值失败')
      }
    }
  })
}

// 处理搜索
const handleSearch = () => {
  // 搜索逻辑已通过计算属性实现
}

// 获取状态类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'NORMAL':
      return 'success'
    case 'FROZEN':
      return 'warning'
    case 'CANCELLED':
      return 'danger'
    default:
      return 'info'
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'NORMAL':
      return '正常'
    case 'FROZEN':
      return '冻结'
    case 'CANCELLED':
      return '注销'
    default:
      return '未知'
  }
}

// 格式化日期
const formatDate = (date: string) => {
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

onMounted(() => {
  fetchCards()
  fetchStudents()
})
</script>

<style scoped>
.meal-card-management {
  padding: 20px;
}

.page-title {
  margin-bottom: 30px;
  color: #303133;
}

.operation-bar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.search-input {
  width: 300px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 