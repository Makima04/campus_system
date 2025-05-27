<template>
  <div class="file-upload-demo">
    <h1>文件上传示例</h1>
    <div class="upload-section">
      <h2>分片上传示例</h2>
      <p>支持大文件分片上传、断点续传</p>
      <ChunkFileUploader />
    </div>
  </div>
</template>

<script setup lang="ts">
import ChunkFileUploader from '../components/ChunkFileUploader.vue';
</script>

<style scoped>
.file-upload-demo {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.upload-section {
  margin-top: 30px;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #fff;
}

h1 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 20px;
}

h2 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 10px;
}

p {
  color: #606266;
  margin-bottom: 20px;
}
</style> 