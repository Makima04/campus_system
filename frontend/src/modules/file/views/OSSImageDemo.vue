<!-- OSS图片展示与使用示例 -->
<template>
  <div class="oss-image-demo">
    <h1>阿里云OSS图片使用示例</h1>
    
    <!-- 图片展示区域 -->
    <el-card class="image-display">
      <template #header>
        <div class="card-header">
          <span>OSS图片示例</span>
          <el-button type="primary" @click="checkOSSConfig">检查OSS配置</el-button>
        </div>
      </template>
      <div class="image-list">
        <div v-for="(image, index) in ossImages" :key="index" class="image-item">
          <el-image 
            :src="image.signedUrl || image.url" 
            :alt="image.name"
            fit="cover"
            :preview-src-list="[image.signedUrl || image.url]">
            <template #error>
              <div class="image-error">
                <el-icon><picture /></el-icon>
                <p>图片加载失败</p>
                <el-button size="small" type="primary" @click="getSignedUrl(image)">
                  获取签名URL
                </el-button>
              </div>
            </template>
          </el-image>
          <div class="image-info">
            <p>{{ image.name }}</p>
            <el-button type="primary" size="small" @click="copyImageUrl(image.signedUrl || image.url)">复制URL</el-button>
          </div>
        </div>
      </div>
    </el-card>
    
    <!-- 图片URL输入测试区域 -->
    <el-card class="image-test">
      <template #header>
        <div class="card-header">
          <span>测试OSS图片URL</span>
        </div>
      </template>
      <div class="url-test">
        <el-input v-model="testImageUrl" placeholder="输入OSS图片路径，例如: images/banner/test.jpg"></el-input>
        <el-button type="primary" @click="getSignedUrlForTest">获取签名URL</el-button>
      </div>
      <div class="test-result" v-if="signedTestUrl">
        <p>签名URL (1小时有效):</p>
        <el-input v-model="signedTestUrl" />
        <el-button type="primary" size="small" @click="copyImageUrl(signedTestUrl)">
          复制URL
        </el-button>
        <div class="image-preview">
          <el-image 
            :src="signedTestUrl" 
            fit="contain"
            style="width: 300px; height: 300px;">
            <template #error>
              <div class="image-error">
                <el-icon><picture /></el-icon>
                <p>图片加载失败</p>
              </div>
            </template>
          </el-image>
        </div>
      </div>
    </el-card>
    
    <!-- OSS配置检查结果 -->
    <el-card v-if="configCheckResult" class="config-check">
      <template #header>
        <div class="card-header">
          <span>OSS配置检查结果</span>
        </div>
      </template>
      <div class="config-details">
        <p><strong>端点:</strong> {{ configCheckResult.endpoint }}</p>
        <p><strong>存储桶:</strong> {{ configCheckResult.bucket }}</p>
        <p><strong>存储桶存在:</strong> {{ configCheckResult.bucketExists ? '是' : '否' }}</p>
        <p><strong>存储桶访问控制:</strong> {{ configCheckResult.bucketAcl }}</p>
        <p><strong>测试URL:</strong></p>
        <el-input v-model="configCheckResult.testUrl" />
        <el-button type="primary" size="small" @click="copyImageUrl(configCheckResult.testUrl)">
          复制URL
        </el-button>
        <div class="image-preview" v-if="configCheckResult.testUrl">
          <el-image 
            :src="configCheckResult.testUrl" 
            fit="contain"
            style="width: 300px;">
          </el-image>
        </div>
      </div>
    </el-card>
    
    <!-- 使用方法说明 -->
    <el-card class="usage-card">
      <template #header>
        <div class="card-header">
          <span>OSS图片访问方法</span>
        </div>
      </template>
      <div class="usage-content">
        <h3>1. 使用签名URL访问私有存储桶中的图片</h3>
        <el-code>
// 从后端获取签名URL
const getSignedUrl = async (objectName) => {
  try {
    const response = await axios.get('/api/oss/get-signed-url', {
      params: { objectName }
    });
    return response.data.signedUrl;
  } catch (error) {
    console.error('获取签名URL失败:', error);
    return null;
  }
};

// 使用签名URL显示图片
const imageUrl = ref('');
getSignedUrl('images/banner/example.jpg').then(url => {
  imageUrl.value = url;
});
        </el-code>
        
        <h3>2. 在HTML中使用签名URL</h3>
        <el-code>
&lt;img :src="signedImageUrl" alt="示例图片" /&gt;
        </el-code>
        
        <h3>3. 上传文件到OSS</h3>
        <el-code>
// 上传文件到OSS
const uploadToOSS = async (file) => {
  try {
    const result = await ossUploader.uploadFile(file, 'images/uploads');
    if (result.success) {
      // 上传成功，获取签名URL以访问私有文件
      const signedUrl = await getSignedUrl(result.name);
      return {
        ...result,
        signedUrl
      };
    }
    return result;
  } catch (error) {
    console.error('上传失败:', error);
    return { success: false, message: error.message };
  }
};
        </el-code>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Picture } from '@element-plus/icons-vue';
