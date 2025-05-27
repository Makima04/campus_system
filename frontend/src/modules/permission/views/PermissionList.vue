<template>
  <div class="permission-list">
    <div class="header">
      <h2>权限管理</h2>
      <el-button type="primary" @click="handleAddPermission">添加权限</el-button>
    </div>

    <el-table
      :data="permissionList"
      row-key="id"
      :tree-props="{ children: 'children' }"
      style="width: 100%"
    >
      <el-table-column prop="name" label="权限名称" width="200" />
      <el-table-column prop="code" label="权限标识" width="200" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.type === 'menu' ? 'success' : 'info'">
            {{ row.type === 'menu' ? '菜单' : '按钮' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleEditPermission(row)">
            编辑
          </el-button>
          <el-button 
            type="danger" 
            size="small" 
            @click="handleDeletePermission(row)"
            :disabled="row.children && row.children.length > 0"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 权限表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑权限' : '添加权限'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="上级权限">
          <el-tree-select
            v-model="form.parentId"
            :data="permissionOptions"
            :props="{ label: 'name', value: 'id' }"
            placeholder="请选择上级权限"
            clearable
          />
        </el-form-item>
        <el-form-item label="权限名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限标识" prop="code">
          <el-input v-model="form.code" placeholder="请输入权限标识" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="请输入描述"
          />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio label="menu">菜单</el-radio>
            <el-radio label="button">按钮</el-radio>
          </el-radio-group>
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
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 模拟的权限数据
const permissionList = ref([
  {
    id: 1,
    name: '系统管理',
    code: 'system',
    type: 'menu',
    description: '系统管理模块',
    children: [
      {
        id: 11,
        name: '用户管理',
        code: 'system:user',
        type: 'menu',
        description: '用户管理模块',
        children: [
          {
            id: 111,
            name: '查看用户',
            code: 'system:user:view',
            type: 'button',
            description: '查看用户信息'
          },
          {
            id: 112,
            name: '编辑用户',
            code: 'system:user:edit',
            type: 'button',
            description: '编辑用户信息'
          }
        ]
      }
    ]
  }
])

const dialogVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

const form = ref({
  parentId: null,
  name: '',
  code: '',
  description: '',
  type: 'menu'
})

const rules = {
  name: [
    { required: true, message: '请输入权限名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入权限标识', trigger: 'blur' },
    { pattern: /^[a-z][\w.:]+$/, message: '权限标识格式不正确', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择权限类型', trigger: 'change' }
  ]
}

// 用于选择上级权限的树形数据
const permissionOptions = ref([
  {
    id: 0,
    name: '根权限',
    children: permissionList.value
  }
])

const resetForm = () => {
  form.value = {
    parentId: null,
    name: '',
    code: '',
    description: '',
    type: 'menu'
  }
}

const handleAddPermission = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEditPermission = (row) => {
  isEdit.value = true
  Object.assign(form.value, row)
  dialogVisible.value = true
}

const handleDeletePermission = (row) => {
  if (row.children && row.children.length > 0) {
    ElMessage.warning('该权限下有子权限，无法删除')
    return
  }

  ElMessageBox.confirm(
    '确定要删除该权限吗？',
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
</script>

<style scoped>
.permission-list {
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