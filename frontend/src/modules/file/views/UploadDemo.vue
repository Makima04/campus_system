<template>
  <div class="upload-demo">
    <h1>OSS文件管理</h1>
    
    <!-- 目录浏览与上传部分 -->
    <el-card class="oss-browser-card">
      <template #header>
        <div class="card-header">
          <div class="title-area">
            <span>OSS文件浏览器</span>
          </div>
          <div class="path-navigation">
            <el-input v-model="currentPrefix" placeholder="输入路径前缀">
              <template #append>
                <el-button @click="loadFileList">浏览</el-button>
              </template>
            </el-input>
          </div>
        </div>
      </template>
      
      <!-- 目录路径导航栏 -->
      <div class="path-breadcrumb">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item @click="navigateTo('')">根目录</el-breadcrumb-item>
          <template v-if="currentPrefix">
            <el-breadcrumb-item 
              v-for="(segment, index) in pathSegments" 
              :key="index"
              @click="navigateTo(getPathUpToIndex(index))"
            >
              {{ segment }}
            </el-breadcrumb-item>
          </template>
        </el-breadcrumb>
      </div>
      
      <!-- 文件列表 -->
      <el-table
        :data="ossFiles"
        style="width: 100%"
        v-loading="loading"
        :empty-text="loading ? '加载中...' : '暂无文件'"
      >
        <el-table-column label="文件名" min-width="250">
          <template #default="scope">
            <div class="file-name" :class="{ 'new-file': scope.row.isNew, 'clickable': scope.row.isDirectory }">
              <el-icon v-if="scope.row.isDirectory"><Folder /></el-icon>
              <el-icon v-else><Document /></el-icon>
              <span @click="handleFileClick(scope.row)">
                {{ scope.row.fileName }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="大小" prop="size" width="120">
          <template #default="scope">
            {{ formatFileSize(scope.row.size || 0) }}
          </template>
        </el-table-column>
        <el-table-column label="修改时间" prop="lastModified" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.lastModified) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button 
              v-if="!scope.row.isDirectory" 
              size="small" 
              type="primary"
              @click="downloadFile(scope.row)"
            >
              下载
            </el-button>
            <el-button 
              v-if="!scope.row.isDirectory" 
              size="small" 
              @click="copyUrl(scope.row.url)"
            >
              复制链接
            </el-button>
            <el-button 
              v-if="!scope.row.isDirectory" 
              size="small"
              type="danger"
              @click="confirmDeleteFile(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="upload-area">
        <div class="smart-upload">
          <h3>文件上传</h3>
          <p class="upload-info">支持普通上传和大文件断点续传（大于20MB的文件会自动启用分片上传）</p>
          <el-upload
            :action="''"
            :auto-upload="false"
            :on-change="handleFileChange"
            :limit="5"
            :multiple="true"
            :file-list="uploadFileList"
          >
            <template #trigger>
              <el-button type="primary">选择文件</el-button>
            </template>
            <template #tip>
              <div class="el-upload__tip">
                当前路径: {{ currentPrefix || '根目录' }}
              </div>
            </template>
          </el-upload>
          
          <!-- 上传状态显示 -->
          <div v-if="uploadingFiles" class="upload-progress">
            <div v-for="(file, index) in uploadingItems" :key="index" class="file-progress-item">
              <div class="file-info">
                <div class="file-name">{{ file.name }}</div>
                <div class="file-size">{{ formatFileSize(file.size) }}</div>
              </div>
              <el-progress 
                :percentage="file.progress" 
                :status="file.status"
                :stroke-width="8"
              />
              <div class="upload-type">
                {{ file.isChunked ? '分片上传' : '普通上传' }}
                <el-tag v-if="file.isChunked" size="small" type="warning">断点续传</el-tag>
              </div>
            </div>
          </div>
          
          <el-button 
            type="success" 
            @click="uploadSelectedFiles" 
            :disabled="uploadFileList.length === 0 || uploadingFiles"
            :loading="uploadingFiles"
            style="margin-top: 10px"
          >
            {{ uploadingFiles ? '上传中...' : '开始上传' }}
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
  
  <!-- 删除确认对话框 -->
  <el-dialog
    v-model="deleteDialog.visible"
    title="确认删除"
    width="400px"
  >
    <span>确定要删除文件 "{{ deleteDialog.fileName }}" 吗？此操作不可恢复！</span>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="deleteDialog.visible = false">取消</el-button>
        <el-button type="danger" @click="deleteFile">确定删除</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from '@vue/runtime-dom'
