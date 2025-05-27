<template>
  <div class="chunk-file-uploader">
    <div class="upload-container">
      <div class="upload-area" v-if="!currentFile">
        <input 
          type="file" 
          ref="fileInput" 
          @change="handleFileSelected" 
          style="display: none"
        />
        <el-button type="primary" @click="triggerFileSelect">选择文件</el-button>
        <p>支持大文件断点续传</p>
      </div>
      
      <div class="upload-progress" v-if="currentFile">
        <div class="file-info">
          <p>{{ currentFile.fileName }} ({{ formatFileSize(currentFile.fileSize) }})</p>
          <el-progress 
            :percentage="currentFile.progress" 
            :format="percentageFormat"
            :status="uploadStatus"
          />
        </div>
        
        <div class="upload-actions">
          <el-button 
            v-if="!isUploading && !isComplete" 
            type="primary" 
            @click="startUpload"
          >
            开始上传
          </el-button>
          <el-button 
            v-if="isUploading && !isComplete" 
            type="warning" 
            @click="pauseUpload"
          >
            暂停
          </el-button>
          <el-button 
            v-if="!isUploading && !isComplete && hasPaused" 
            type="success" 
            @click="resumeUpload"
          >
            继续上传
          </el-button>
          <el-button 
            v-if="!isComplete" 
            type="danger" 
            @click="cancelUpload"
          >
            取消
          </el-button>
          <el-button 
            v-if="isComplete" 
            type="success" 
            @click="uploadAnother"
          >
            上传新文件
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { chunkUploader } from '@/utils/chunkUploader';

const fileInput = ref<HTMLInputElement | null>(null);
const currentFile = ref<any>(null);
const isUploading = ref(false);
const hasPaused = ref(false);
const isComplete = ref(false);

const uploadStatus = computed(() => {
  if (isComplete.value) return 'success';
  if (hasPaused.value) return 'warning';
  if (isUploading.value) return '';
  return '';
});

const triggerFileSelect = () => {
  fileInput.value?.click();
};

const handleFileSelected = async (event: Event) => {
  const input = event.target as HTMLInputElement;
  if (!input.files || input.files.length === 0) return;
  
  const file = input.files[0];
  isUploading.value = false;
  hasPaused.value = false;
  isComplete.value = false;
  
  try {
    ElMessage.info('正在初始化上传...');
    const fileInfo = await chunkUploader.initUpload(file);
    currentFile.value = fileInfo;
    
    if (fileInfo.progress === 100) {
      ElMessage.success('文件已经上传完成');
      isComplete.value = true;
    } else if (fileInfo.uploadedChunks.length > 0) {
      ElMessage.info(`发现未完成的上传任务，已上传${fileInfo.uploadedChunks.length}/${fileInfo.chunkCount}个分片`);
    }
  } catch (error) {
    ElMessage.error('初始化上传失败');
    console.error('初始化上传失败:', error);
  }
};

const startUpload = async () => {
  if (!currentFile.value) return;
  isUploading.value = true;
  
  try {
    const result = await chunkUploader.uploadChunks();
    if (result.success) {
      ElMessage.success('文件上传成功');
      isComplete.value = true;
      currentFile.value.fileUrl = result.fileUrl;
    } else {
      if (result.error !== '上传未完成') {
        ElMessage.error(`上传失败: ${result.error}`);
      }
      hasPaused.value = true;
    }
  } catch (error) {
    ElMessage.error('上传过程发生错误');
    console.error('上传失败:', error);
  } finally {
    isUploading.value = false;
  }
};

const pauseUpload = () => {
  chunkUploader.pauseUpload();
  hasPaused.value = true;
  isUploading.value = false;
  ElMessage.warning('上传已暂停');
};

const resumeUpload = async () => {
  isUploading.value = true;
  hasPaused.value = false;
  
  try {
    const result = await chunkUploader.resumeUpload();
    if (result.success) {
      ElMessage.success('文件上传成功');
      isComplete.value = true;
      currentFile.value.fileUrl = result.fileUrl;
    } else {
      if (result.error !== '上传未完成') {
        ElMessage.error(`上传失败: ${result.error}`);
      }
      hasPaused.value = true;
    }
  } catch (error) {
    ElMessage.error('恢复上传失败');
    console.error('恢复上传失败:', error);
  } finally {
    isUploading.value = false;
  }
};

const cancelUpload = () => {
  chunkUploader.cancelUpload();
  currentFile.value = null;
  isUploading.value = false;
  hasPaused.value = false;
  isComplete.value = false;
  ElMessage.info('上传已取消');
};

const uploadAnother = () => {
  currentFile.value = null;
  isUploading.value = false;
  hasPaused.value = false;
  isComplete.value = false;
  triggerFileSelect();
};

const formatFileSize = (bytes: number): string => {
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB';
  if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
  return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
};

const percentageFormat = (percentage: number): string => {
  if (isComplete.value) return '已完成';
  if (hasPaused.value) return '已暂停';
  return `${percentage}%`;
};
</script>

<style scoped>
.chunk-file-uploader {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.upload-container {
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  padding: 30px;
  text-align: center;
}

.upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 150px;
}

.upload-progress {
  width: 100%;
}

.file-info {
  margin-bottom: 20px;
}

.upload-actions {
  margin: 20px 0;
  display: flex;
  justify-content: center;
  gap: 10px;
}
</style> 