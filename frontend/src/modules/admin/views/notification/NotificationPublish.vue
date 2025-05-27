<template>
  <div class="notification-publish-container">
    <div class="page-header">
      <h2>{{ isEdit ? '编辑通知' : '发布通知' }}</h2>
    </div>

    <el-card class="notification-form-card">
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="120px" 
        label-position="right"
        size="default"
      >
        <el-form-item label="通知标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入通知标题（100字以内）" :maxlength="100" show-word-limit></el-input>
        </el-form-item>

        <el-form-item label="通知类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio label="普通">普通通知</el-radio>
            <el-radio label="紧急">紧急通知</el-radio>
            <el-radio label="活动">活动通知</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="通知范围" prop="level">
          <el-radio-group v-model="form.level">
            <el-radio label="全校">全校通知</el-radio>
            <el-radio label="院系">院系通知</el-radio>
            <el-radio label="班级">班级通知</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="form.level === '院系'" label="目标院系" prop="targetDepartment">
          <el-select v-model="form.targetDepartment" placeholder="请选择目标院系">
            <el-option v-for="item in departmentOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="通知内容" prop="content">
          <el-input 
            v-model="form.content" 
            type="textarea" 
            placeholder="请输入通知内容" 
            :rows="10"
            maxlength="5000"
            show-word-limit
          ></el-input>
        </el-form-item>

        <el-form-item label="附件">
          <el-upload
            class="upload-demo"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :before-upload="beforeUpload"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :on-exceed="handleExceed"
            :file-list="fileList"
            :limit="1"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>
              点击上传
            </el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持上传PDF、DOC、DOCX等文档格式，单个文件不超过10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-divider />

        <el-form-item>
          <el-button type="primary" @click="submitForm">{{ isEdit ? '保存修改' : '保存为草稿' }}</el-button>
          <el-button type="success" @click="submitAndPublish" v-if="!isEdit || (isEdit && form.status === '草稿')">立即发布</el-button>
          <el-button @click="cancelEdit">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox, FormInstance, UploadProps } from 'element-plus';
import { Upload } from '@element-plus/icons-vue';
import api from '@/api/index';

const route = useRoute();
const router = useRouter();
const formRef = ref<FormInstance>();

// 判断是否为编辑模式
const isEdit = computed(() => {
  return route.params.id !== undefined;
});

// 表单数据
interface NotificationForm {
  id?: number;
  title: string;
  content: string;
  type: string;
  level: string;
  targetDepartment: string | null;
  attachmentUrl: string | null;
  status: string;
  isPinned: boolean;
}

const form = reactive<NotificationForm>({
  title: '',
  content: '',
  type: '普通',
  level: '全校',
  targetDepartment: null,
  attachmentUrl: null,
  status: '草稿',
  isPinned: false
});

// 院系选项
const departmentOptions = [
  { label: '计算机学院', value: '计算机学院' },
  { label: '电子工程学院', value: '电子工程学院' },
  { label: '机械工程学院', value: '机械工程学院' },
  { label: '经济管理学院', value: '经济管理学院' },
  { label: '外国语学院', value: '外国语学院' },
  { label: '艺术学院', value: '艺术学院' }
];

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入通知标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在2到100个字符之间', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入通知内容', trigger: 'blur' },
    { min: 5, max: 5000, message: '内容长度在5到5000个字符之间', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择通知类型', trigger: 'change' }
  ],
  level: [
    { required: true, message: '请选择通知范围', trigger: 'change' }
  ],
  targetDepartment: [
    { 
      required: true, 
      message: '请选择目标院系', 
      trigger: 'change',
      validator: (rule: any, value: string | null, callback: (error?: Error) => void) => {
        if (form.level === '院系' && !value) {
          callback(new Error('请选择目标院系'));
        } else {
          callback();
        }
      }
    }
  ]
};