import { ElMessage, ElMessageBox, UploadInstance, UploadUserFile } from 'element-plus'
import { Document, Folder } from '@element-plus/icons-vue'
import { smartUploadFile, listOSSFiles, getFileDownloadUrl, deleteOSSFile, checkFileSecurity, sanitizeFileName } from '@/modules/file/utils/ossUploader'
import { escapeHtml, sanitizeHtml } from '@/utils/security'

interface OSSFile {
  key: string;
  fileName: string;
  url: string;
  size: number;
  lastModified: string;
  isDirectory: boolean;
  isNew?: boolean;
}

// 定义上传文件项的接口
interface UploadItem {
  name: string;
  size: number;
  progress: number;
  status: '' | 'success' | 'exception' | 'warning';
  file: File;
  isChunked: boolean;
}

// 当前目录前缀
const currentPrefix = ref('');
// OSS文件列表
const ossFiles = ref<OSSFile[]>([]);
// 上传文件列表
const uploadFileList = ref<UploadUserFile[]>([]);
// 加载状态
const loading = ref(false);
// 是否正在上传
const uploadingFiles = ref(false);
// 上传中的文件状态
const uploadingItems = ref<UploadItem[]>([]);
// 删除对话框
const deleteDialog = ref({
  visible: false,
  fileName: '',
  fileKey: ''
});

// 计算当前路径的分段
const pathSegments = computed(() => {
  if (!currentPrefix.value) return [];
  
  // 去除末尾的斜杠
  const path = currentPrefix.value.endsWith('/') 
    ? currentPrefix.value.slice(0, -1) 
    : currentPrefix.value;
    
  return path.split('/');
});

// 获取指定索引前的路径
const getPathUpToIndex = (index: number) => {
  return pathSegments.value.slice(0, index + 1).join('/') + '/';
};

// 防抖函数
const debounce = <T extends (...args: any[]) => any>(
  fn: T,
  delay: number
): ((...args: Parameters<T>) => void) => {
  let timer: number | null = null;
  
  return (...args: Parameters<T>) => {
    if (timer) {
      clearTimeout(timer);
    }
    
    timer = window.setTimeout(() => {
      fn(...args);
      timer = null;
    }, delay);
  };
};

// 使用防抖处理文件列表加载
const debouncedLoadFileList = debounce(async () => {
  await loadFileList();
}, 300);

// 使用防抖处理搜索
const debouncedSearch = debounce(async () => {
  await loadFileList();
}, 500);

