<template>
  <div class="admin-management-container">
    <div class="page-header">
      <h2>通知管理员管理</h2>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        新增通知管理员
      </el-button>
    </div>

    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="adminList"
        style="width: 100%"
        stripe
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="realName" label="真实姓名" width="150" />
        <el-table-column prop="department" label="所属部门" width="180" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="phone" label="电话" width="150" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ scope.row.status === 'ACTIVE' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-button type="primary" size="small" @click="editAdmin(scope.row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button 
                :type="scope.row.status === 'ACTIVE' ? 'danger' : 'success'" 
                size="small" 
                @click="toggleStatus(scope.row)"
              >
                <el-icon><component :is="scope.row.status === 'ACTIVE' ? 'Lock' : 'Unlock'" /></el-icon>
                {{ scope.row.status === 'ACTIVE' ? '禁用' : '启用' }}
              </el-button>
              <el-button type="danger" size="small" @click="resetPassword(scope.row)">
                <el-icon><Key /></el-icon>
                重置密码
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

    <!-- 新增/编辑管理员对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑管理员' : '新增通知管理员'"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        label-position="right"
        size="default"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码"
            show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名"></el-input>
        </el-form-item>
        <el-form-item label="所属部门" prop="department">
          <el-select v-model="form.department" placeholder="请选择部门">
            <el-option 
              v-for="item in departmentOptions" 
              :key="item.value" 
              :label="item.label" 
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入电话号码"></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="form.status"
            :active-value="'ACTIVE'"
            :inactive-value="'INACTIVE'"
            active-text="启用"
            inactive-text="禁用"
          ></el-switch>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="resetPasswordDialogVisible"
      title="重置密码"
      width="400px"
      destroy-on-close
    >
      <el-form
        ref="resetPasswordFormRef"
        :model="resetPasswordForm"
        :rules="resetPasswordRules"
        label-width="100px"
      >
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="resetPasswordForm.newPassword" 
            type="password" 
            placeholder="请输入新密码"
            show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="resetPasswordForm.confirmPassword" 
            type="password" 
            placeholder="请再次输入新密码"
            show-password
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitResetPassword">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus';
import { Plus, Edit, Lock, Unlock, Key } from '@element-plus/icons-vue';
import api from '@/api/index';

// 表单引用
const formRef = ref<FormInstance>();
const resetPasswordFormRef = ref<FormInstance>();

// 列表数据和分页
const loading = ref(false);
const adminList = ref<any[]>([]);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

// 对话框控制
const dialogVisible = ref(false);
const resetPasswordDialogVisible = ref(false);
const isEdit = ref(false);
const currentAdminId = ref<number | null>(null);

// 部门选项
const departmentOptions = [
  { label: '校办公室', value: '校办公室' },
  { label: '学生处', value: '学生处' },
  { label: '教务处', value: '教务处' },
  { label: '人事处', value: '人事处' },
  { label: '计算机学院', value: '计算机学院' },
  { label: '电子工程学院', value: '电子工程学院' },
  { label: '机械工程学院', value: '机械工程学院' },
  { label: '经济管理学院', value: '经济管理学院' },
  { label: '外国语学院', value: '外国语学院' },
  { label: '艺术学院', value: '艺术学院' }
];

// 表单数据
interface AdminForm {
  id?: number;
  username: string;
  password: string;
  realName: string;
  department: string;
  email: string;
  phone: string;
  status: string;
  role: string;
}

