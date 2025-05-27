<template>
  <div class="role-list">
    <div class="header">
      <h2>角色管理</h2>
      <el-button type="primary" @click="handleAddRole">添加角色</el-button>
    </div>

    <el-table :data="roleList" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="角色名称" width="150" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleEditRole(row)">
            编辑
          </el-button>
          <el-button type="success" size="small" @click="handleSetPermissions(row)">
            设置权限
          </el-button>
          <el-button type="danger" size="small" @click="handleDeleteRole(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 角色表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑角色' : '添加角色'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="请输入角色描述"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 权限设置对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      title="设置权限"
      width="600px"
    >
      <el-tree
        ref="permissionTreeRef"
        :data="permissionTree"
        show-checkbox
        node-key="id"
        :props="{ label: 'name' }"
      />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="permissionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSavePermissions">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const roleList = ref([
  {
    id: 1,
    name: '超级管理员',
    description: '系统最高权限',
    status: 1
  },
  {
    id: 2,
    name: '教师',
    description: '教师角色',
    status: 1
  },
  {
    id: 3,
    name: '学生',
    description: '学生角色',
    status: 1
  }
])

const dialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const formRef = ref(null)
const permissionTreeRef = ref(null)
const isEdit = ref(false)
const currentRole = ref(null)

const form = ref({
  name: '',
  description: '',
  status: 1
})

const rules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入角色描述', trigger: 'blur' }
  ]
}

// 模拟的权限树数据
const permissionTree = ref([
  {
    id: 1,
    name: '系统管理',
    children: [
      { id: 11, name: '用户管理' },
      { id: 12, name: '角色管理' },
      { id: 13, name: '权限管理' }
    ]
  },
  {
    id: 2,
    name: '教务管理',
    children: [
      { id: 21, name: '课程管理' },
      { id: 22, name: '班级管理' },
      { id: 23, name: '成绩管理' }
    ]
  }
])

const resetForm = () => {
  form.value = {
    name: '',
    description: '',
    status: 1
  }
}

const handleAddRole = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEditRole = (row) => {
  isEdit.value = true
  Object.assign(form.value, row)
  dialogVisible.value = true
}

const handleSetPermissions = (row) => {
  currentRole.value = row
  // TODO: 加载该角色的权限
  permissionDialogVisible.value = true
}

const handleDeleteRole = (row) => {
  ElMessageBox.confirm(
    '确定要删除该角色吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    // TODO: 调用删除API
    ElMessage.success('删除成功')
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // TODO: 调用保存API
        ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
        dialogVisible.value = false
      } catch (error) {
        ElMessage.error(isEdit.value ? '修改失败' : '添加失败')
      }
    }
  })
}

const handleSavePermissions = async () => {
  if (!permissionTreeRef.value || !currentRole.value) return
  
  const checkedKeys = permissionTreeRef.value.getCheckedKeys()
  const halfCheckedKeys = permissionTreeRef.value.getHalfCheckedKeys()
  
  try {
    // TODO: 调用保存权限API
    console.log('选中的权限:', [...checkedKeys, ...halfCheckedKeys])
    ElMessage.success('权限设置成功')
    permissionDialogVisible.value = false
  } catch (error) {
    ElMessage.error('权限设置失败')
  }
}
</script>

<style scoped>
.role-list {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 