// 加载文件列表
const loadFileList = async () => {
  loading.value = true;
  ossFiles.value = []; // 清空当前列表
  
  try {
    // 确保路径格式正确
    if (currentPrefix.value && !currentPrefix.value.endsWith('/')) {
      currentPrefix.value += '/';
    }
    
    console.log('正在获取文件列表，路径:', currentPrefix.value || '根目录');
    const result = await listOSSFiles(currentPrefix.value);
    
    if (result.success) {
      const filesCount = result.files?.length || 0;
      console.log(`成功获取文件列表: ${filesCount} 个文件，当前路径: ${currentPrefix.value || '根目录'}`);
      
      // 处理文件列表
      ossFiles.value = (result.files || []).map((file: any) => {
        const key = file.key || '';
        const isDir = file.isDirectory || key.endsWith('/');
        
        // 获取文件名
        let fileName = key;
        if (currentPrefix.value && key.startsWith(currentPrefix.value)) {
          fileName = key.substring(currentPrefix.value.length);
        }
        
        // 去除目录末尾的斜杠
        if (isDir && fileName.endsWith('/')) {
          fileName = fileName.slice(0, -1);
        }
        
        // 如果是目录且文件名为空，表示当前目录
        if (isDir && fileName === '') {
          fileName = '当前目录';
        }
        
        // 对文件名进行安全处理
        fileName = sanitizeFileName(fileName);
        
        console.log(`处理文件: ${key}, 文件名: ${fileName}, 是否为目录: ${isDir}`);
        
        return {
          key,
          fileName: fileName || '根目录',
          url: file.url || '',
          size: file.size || 0,
          lastModified: file.lastModified || new Date().toISOString(),
          isDirectory: isDir
        };
      });
      
      // 过滤掉空目录条目
      ossFiles.value = ossFiles.value.filter(file => 
        !(file.isDirectory && (file.fileName === '当前目录' || file.fileName === ''))
      );
      
      console.log('处理后的文件列表:', ossFiles.value);
    } else {
      ElMessage.error({
        message: result.error || '获取文件列表失败',
        duration: 5000,
        showClose: true
      });
      ossFiles.value = [];
    }
  } catch (error) {
    console.error('加载文件列表出错:', error);
    ElMessage.error({
      message: '加载文件列表失败，请检查网络连接或后端服务状态',
      duration: 5000,
      showClose: true
    });
    ossFiles.value = [];
  } finally {
    loading.value = false;
  }
};

// 导航到指定路径
const navigateTo = (path: string) => {
  console.log('导航到路径:', path);
  currentPrefix.value = path;
  debouncedLoadFileList();
};

// 处理文件点击
const handleFileClick = async (file: OSSFile) => {
  if (file.isDirectory) {
    console.log('点击目录:', file.key);
    navigateTo(file.key);
  } else {
    try {
      // 对文件名进行安全处理
      const safeFileName = escapeHtml(file.fileName);
      const safeUrl = sanitizeHtml(file.url);
      
      // 使用安全的URL打开文件
      window.open(safeUrl, '_blank');
    } catch (error) {
      console.error('打开文件失败:', error);
      ElMessage.error('打开文件失败');
    }
  }
};

// 文件选择变更
const handleFileChange = (uploadFile: UploadUserFile) => {
  if (!uploadFile.raw) {
    ElMessage.warning('无法获取文件内容');
    return;
  }
  
  // 进行文件安全检查
  const securityCheck = checkFileSecurity(uploadFile.raw);
  if (!securityCheck.isValid) {
    ElMessage.error(securityCheck.message || '文件安全检查未通过');
    return;
  }
  
  // 如果文件名被修改，更新文件名
  if (securityCheck.sanitizedFileName && securityCheck.sanitizedFileName !== uploadFile.name) {
    uploadFile.name = securityCheck.sanitizedFileName;
    ElMessage.info('文件名已安全处理');
  }
  
  // 将文件添加到上传列表
  uploadFileList.value.push(uploadFile);
};