// 文件上传相关
const fileList = ref<any[]>([]);
const uploadUrl = computed(() => {
  return `${api.defaults.baseURL}/file/upload`;
});
const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${localStorage.getItem('token')}`
  };
});

// 上传前验证
const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  // 文件类型验证
  const allowTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
  const isAllowType = allowTypes.includes(file.type);
  
  // 文件大小验证，限制为10MB
  const isLt10M = file.size / 1024 / 1024 < 10;
  
  if (!isAllowType) {
    ElMessage.error('只能上传PDF/DOC/DOCX文件!');
    return false;
  }
  
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB!');
    return false;
  }
  
  return true;
};

// 上传成功回调
const handleUploadSuccess: UploadProps['onSuccess'] = (response, uploadFile) => {
  if (response && response.url) {
    form.attachmentUrl = response.url;
    ElMessage.success('文件上传成功');
  } else {
    ElMessage.error('文件上传失败');
  }
};

// 上传失败回调
const handleUploadError: UploadProps['onError'] = (error) => {
  console.error('上传失败:', error);
  ElMessage.error('文件上传失败，请重试');
};

// 超出文件数量限制
const handleExceed: UploadProps['onExceed'] = () => {
  ElMessage.warning('最多只能上传1个文件');
};

// 获取通知详情
const fetchNotificationDetail = async (id: string | string[]) => {
  try {
    const response = await api.get(`/notification/${id}`);
    
    if (response.data) {
      // 更新表单数据
      Object.assign(form, response.data);
      
      // 如果有附件，更新文件列表
      if (form.attachmentUrl) {
        fileList.value = [{
          name: getAttachmentName(form.attachmentUrl),
          url: form.attachmentUrl
        }];
      }
    }
  } catch (error) {
    console.error('获取通知详情失败:', error);
    ElMessage.error('获取通知详情失败');
    // 返回列表页
    router.push('/notification/list');
  }
};

// 从附件URL中获取文件名
const getAttachmentName = (url: string): string => {
  if (!url) return '';
  
  const parts = url.split('/');
  return decodeURIComponent(parts[parts.length - 1]);
};

// 提交表单（保存为草稿）
const submitForm = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        const apiUrl = isEdit.value 
          ? `/notification/${form.id}` 
          : '/notification';
        
        const method = isEdit.value ? 'put' : 'post';
        
        const response = await api[method](apiUrl, form);
        
        if (response.data) {
          ElMessage.success(isEdit.value ? '通知更新成功' : '通知保存成功');
          router.push('/notification/list');
        }
      } catch (error) {
        console.error(isEdit.value ? '更新通知失败:' : '保存通知失败:', error);
        ElMessage.error(isEdit.value ? '更新通知失败' : '保存通知失败');
      }
    } else {
      ElMessage.warning('请完善表单信息');
    }
  });
};

// 提交并发布
const submitAndPublish = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        // 先保存或更新通知
        const saveApiUrl = isEdit.value 
          ? `/notification/${form.id}` 
          : '/notification';
        
        const saveMethod = isEdit.value ? 'put' : 'post';
        const saveResponse = await api[saveMethod](saveApiUrl, form);
        
        if (saveResponse.data) {
          // 再发布通知
          const notificationId = isEdit.value 
            ? form.id 
            : saveResponse.data.id;
          
          await api.post(`/notification/${notificationId}/publish`);
          
          ElMessage.success('通知发布成功');
          router.push('/notification/list');
        }
      } catch (error) {
        console.error('发布通知失败:', error);
        ElMessage.error('发布通知失败');
      }
    } else {
      ElMessage.warning('请完善表单信息');
    }
  });
};

// 取消编辑
const cancelEdit = () => {
  ElMessageBox.confirm('确定要取消编辑吗？未保存的内容将会丢失', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    router.push('/notification/list');
  }).catch(() => {
    // 用户取消操作，不做任何处理
  });
};

// 初始化
onMounted(() => {
  // 如果是编辑模式，获取通知详情
  if (isEdit.value && route.params.id) {
    fetchNotificationDetail(route.params.id);
  }
});
</script>

<style scoped>
.notification-publish-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.notification-form-card {
  margin-bottom: 20px;
}

.el-form {
  max-width: 800px;
  margin: 0 auto;
}

.el-upload__tip {
  line-height: 1.2;
  margin-top: 8px;
  color: #909399;
}
</style> 