import axios from 'axios';

interface OssImage {
  url: string;
  name: string;
  objectPath?: string;
  signedUrl?: string;
}

// OSS图片列表
const ossImages = ref<OssImage[]>([
  {
    url: 'https://campus-system1122.oss-cn-chengdu.aliyuncs.com/images/banner/null.gif',
    name: 'Banner图片',
    objectPath: 'images/banner/null.gif'
  },
  {
    url: 'https://campus-system1122.oss-cn-chengdu.aliyuncs.com/images/logo.png',
    name: 'Logo图片',
    objectPath: 'images/logo.png'
  },
  {
    url: 'https://campus-system1122.oss-cn-chengdu.aliyuncs.com/images/background.jpg',
    name: '背景图片',
    objectPath: 'images/background.jpg'
  }
]);

// 测试图片URL
const testImageUrl = ref('');
const signedTestUrl = ref('');

// 配置检查结果
const configCheckResult = ref<any>(null);

// 复制图片URL到剪贴板
const copyImageUrl = (url: string) => {
  if (!url) {
    ElMessage.warning('没有可用的URL');
    return;
  }
  
  navigator.clipboard.writeText(url)
    .then(() => {
      ElMessage.success('图片URL已复制到剪贴板');
    })
    .catch(err => {
      ElMessage.error('复制失败: ' + err);
    });
};

// 获取签名URL
const getSignedUrl = async (image: OssImage) => {
  if (!image.objectPath) {
    ElMessage.warning('没有设置对象路径');
    return;
  }
  
  try {
    ElMessage.info('正在获取签名URL...');
    const response = await axios.get('/api/oss/get-signed-url', {
      params: { objectName: image.objectPath }
    });
    
    if (response.data && response.data.signedUrl) {
      image.signedUrl = response.data.signedUrl;
      ElMessage.success('签名URL获取成功');
    } else {
      ElMessage.error('获取签名URL失败: 返回数据不完整');
    }
  } catch (error) {
    console.error('获取签名URL失败:', error);
    ElMessage.error('获取签名URL失败: ' + (error as any).message);
  }
};

// 为测试输入获取签名URL
const getSignedUrlForTest = async () => {
  if (!testImageUrl.value) {
    ElMessage.warning('请输入OSS图片路径');
    return;
  }
  
  try {
    ElMessage.info('正在获取签名URL...');
    const response = await axios.get('/api/oss/get-signed-url', {
      params: { objectName: testImageUrl.value }
    });
    
    if (response.data && response.data.signedUrl) {
      signedTestUrl.value = response.data.signedUrl;
      ElMessage.success('签名URL获取成功');
    } else {
      ElMessage.error('获取签名URL失败: 返回数据不完整');
    }
  } catch (error) {
    console.error('获取签名URL失败:', error);
    ElMessage.error('获取签名URL失败: ' + (error as any).message);
  }
};

// 检查OSS配置
const checkOSSConfig = async () => {
  try {
    ElMessage.info('正在检查OSS配置...');
    const response = await axios.get('/api/oss/check-config');
    configCheckResult.value = response.data;
    
    if (response.data.status === 'success') {
      ElMessage.success('OSS配置检查成功');
    } else {
      ElMessage.error('OSS配置检查失败: ' + response.data.message);
    }
  } catch (error) {
    console.error('OSS配置检查失败:', error);
    ElMessage.error('OSS配置检查失败: ' + (error as any).message);
  }
};

// 初始化时为所有图片获取签名URL
onMounted(() => {
  ossImages.value.forEach(image => {
    if (image.objectPath) {
      getSignedUrl(image);
    }
  });
});
</script>

<style scoped>
.oss-image-demo {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.image-display, .image-test, .usage-card, .config-check {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.image-item {
  width: 200px;
  border: 1px solid #eee;
  border-radius: 4px;
  overflow: hidden;
}

.image-info {
  padding: 10px;
}

.image-info p {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #333;
}

.url-test {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.test-result {
  margin-top: 20px;
}

.test-result p {
  margin: 0 0 10px 0;
}

.test-result .el-button {
  margin: 10px 0;
}

.image-preview {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
  padding: 20px;
}

.image-error .el-icon {
  font-size: 40px;
  margin-bottom: 10px;
}

.image-error .el-button {
  margin-top: 10px;
}

h1 {
  text-align: center;
  color: #303133;
  margin-bottom: 20px;
}

.usage-content {
  padding: 10px;
}

.usage-content h3 {
  margin: 15px 0 10px;
  color: #303133;
}

.el-code {
  display: block;
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 12px;
  margin: 10px 0;
  font-family: monospace;
  color: #606266;
  white-space: pre-wrap;
  word-break: break-all;
}

.config-details {
  padding: 10px;
}

.config-details p {
  margin: 5px 0;
}

.config-details .el-button {
  margin: 10px 0;
}
</style> 