// 上传选中的文件
const uploadSelectedFiles = async () => {
  if (uploadFileList.value.length === 0) {
    ElMessage.warning('请先选择文件');
    return;
  }
  
  uploadingFiles.value = true;
  uploadingItems.value = [];
  
  // 初始化上传状态列表
  uploadingItems.value = uploadFileList.value
    .filter(item => item.raw)
    .map(item => ({
      name: item.name,
      size: item.raw?.size || 0,
      progress: 0,
      status: '',
      file: item.raw as File,
      isChunked: (item.raw?.size || 0) > 20 * 1024 * 1024 // 20MB
    }));
  
  try {
    console.log('开始上传文件到路径:', currentPrefix.value);
    
    // 创建上传任务数组
    const uploadTasks = uploadingItems.value.map(async (item, index) => {
      try {
        // 使用智能上传函数，根据文件大小自动选择上传方式
        // 确保当前路径正确传递，并移除结尾的斜杠以确保一致性
        let uploadPath = '/';  // 默认为根目录
        if (currentPrefix.value) {
          // 如果有指定目录，则使用该目录
          uploadPath = currentPrefix.value.endsWith('/') ? currentPrefix.value : currentPrefix.value + '/';
        }
        console.log(`上传文件 ${item.name} 到路径: ${uploadPath}`);
        
        const result = await smartUploadFile(
          item.file, 
          uploadPath,
          (progress, isChunked) => {
            // 更新上传进度
            item.progress = progress;
            item.isChunked = isChunked;
            
            // 设置状态
            if (progress === 100) {
              item.status = 'success';
            }
          }
        );
        
        if (result.success) {
          item.progress = 100;
          item.status = 'success';
          console.log(`文件 ${item.name} 上传成功:`, result);
          return { 
            success: true, 
            fileName: item.name,
            isChunked: result.isChunked,
            fileInfo: result  // 保存完整的上传结果信息
          };
        } else {
          item.status = 'exception';
          console.error(`文件 ${item.name} 上传失败:`, result.error);
          return { 
            success: false, 
            error: result.error,
            fileName: item.name,
            isChunked: result.isChunked
          };
        }
      } catch (error) {
        item.status = 'exception';
        console.error(`文件 ${item.name} 上传错误:`, error);
        return { 
          success: false, 
          error: error instanceof Error ? error.message : '上传过程中发生错误',
          fileName: item.name
        };
      }
    });
    
    // 等待所有上传任务完成
    const results = await Promise.all(uploadTasks);
    
    // 统计上传结果
    const succeeded = results.filter((r: any) => r.success).length;
    const failed = results.length - succeeded;
    
    // 显示上传结果消息
    if (succeeded > 0) {
      ElMessage.success(`成功上传 ${succeeded} 个文件`);
    }
    
    if (failed > 0) {
      const failedFiles = results
        .filter((r: any) => !r.success)
        .map((r: any) => r.fileName || '未知文件')
        .join(', ');
      
      ElMessage.error({
        message: `${failed} 个文件上传失败${failedFiles ? ': ' + failedFiles : ''}`,
        duration: 5000,
        showClose: true
      });
    }
    
    // 如果有任何成功上传的文件
    if (succeeded > 0) {
      // 先从视图中立即删除列表
      uploadFileList.value = [];
      
      // 确保延迟刷新，等待后端处理完成
      setTimeout(async () => {
        console.log('延迟刷新文件列表，确保后端已完成处理');
        
        // 对于大文件，给出更长的等待时间
        const hasLargeFiles = results.some((r: any) => r.isChunked);
        if (hasLargeFiles) {
          console.log('检测到大文件上传，等待更长时间确保处理完成');
          // 再等待4秒
          await new Promise(resolve => setTimeout(resolve, 4000));
        }
        
        // 立即刷新文件列表，确保显示最新状态
        await loadFileList();
        
        // 找到成功上传的文件并突出显示
        const successFiles = results
          .filter((r: any) => r.success && r.fileInfo)
          .map((r: any) => r.fileInfo.key || '');
        
        console.log('要高亮显示的文件:', successFiles);
        
        if (successFiles.length > 0) {
          // 突出显示新上传的文件
          highlightNewFiles(successFiles);
        }
        
        // 上传后验证文件是否真的存在
        setTimeout(async () => {
          console.log('验证上传文件是否在文件列表中...');
          const fileFound = verifyFilesExist(successFiles);
          if (!fileFound && hasLargeFiles) {
            console.warn('未在列表中找到上传的大文件，可能需要手动刷新');
            ElMessage.warning({
              message: '大文件处理可能仍在进行中，可能需要手动刷新页面查看最新文件',
              duration: 5000,
              showClose: true
            });
          }
        }, 1000);
      }, 1000); // 等待1秒，确保后端处理完成
    }
    
    // 延迟清除上传状态列表，让用户能看到完成状态
    setTimeout(() => {
      uploadingFiles.value = false;
      uploadingItems.value = [];
    }, 2000);
    
  } catch (error) {
    console.error('上传文件过程错误:', error);
    ElMessage.error({
      message: `上传过程中出现错误: ${error instanceof Error ? error.message : '未知错误'}`,
      duration: 5000,
      showClose: true
    });
    
    uploadingFiles.value = false;
  }
};