const form = reactive<AdminForm>({
  username: '',
  password: '',
  realName: '',
  department: '',
  email: '',
  phone: '',
  status: 'ACTIVE',
  role: 'NOTICE_ADMIN' // 默认角色为通知管理员
});

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度在4到20个字符之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符之间', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { max: 20, message: '真实姓名长度不超过20个字符', trigger: 'blur' }
  ],
  department: [
    { required: true, message: '请选择所属部门', trigger: 'change' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入电话号码', trigger: 'blur' },
    { pattern: /^1[3456789]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
};

// 重置密码表单
interface ResetPasswordForm {
  userId: number | null;
  newPassword: string;
  confirmPassword: string;
}

const resetPasswordForm = reactive<ResetPasswordForm>({
  userId: null,
  newPassword: '',
  confirmPassword: ''
});

// 重置密码表单验证规则
const resetPasswordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value !== resetPasswordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

// 获取通知管理员列表
const fetchAdminList = async () => {
  try {
    loading.value = true;
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      role: 'NOTICE_ADMIN'
    };
    
    const response = await api.get('/user/admin/list', { params });
    adminList.value = response.data.content || [];
    total.value = response.data.totalElements || 0;
  } catch (error) {
    console.error('获取通知管理员列表失败:', error);
    ElMessage.error('获取通知管理员列表失败');
    // 使用模拟数据用于演示
    adminList.value = generateMockData();
    total.value = adminList.value.length;
  } finally {
    loading.value = false;
  }
};

// 生成模拟数据
const generateMockData = () => {
  return [
    {
      id: 1,
      username: 'notice_admin1',
      realName: '张三',
      department: '校办公室',
      email: 'zhangsan@example.com',
      phone: '13800138001',
      status: 'ACTIVE',
      createdAt: '2025-03-25T10:00:00'
    },
    {
      id: 2,
      username: 'notice_admin2',
      realName: '李四',
      department: '学生处',
      email: 'lisi@example.com',
      phone: '13800138002',
      status: 'ACTIVE',
      createdAt: '2025-03-26T11:30:00'
    },
    {
      id: 3,
      username: 'notice_admin3',
      realName: '王五',
      department: '教务处',
      email: 'wangwu@example.com',
      phone: '13800138003',
      status: 'INACTIVE',
      createdAt: '2025-03-27T14:15:00'
    }
  ];
};

// 格式化日期时间
const formatDateTime = (dateTime: string): string => {
  if (!dateTime) return '-';
  
  const date = new Date(dateTime);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};

// 打开新增对话框
const openAddDialog = () => {
  // 重置表单
  if (formRef.value) {
    formRef.value.resetFields();
  }
  
  // 设置默认值
  Object.assign(form, {
    username: '',
    password: '',
    realName: '',
    department: '',
    email: '',
    phone: '',
    status: 'ACTIVE',
    role: 'NOTICE_ADMIN'
  });
  
  isEdit.value = false;
  dialogVisible.value = true;
};

// 打开编辑对话框
const editAdmin = (admin: any) => {
  // 重置表单
  if (formRef.value) {
    formRef.value.resetFields();
  }
  
  // 填充表单数据
  Object.assign(form, {
    id: admin.id,
    username: admin.username,
    realName: admin.realName || '',
    department: admin.department || '',
    email: admin.email || '',
    phone: admin.phone || '',
    status: admin.status || 'ACTIVE',
    role: 'NOTICE_ADMIN'
  });
  
  isEdit.value = true;
  dialogVisible.value = true;
};

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    
    const apiUrl = isEdit.value 
      ? `/user/${form.id}` 
      : '/user/admin/create';
    
    const method = isEdit.value ? 'put' : 'post';
    
    const response = await api[method](apiUrl, form);
    
    if (response.data) {
      ElMessage.success(isEdit.value ? '管理员更新成功' : '管理员创建成功');
      dialogVisible.value = false;
      fetchAdminList();
    }
  } catch (error) {
    console.error(isEdit.value ? '更新管理员失败:' : '创建管理员失败:', error);
    ElMessage.error(isEdit.value ? '更新管理员失败' : '创建管理员失败');
  }
};

// 切换管理员状态
const toggleStatus = async (admin: any) => {
  try {
    const newStatus = admin.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    const message = newStatus === 'ACTIVE' ? '启用' : '禁用';
    
    await ElMessageBox.confirm(`确定要${message}管理员 ${admin.realName || admin.username} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    await api.put(`/user/${admin.id}/status`, { status: newStatus });
    
    ElMessage.success(`${message}管理员成功`);
    fetchAdminList();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('切换管理员状态失败:', error);
      ElMessage.error('操作失败');
    }
  }
};

// 打开重置密码对话框
const resetPassword = (admin: any) => {
  resetPasswordForm.userId = admin.id;
  resetPasswordForm.newPassword = '';
  resetPasswordForm.confirmPassword = '';
  
  if (resetPasswordFormRef.value) {
    resetPasswordFormRef.value.resetFields();
  }
  
  resetPasswordDialogVisible.value = true;
};

// 提交重置密码
const submitResetPassword = async () => {
  if (!resetPasswordFormRef.value) return;
  
  try {
    await resetPasswordFormRef.value.validate();
    
    await api.post(`/user/${resetPasswordForm.userId}/reset-password`, {
      newPassword: resetPasswordForm.newPassword
    });
    
    ElMessage.success('密码重置成功');
    resetPasswordDialogVisible.value = false;
  } catch (error) {
    console.error('重置密码失败:', error);
    ElMessage.error('重置密码失败');
  }
};

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  fetchAdminList();
};

const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  fetchAdminList();
};

onMounted(() => {
  fetchAdminList();
});
</script>

<style scoped>
.admin-management-container {
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

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 