// 突出显示新上传的文件的函数
const highlightNewFiles = (fileKeys: string[]) => {
  console.log('调用高亮函数，文件keys:', fileKeys, '当前文件列表:', ossFiles.value);
  
  // 找到对应的DOM元素并添加高亮效果
  nextTick(() => {
    if (fileKeys.length === 0 || ossFiles.value.length === 0) {
      console.log('没有文件可以高亮显示');
      return;
    }
    
    // 转换为小写并且规范化路径以便于比较
    const normalizedKeys = fileKeys.map(key => 
      key.toLowerCase().replace(/^\/+|\/+$/g, '')
    );
    
    // 检查每个文件是否需要高亮
    ossFiles.value.forEach((file, index) => {
      const normalizedFileKey = file.key.toLowerCase().replace(/^\/+|\/+$/g, '');
      
      // 尝试多种匹配方式
      const isMatch = normalizedKeys.some(key => {
        const normalizedKey = key.toLowerCase().replace(/^\/+|\/+$/g, '');
        
        // 1. 完全匹配
        if (normalizedFileKey === normalizedKey) return true;
        
        // 2. 文件名匹配（忽略路径）
        const fileName = normalizedFileKey.split('/').pop() || '';
        const keyFileName = normalizedKey.split('/').pop() || '';
        if (fileName && keyFileName && fileName === keyFileName) return true;
        
        // 3. 部分匹配（文件名包含在key中）
        if (normalizedFileKey.includes(normalizedKey)) return true;
        if (normalizedKey.includes(normalizedFileKey)) return true;
        
        // 4. 只匹配不带时间戳的文件名部分
        // 提取基本文件名（不含时间戳部分）
        const baseFileName = fileName.replace(/(_\d+)?\.[^.]+$/, '');
        const baseKeyFileName = keyFileName.replace(/(_\d+)?\.[^.]+$/, '');
        
        if (baseFileName && baseKeyFileName && 
            (baseFileName.includes(baseKeyFileName) || baseKeyFileName.includes(baseFileName))) {
          return true;
        }
        
        return false;
      });
      
      if (isMatch) {
        console.log(`找到匹配的文件: ${file.fileName}`);
        // 标记这个文件是新上传的
        file.isNew = true;
        
        // 3秒后移除高亮效果
        setTimeout(() => {
          if (ossFiles.value[index]) {
            ossFiles.value[index].isNew = false;
          }
        }, 3000);
      }
    });
  });
};

// 下载文件
const downloadFile = async (file: OSSFile) => {
  try {
    const result = await getFileDownloadUrl(file.key);
    
    if (result.success && result.url) {
      // 创建一个临时链接并点击它以触发下载
      const link = document.createElement('a');
      link.href = result.url;
      link.download = file.fileName;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } else {
      // 处理会话过期错误
      if (result.error && (result.error.includes('会话已过期') || result.error.includes('重新登录'))) {
        ElMessageBox.confirm(
          '您的登录会话已过期，需要重新登录才能下载文件。',
          '会话过期',
          {
            confirmButtonText: '立即登录',
            cancelButtonText: '取消',
            type: 'warning',
          }
        ).then(() => {
          // 清除本地存储的令牌和其他会话信息
          localStorage.removeItem('token');
          
          // 保存当前页面URL，以便登录后返回
          localStorage.setItem('redirectUrl', window.location.href);
          
          // 重定向到登录页面
          window.location.href = '/login';
        }).catch(() => {
          // 用户取消了登录，不做任何操作
        });
      } else {
        ElMessage.error(result.error || '获取下载链接失败');
      }
    }
  } catch (error) {
    console.error('下载文件出错:', error);
    ElMessage.error('下载文件失败');
  }
};

// 复制URL
const copyUrl = (url: string) => {
  if (!url) {
    ElMessage.warning('没有可用的URL');
    return;
  }
  
  navigator.clipboard.writeText(url)
    .then(() => {
      ElMessage.success('链接已复制到剪贴板');
    })
    .catch(err => {
      console.error('复制失败:', err);
      ElMessage.error('复制失败');
    });
};

// 格式化文件大小
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B';
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB';
  if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
  return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
};

// 格式化日期
const formatDate = (dateStr: string): string => {
  if (!dateStr) return '-';
  try {
    const date = new Date(dateStr);
    return date.toLocaleString();
  } catch (e) {
    return dateStr;
  }
};

// 确认删除文件
const confirmDeleteFile = (file: OSSFile) => {
  deleteDialog.value.visible = true;
  deleteDialog.value.fileName = file.fileName;
  deleteDialog.value.fileKey = file.key;
  
  // 如果是目录，确保路径以/结尾
  if (file.isDirectory && !deleteDialog.value.fileKey.endsWith('/')) {
    deleteDialog.value.fileKey += '/';
  }
};

// 删除文件
const deleteFile = async () => {
  try {
    const isDirectory = deleteDialog.value.fileKey.endsWith('/');
    console.log(`正在删除${isDirectory ? '目录' : '文件'}:`, deleteDialog.value.fileKey);
    
    // 检查用户是否已登录
    const token = localStorage.getItem('token');
    if (!token) {
      ElMessageBox.confirm(
        '您需要登录才能删除文件。是否前往登录页面？',
        '需要登录',
        {
          confirmButtonText: '前往登录',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        window.location.href = '/login';
      });
      return;
    }
    
    // 如果是目录，需要二次确认
    if (isDirectory) {
      const confirmResult = await ElMessageBox.confirm(
        `您将删除目录"${deleteDialog.value.fileName}"及其所有内容，此操作不可恢复。确定要继续吗？`,
        '删除目录确认',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).catch(() => 'cancel');
      
      if (confirmResult === 'cancel') {
        deleteDialog.value.visible = false; // 用户取消，关闭对话框
        return;
      }
    }
    
    // 显示加载中状态
    loading.value = true;
    
    try {
      const result = await deleteOSSFile(deleteDialog.value.fileKey);
      
      // 无论成功失败，先关闭对话框
      deleteDialog.value.visible = false;
      
      if (result.success) {
        ElMessage.success(`${isDirectory ? '目录' : '文件'}删除成功`);
        
        // 保存当前目录路径
        const currentPath = currentPrefix.value;
        
        // 即时从列表中删除文件/目录
        const deletedKey = deleteDialog.value.fileKey;
        ossFiles.value = ossFiles.value.filter(file => file.key !== deletedKey);
        
        // 刷新文件列表 - 完整刷新以确保和服务器同步
        await loadFileList();
      } else {
        // 处理不同的错误情况
        if (result.error && result.error.includes('403')) {
          ElMessageBox.confirm(
            `您没有删除此${isDirectory ? '目录' : '文件'}的权限。只有管理员用户可以删除${isDirectory ? '目录' : '文件'}。`,
            '权限不足',
            {
              confirmButtonText: '确定',
              type: 'warning',
              showCancelButton: false
            }
          );
        } 
        // 处理会话过期错误
        else if (result.error && (result.error.includes('会话已过期') || result.error.includes('重新登录'))) {
          ElMessageBox.confirm(
            '您的登录会话已过期，需要重新登录才能继续操作。',
            '会话过期',
            {
              confirmButtonText: '立即登录',
              cancelButtonText: '取消',
              type: 'warning',
            }
          ).then(() => {
            // 清除本地存储的令牌和其他会话信息
            localStorage.removeItem('token');
            
            // 保存当前页面URL，以便登录后返回
            localStorage.setItem('redirectUrl', window.location.href);
            
            // 重定向到登录页面
            window.location.href = '/login';
          }).catch(() => {
            // 用户取消了登录，不做任何操作
          });
        }
        else {
          ElMessage({
            message: result.error || `删除${isDirectory ? '目录' : '文件'}失败`,
            type: 'error',
            duration: 5000,
            showClose: true,
            grouping: true
          });
        }
      }
    } catch (error) {
      // 出错时也关闭对话框
      deleteDialog.value.visible = false;
      console.error('删除出错:', error);
      ElMessage.error({
        message: `删除失败: ${error instanceof Error ? error.message : '未知错误'}`,
        duration: 5000,
        showClose: true
      });
    } finally {
      // 最后确保加载状态恢复
      loading.value = false;
    }
  } catch (error) {
    deleteDialog.value.visible = false;
    console.error('删除处理过程出错:', error);
    ElMessage.error({
      message: `处理失败: ${error instanceof Error ? error.message : '未知错误'}`,
      duration: 5000,
      showClose: true
    });
    loading.value = false;
  }
};

// 验证上传的文件是否存在于文件列表中
const verifyFilesExist = (fileKeys: string[]): boolean => {
  if (!fileKeys.length || !ossFiles.value.length) return false;
  
  // 规范化所有文件key
  const normalizedKeys = fileKeys.map(key => 
    key.toLowerCase().replace(/^\/+|\/+$/g, '')
  );
  
  // 规范化文件列表中的所有key
  const normalizedFileKeys = ossFiles.value.map(file => 
    file.key.toLowerCase().replace(/^\/+|\/+$/g, '')
  );
  
  // 检查是否至少有一个文件存在
  for (const key of normalizedKeys) {
    // 尝试找到文件名
    const fileName = key.split('/').pop() || '';
    
    // 检查完全匹配
    if (normalizedFileKeys.some(fileKey => fileKey === key)) {
      console.log(`文件验证成功: 完全匹配 ${key}`);
      return true;
    }
    
    // 检查文件名匹配
    if (fileName && ossFiles.value.some(file => {
      const ossFileName = file.key.split('/').pop() || '';
      return ossFileName === fileName;
    })) {
      console.log(`文件验证成功: 文件名匹配 ${fileName}`);
      return true;
    }
    
    // 检查部分匹配
    if (ossFiles.value.some(file => {
      const normalizedFileKey = file.key.toLowerCase().replace(/^\/+|\/+$/g, '');
      return normalizedFileKey.includes(key) || key.includes(normalizedFileKey);
    })) {
      console.log(`文件验证成功: 部分匹配 ${key}`);
      return true;
    }
  }
  
  console.warn('文件验证失败: 未找到上传的文件', {
    uploadedKeys: normalizedKeys,
    fileListKeys: normalizedFileKeys
  });
  
  return false;
};

// 页面加载时获取文件列表
onMounted(() => {
  loadFileList();
});
</script>

<style scoped>
.upload-demo {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-area {
  font-size: 16px;
  font-weight: bold;
}

.path-navigation {
  width: 50%;
}

.path-breadcrumb {
  margin-bottom: 20px;
}

.file-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 新上传文件的高亮样式 */
.file-name.new-file {
  animation: highlight-fade 3s ease-in-out;
}

@keyframes highlight-fade {
  0%, 50% {
    background-color: rgba(64, 158, 255, 0.2);
    border-radius: 4px;
    padding: 4px;
    margin: -4px;
  }
  100% {
    background-color: transparent;
  }
}

.clickable {
  color: #409eff;
  cursor: pointer;
}

.clickable:hover {
  text-decoration: underline;
}

.upload-area {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.smart-upload {
  margin-bottom: 20px;
}

.oss-browser-card {
  margin-bottom: 20px;
}

h1 {
  text-align: center;
  color: #303133;
  margin-bottom: 20px;
}

h3 {
  margin-bottom: 15px;
  color: #606266;
}

.upload-progress {
  margin-bottom: 10px;
}

.file-progress-item {
  margin-bottom: 10px;
}

.file-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.upload-type {
  margin-top: 5px;
  font-size: 0.8em;
}

.upload-info {
  color: #606266;
  font-size: 0.9em;
  margin-bottom: 10px;
}
</style> 