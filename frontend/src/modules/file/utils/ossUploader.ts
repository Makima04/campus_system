import axios from 'axios';
import { getApiBaseUrl } from '@/utils/api';
import { chunkUploader } from '@/utils/chunkUploader';

// 定义智能上传阈值 (20MB)
const CHUNK_UPLOAD_THRESHOLD = 20 * 1024 * 1024;

// 分片大小定义为5MB
const DEFAULT_CHUNK_SIZE = 5 * 1024 * 1024;

// 定义ResultDTO接口
interface ResultDTO<T> {
  code: number;
  message: string;
  data: T;
}

// 定义OSS签名响应接口
interface OSSSignature {
  accessId: string;
  policy: string;
  signature: string;
  dir: string;
  host: string;
  expire: string;
}

// 定义上传结果接口
export interface UploadResult {
  success: boolean;
  fileUrl?: string;
  url?: string;
  key?: string;
  bucket?: string;
  error?: string;
  details?: any;
  isChunked?: boolean;
}

// 定义上传进度回调
export interface UploadProgressCallback {
  (progress: number, isChunked: boolean): void;
}

// 添加接口定义
interface MultipartUploadInitResult {
  success: boolean;
  data?: {
    uploadId: string;
    objectKey: string;
  };
  error?: string;
}

interface UploadPartUrlResult {
  success: boolean;
  data?: {
    uploadUrl: string;
    partNumber: string;
  };
  error?: string;
}

interface CompleteMultipartResult {
  success: boolean;
  data?: {
    etag: string;
    fileUrl: string;
  };
  error?: string;
}

// 定义API响应接口
interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
}

/**
 * 获取OSS签名
 */
const getOSSSignature = async (): Promise<OSSSignature> => {
  try {
    const apiBaseUrl = getApiBaseUrl();
    
    // 检查是否在开发环境中
    const isDev = (import.meta as any).env.DEV;
    
    // 构建正确的API URL
    let baseURL;
    if (isDev) {
      // 使用直接的后端URL
      const serverHost = window.location.hostname;
      const serverPort = 80; // 后端默认端口
      baseURL = `http://${serverHost}:${serverPort}/api`;
      console.log('开发环境下获取签名使用直接后端URL:', baseURL);
    } else {
      baseURL = apiBaseUrl;
    }
    
    // 创建一个配置了baseURL的axios实例
    const apiClient = axios.create({
      baseURL,
      timeout: 15000,
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Accept': 'application/json'
      }
    });
    
    console.log('发送OSS签名请求到:', `${baseURL}/oss/signature`);
    
    const response = await apiClient.get<OSSSignature>('/oss/signature');
    
    // 检查响应是否是HTML (可能是Vite开发服务器的响应)
    if (typeof response.data === 'string' && (response.data as string).trim().startsWith('<!DOCTYPE html>')) {
      console.error('收到HTML响应而非JSON，可能是请求错误地路由到了前端开发服务器');
      throw new Error('获取上传签名失败: API请求被误导向前端开发服务器');
    }
    
    if (response.data && response.data.accessId) {
      console.log('成功获取OSS签名');
      return response.data;
    } else {
      console.error('签名响应数据不完整:', response.data);
      throw new Error('获取上传签名失败: 返回数据不完整');
    }
  } catch (error) {
    console.error('获取上传签名错误:', error);
    throw error;
  }
};

// 自定义上传进度事件接口
interface ProgressEvent {
  loaded: number;
  total?: number;
}

/**
 * 文件安全检查
 * @param file 要检查的文件
 * @returns 检查结果，包含是否通过和安全信息
 */
export const checkFileSecurity = (file: File): { 
  isValid: boolean; 
  message?: string;
  sanitizedFileName?: string;
} => {
  // 文件大小限制 (100MB)
  const maxSize = 100 * 1024 * 1024;
  if (file.size > maxSize) {
    return { 
      isValid: false, 
      message: `文件大小超过限制 (最大 ${formatFileSize(maxSize)})` 
    };
  }

  // 获取文件扩展名
  const extension = file.name.split('.').pop()?.toLowerCase() || '';
  
  // 允许的文件类型
  const allowedTypes = [
    // 文档
    'pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'md',
    // 图片
    'jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp',
    // 音频
    'mp3', 'wav', 'ogg',
    // 视频
    'mp4', 'avi', 'mov', 'wmv',
    // 压缩文件
    'zip', 'rar', '7z', 'tar', 'gz'
  ];
  
  // 检查文件类型
  if (!allowedTypes.includes(extension)) {
    return { 
      isValid: false, 
      message: `不支持的文件类型: ${extension}` 
    };
  }
  
  // 检查文件名是否包含恶意字符
  const sanitizedFileName = sanitizeFileName(file.name);
  if (sanitizedFileName !== file.name) {
    return { 
      isValid: true, 
      message: '文件名已安全处理', 
      sanitizedFileName 
    };
  }
  
  return { isValid: true };
};

/**
 * 清理文件名，移除不安全字符
 * @param fileName 原始文件名
 * @returns 安全处理后的文件名
 */
export const sanitizeFileName = (fileName: string): string => {
  // 移除路径分隔符和特殊字符
  let safeName = fileName.replace(/[\\/:*?"<>|]/g, '_');
  
  // 限制文件名长度
  const maxLength = 100;
  if (safeName.length > maxLength) {
    const extension = safeName.split('.').pop() || '';
    const nameWithoutExt = safeName.substring(0, safeName.lastIndexOf('.'));
    safeName = nameWithoutExt.substring(0, maxLength - extension.length - 1) + '.' + extension;
  }
  
  return safeName;
};

/**
 * 格式化文件大小显示
 * @param bytes 文件大小（字节）
 * @returns 格式化后的文件大小字符串
 */
export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

/**
 * 智能文件上传 - 根据文件大小自动选择普通上传或分片上传
 * @param file 要上传的文件
 * @param customPath 自定义路径，如folder/subfolder/
 * @param onProgress 上传进度回调函数
 * @returns 上传成功后的文件URL和其它信息
 */
export const smartUploadFile = async (
  file: File, 
  customPath: string = '',
  onProgress?: UploadProgressCallback
): Promise<UploadResult> => {
  try {
    // 添加文件安全检查
    const securityCheck = checkFileSecurity(file);
    if (!securityCheck.isValid) {
      return {
        success: false,
        error: securityCheck.message || '文件安全检查未通过'
      };
    }
    
    // 使用安全处理后的文件名
    const safeFileName = securityCheck.sanitizedFileName || file.name;
    
    // 根据文件大小选择上传方式
    const isLargeFile = file.size > 20 * 1024 * 1024; // 20MB
    
    if (isLargeFile) {
      console.log(`使用分片上传大文件: ${safeFileName}, 大小: ${formatFileSize(file.size)}`);
      return await uploadLargeFile(file, customPath, onProgress);
    } else {
      console.log(`使用普通上传文件: ${safeFileName}, 大小: ${formatFileSize(file.size)}`);
      return await uploadToOSS(file, customPath, onProgress);
    }
  } catch (error) {
    console.error('文件上传失败:', error);
    return {
      success: false,
      error: error instanceof Error ? error.message : '上传过程中发生未知错误'
    };
  }
};

/**
 * 上传大文件（使用OSS分片上传）
 * @param file 要上传的文件
 * @param customPath 自定义路径，如folder/subfolder/
 * @param onProgress 上传进度回调函数
 */
const uploadLargeFile = async (file: File, customPath: string = '', onProgress?: UploadProgressCallback): Promise<UploadResult> => {
  try {
    const fileName = file.name;
    const fileSize = file.size;
    
    // 步骤1：初始化分片上传任务，包含用户指定路径
    let uploadPath;
    
    // 检查是否是根目录路径
    const isRootPath = customPath === '/' || customPath === '';
    
    if (isRootPath) {
      // 用户想要上传到根目录，不添加任何前缀
      uploadPath = fileName;
      console.log(`用户指定上传到根目录，完整文件路径: ${uploadPath}`);
    } else if (customPath) {
      // 使用用户指定路径（确保以/结尾）
      const path = customPath.endsWith('/') ? customPath : `${customPath}/`;
      uploadPath = `${path}${fileName}`;
      console.log(`使用用户指定路径上传文件: ${uploadPath}`);
    } else {
      uploadPath = fileName;
      console.log(`未指定路径，直接使用文件名: ${uploadPath}`);
    }
    
    console.log(`使用上传路径: ${uploadPath}`);
    
    // 初始化分片上传
    console.log('开始初始化分片上传，上传路径:', uploadPath);
    const initResult = await initMultipartUpload(uploadPath, customPath);
    if (!initResult.success || !initResult.data) {
      return {
        success: false,
        error: initResult.error || '初始化分片上传失败',
        isChunked: true
      };
    }
    
    const { uploadId, objectKey } = initResult.data;
    console.log(`分片上传初始化成功, uploadId: ${uploadId}, objectKey: ${objectKey}`);
    
    // 步骤2：计算分片数量
    const chunkSize = DEFAULT_CHUNK_SIZE;
    const chunkCount = Math.ceil(fileSize / chunkSize);
    console.log(`文件大小: ${fileSize} 字节, 分片大小: ${chunkSize} 字节, 分片数量: ${chunkCount}`);
    
    // 步骤3：并行上传所有分片
    const uploadPromises: Promise<{partNumber: number, eTag: string}>[] = [];
    let completedChunks = 0;
    
    // 用于跟踪失败的分片
    const failedParts: number[] = [];
    
    for (let i = 0; i < chunkCount; i++) {
      const startPos = i * chunkSize;
      const endPos = Math.min(startPos + chunkSize, fileSize);
      const chunkBlob = file.slice(startPos, endPos);
      
      // 创建上传任务但不立即执行
      const uploadPromise = (async () => {
        const partNumber = i + 1; // 分片编号从1开始
        
        let retryCount = 0;
        const maxRetries = 3;
        
        while (retryCount < maxRetries) {
          try {
            // 获取分片上传URL
            const urlResult = await getUploadPartUrl(objectKey, uploadId, partNumber);
            if (!urlResult.success || !urlResult.data) {
              throw new Error(`获取分片${partNumber}上传URL失败: ${urlResult.error}`);
            }
            
            // 使用预签名URL上传分片
            const partResult = await uploadPart(urlResult.data.uploadUrl, chunkBlob);
            
            // 上传完成后更新进度
            completedChunks++;
            if (onProgress) {
              // 计算总体进度，留5%给合并操作
              const progress = Math.floor((completedChunks / chunkCount) * 95);
              onProgress(progress, true);
            }
            
            console.log(`分片${partNumber}上传成功，ETag: ${partResult.eTag}`);
            return {
              partNumber,
              eTag: partResult.eTag
            };
          } catch (error) {
            retryCount++;
            const errorMsg = error instanceof Error ? error.message : String(error);
            console.warn(`分片${partNumber}上传失败(尝试${retryCount}/${maxRetries}): ${errorMsg}`);
            
            // 如果是403错误，等待更长时间再重试
            if (errorMsg.includes('403') || errorMsg.includes('Forbidden')) {
              console.warn(`分片${partNumber}遇到403错误，可能是签名已过期，重新获取URL`);
              await new Promise(resolve => setTimeout(resolve, 2000)); // 等待2秒后重试
            } else {
              await new Promise(resolve => setTimeout(resolve, 1000)); // 默认等待1秒
            }
            
            // 达到最大重试次数
            if (retryCount >= maxRetries) {
              failedParts.push(partNumber);
              throw new Error(`分片${partNumber}上传失败，已达到最大重试次数: ${errorMsg}`);
            }
          }
        }
        
        // 这里永远不会到达，但TypeScript需要返回值
        throw new Error('意外的执行流程');
      })();
      
      uploadPromises.push(uploadPromise);
    }
    
    // 等待所有分片上传完成
    console.log('开始上传所有分片...');
    
    try {
      const parts = await Promise.all(uploadPromises);
      console.log('所有分片上传完成，准备合并文件', parts);
      
      // 步骤4：完成分片上传（合并文件）
      if (onProgress) {
        onProgress(95, true);  // 指示合并文件开始
      }
      
      const completeResult = await completeMultipartUpload(objectKey, uploadId, parts);
      if (!completeResult.success || !completeResult.data) {
        throw new Error(`完成分片上传失败: ${completeResult.error}`);
      }
      
      if (onProgress) {
        onProgress(100, true);  // 上传完成
      }
      
      // 构造完整的文件URL
      let fileUrl = completeResult.data.fileUrl;
      // 检查是否为相对路径，如果是则构建完整URL
      if (fileUrl.startsWith('/')) {
        const baseUrl = getApiBaseUrl();
        const baseUrlWithoutApi = baseUrl.endsWith('/api')
          ? baseUrl.substring(0, baseUrl.length - 4)
          : baseUrl;
        fileUrl = `${baseUrlWithoutApi}${fileUrl}`;
      }
      
      console.log('文件上传成功，URL:', fileUrl);
      
      return {
        success: true,
        fileUrl,
        url: fileUrl,
        key: objectKey,
        bucket: fileUrl.split('.')[0].replace('https://', ''),
        isChunked: true
      };
    } catch (error) {
      // 如果有分片上传失败，尝试中止上传
      console.error('分片上传过程出错，尝试中止上传:', error);
      try {
        if (failedParts.length > 0) {
          console.error(`以下分片上传失败: ${failedParts.join(', ')}`);
        }
        const abortResult = await abortMultipartUpload(objectKey, uploadId);
        if (abortResult.success) {
          console.log('已成功中止分片上传');
        } else {
          console.warn('中止分片上传失败:', abortResult.error);
        }
      } catch (abortError) {
        console.error('中止分片上传时发生错误:', abortError);
      }
      throw error;
    }
  } catch (error) {
    console.error('分片上传失败:', error);
    return {
      success: false,
      error: error instanceof Error ? error.message : '分片上传失败',
      isChunked: true
    };
  }
};

/**
 * 初始化分片上传
 * @param filePath 文件完整路径（包含文件名）
 * @param customPath 自定义上传路径（可选）
 */
async function initMultipartUpload(filePath: string, customPath?: string): Promise<MultipartUploadInitResult> {
  try {
    let objectKey = filePath;
    
    // 根据 customPath 处理 objectKey
    if (customPath !== undefined) {
      const fileName = filePath.split('/').pop() || filePath;
      
      if (customPath === '' || customPath === '/') {
        // 上传到根目录
        objectKey = fileName;
        console.log('将文件上传到根目录:', objectKey);
      } else {
        // 上传到指定目录
        const formattedPath = customPath.endsWith('/') ? customPath : `${customPath}/`;
        objectKey = `${formattedPath}${fileName}`;
        console.log('将文件上传到指定目录:', objectKey);
      }
    } else {
      console.log('使用原始路径上传文件:', objectKey);
    }
    
    const apiBaseUrl = getApiBaseUrl();
    
    // 创建一个配置了认证头的axios实例
    const authToken = localStorage.getItem('token');
    const headers: Record<string, string> = {
      'Content-Type': 'application/json'
    };
    
    if (authToken) {
      headers['Authorization'] = `Bearer ${authToken}`;
    }
    
    console.log('初始化分片上传请求URL:', `${apiBaseUrl}/oss/multipart/init`);
    console.log('上传对象路径:', objectKey);
    
    const response = await axios.post<ApiResponse<{uploadId: string; objectKey: string;}>>(
      `${apiBaseUrl}/oss/multipart/init`,
      null,
      {
        params: { objectKey },
        headers
      }
    );

    if (response.data.code === 200 && response.data.data) {
      return {
        success: true,
        data: response.data.data
      };
    } else {
      console.error('初始化分片上传请求成功但返回错误:', response.data);
      return {
        success: false,
        error: response.data.message || '初始化分片上传失败'
      };
    }
  } catch (error) {
    console.error('初始化分片上传错误:', error);
    return {
      success: false,
      error: error instanceof Error ? error.message : '初始化分片上传失败'
    };
  }
}

/**
 * 获取分片上传URL
 * @param objectKey 对象键（文件路径）
 * @param uploadId 上传ID
 * @param partNumber 分片编号
 */
async function getUploadPartUrl(objectKey: string, uploadId: string, partNumber: number): Promise<UploadPartUrlResult> {
  try {
    const apiBaseUrl = getApiBaseUrl();
    
    // 创建认证头
    const authToken = localStorage.getItem('token');
    const headers: Record<string, string> = {
      'Content-Type': 'application/json'
    };
    
    if (authToken) {
      headers['Authorization'] = `Bearer ${authToken}`;
    }
    
    console.log('获取分片上传URL:', `${apiBaseUrl}/oss/multipart/upload-url`);
    
    const response = await axios.get<ApiResponse<{uploadUrl: string; partNumber: string;}>>(
      `${apiBaseUrl}/oss/multipart/upload-url`,
      {
        params: {
          objectKey,
          uploadId,
          partNumber
        },
        headers
      }
    );
    
    if (response.data.code === 200 && response.data.data) {
      return {
        success: true,
        data: response.data.data
      };
    } else {
      console.error('获取分片上传URL失败:', response.data);
      return {
        success: false,
        error: response.data.message || '获取分片上传URL失败'
      };
    }
  } catch (error) {
    console.error('获取分片上传URL错误:', error);
    return {
      success: false,
      error: error instanceof Error ? error.message : '获取分片上传URL失败'
    };
  }
}

/**
 * 上传单个分片
 * @param url 预签名URL
 * @param blob 分片数据
 */
async function uploadPart(url: string, blob: Blob): Promise<{success: boolean, eTag: string, error?: string}> {
  try {
    console.log('上传分片到URL:', url);
    
    // 创建使用fetch来上传，而不是axios，以便更好地控制请求
    const response = await fetch(url, {
      method: 'PUT',
      body: blob,
      headers: {
        'Content-Type': 'application/octet-stream'
      },
      // 不包含认证信息，因为URL已预签名
      credentials: 'omit'
    });
    
    if (!response.ok) {
      throw new Error(`上传分片失败，状态码: ${response.status}, 状态: ${response.statusText}`);
    }
    
    // 获取ETag值
    const eTag = response.headers.get('etag') || '';
    if (eTag) {
      console.log('分片上传成功，ETag:', eTag);
      return {
        success: true,
        eTag: eTag.replace(/"/g, '') // 去除引号
      };
    } else {
      console.warn('上传分片成功，但缺少ETag响应头');
      
      // 尝试从响应中获取ETag
      const text = await response.text();
      const eTagMatch = /<ETag>(.*?)<\/ETag>/i.exec(text);
      if (eTagMatch && eTagMatch[1]) {
        console.log('从响应体中找到ETag:', eTagMatch[1]);
        return {
          success: true,
          eTag: eTagMatch[1].replace(/"/g, '')
        };
      }
      
      throw new Error('上传分片成功，但未返回ETag');
    }
  } catch (error) {
    console.error('上传分片错误:', error);
    throw error; // 将错误向上传播，让上层处理重试逻辑
  }
}

/**
 * 完成分片上传（合并分片）
 * @param objectKey 对象键（文件路径）
 * @param uploadId 上传ID
 * @param parts 分片列表
 */
async function completeMultipartUpload(objectKey: string, uploadId: string, parts: {partNumber: number, eTag: string}[]): Promise<CompleteMultipartResult> {
  try {
    const apiBaseUrl = getApiBaseUrl();
    
    // 创建认证头
    const authToken = localStorage.getItem('token');
    const headers: Record<string, string> = {
      'Content-Type': 'application/json'
    };
    
    if (authToken) {
      headers['Authorization'] = `Bearer ${authToken}`;
    }
    
    console.log('完成分片上传请求:', `${apiBaseUrl}/oss/multipart/complete`);
    
    const response = await axios.post<ApiResponse<{etag: string; fileUrl: string;}>>(
      `${apiBaseUrl}/oss/multipart/complete`,
      parts,
      {
        params: {
          objectKey,
          uploadId
        },
        headers
      }
    );
    
    if (response.data.code === 200 && response.data.data) {
      return {
        success: true,
        data: response.data.data
      };
    } else {
      console.error('完成分片上传失败:', response.data);
      return {
        success: false,
        error: response.data.message || '完成分片上传失败'
      };
    }
  } catch (error) {
    console.error('完成分片上传错误:', error);
    return {
      success: false,
      error: error instanceof Error ? error.message : '完成分片上传失败'
    };
  }
}

/**
 * 取消分片上传
 * @param objectKey 对象键（文件路径）
 * @param uploadId 上传ID
 */
async function abortMultipartUpload(objectKey: string, uploadId: string): Promise<{success: boolean, error?: string}> {
  try {
    const apiBaseUrl = getApiBaseUrl();
    
    // 创建认证头
    const authToken = localStorage.getItem('token');
    const headers: Record<string, string> = {
      'Content-Type': 'application/json'
    };
    
    if (authToken) {
      headers['Authorization'] = `Bearer ${authToken}`;
    }
    
    console.log('取消分片上传请求:', `${apiBaseUrl}/oss/multipart/abort`);
    
    const response = await axios.post<ApiResponse<boolean>>(
      `${apiBaseUrl}/oss/multipart/abort`,
      null,
      {
        params: {
          objectKey,
          uploadId
        },
        headers
      }
    );
    
    if (response.data.code === 200) {
      return {
        success: true
      };
    } else {
      console.error('取消分片上传失败:', response.data);
      return {
        success: false,
        error: response.data.message || '取消分片上传失败'
      };
    }
  } catch (error) {
    console.error('取消分片上传错误:', error);
    return {
      success: false,
      error: error instanceof Error ? error.message : '取消分片上传失败'
    };
  }
}

/**
 * 使用OSS表单上传文件（适用于小文件）
 * @param file 要上传的文件
 * @param customPath 自定义路径，如folder/subfolder/
 * @param onProgress 上传进度回调函数
 * @returns 上传结果，包含文件URL
 */
export const uploadToOSS = async (
  file: File, 
  customPath: string = '',
  onProgress?: UploadProgressCallback
): Promise<UploadResult> => {
  try {
    // 添加文件安全检查
    const securityCheck = checkFileSecurity(file);
    if (!securityCheck.isValid) {
      return {
        success: false,
        error: securityCheck.message || '文件安全检查未通过'
      };
    }
    
    // 使用安全处理后的文件名
    const safeFileName = securityCheck.sanitizedFileName || file.name;
    
    // 生成唯一文件名，避免覆盖
    const timestamp = new Date().getTime();
    const randomStr = Math.floor(Math.random() * 10000).toString().padStart(4, '0');
    const fileName = `${timestamp}-${randomStr}-${safeFileName}`;
    
    // 构建上传路径
    let key = '';
    
    // 检查是否为根目录上传
    const isRootPath = !customPath || customPath === '/' || customPath === '';
    
    if (isRootPath) {
      // 上传到根目录
      key = fileName;
      console.log(`上传文件到根目录: ${key}`);
    } else if (customPath) {
      // 确保路径以斜杠结尾
      const path = customPath.endsWith('/') ? customPath : `${customPath}/`;
      key = `${path}${fileName}`;
      console.log(`上传文件到指定路径: ${key}`);
    } else {
      // 默认使用签名中的目录
      key = fileName;
      console.log(`使用默认路径上传文件: ${key}`);
    }
    
    // 获取OSS签名
    const signature = await getOSSSignature();
    
    // 准备表单数据
    const formData = new FormData();
    formData.append('key', key);
    formData.append('OSSAccessKeyId', signature.accessId);
    formData.append('policy', signature.policy);
    formData.append('signature', signature.signature);
    
    // 添加文件
    formData.append('file', file);
    
    // 发送上传请求
    const response = await axios.post(signature.host, formData, {
      onUploadProgress: (e: ProgressEvent) => {
        // 计算上传进度
        const progress = e.total ? Math.floor((e.loaded / e.total) * 100) : 0;
        // 通过回调函数更新上传进度
        if (onProgress) {
          onProgress(progress, false);
        }
      }
    } as any); // 使用类型断言解决axios配置类型问题
    
    // 检查响应状态码
    if (response.status >= 200 && response.status < 300) {
      // 构建文件URL
      const fileUrl = `${signature.host}/${key}`;
      
      console.log('上传成功，URL:', fileUrl);
      
      return {
        success: true,
        fileUrl,
        url: fileUrl,
        key,
        bucket: signature.host.split('.')[0].replace('https://', '')
      };
    } else {
      throw new Error(`上传请求失败，状态码: ${response.status}`);
    }
  } catch (error) {
    console.error('上传OSS过程出错:', error);
    return {
      success: false,
      error: error instanceof Error ? error.message : '未知错误',
      details: error
    };
  }
};

// 定义文件列表响应接口
interface FileListResponse {
  success: boolean;
  files?: any[];
  error?: string;
}

/**
 * 获取OSS文件列表
 * @param prefix 路径前缀
 * @returns 文件列表和请求结果
 */
export const listOSSFiles = async (prefix: string = '') => {
  try {
    const apiBaseUrl = getApiBaseUrl();
    console.log('使用API基础URL:', apiBaseUrl);
    
    // 检查我们是否在开发环境中
    const isDev = (import.meta as any).env.DEV;
    
    // 如果在开发环境中，直接使用完整URL访问后端
    let baseURL;
    if (isDev) {
      // 从环境配置获取后端服务器地址
      const serverHost = window.location.hostname; // 或使用配置中的值
      const serverPort = 80; // 后端默认端口
      baseURL = `http://${serverHost}:${serverPort}/api`;
      console.log('开发环境下使用直接后端URL:', baseURL);
    } else {
      // 生产环境使用相对路径
      baseURL = apiBaseUrl;
    }
    
    // 创建一个配置了正确baseURL的axios实例
    const apiClient = axios.create({
      baseURL,
      timeout: 15000,
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Accept': 'application/json'
      }
    });
    
    console.log('发送请求到:', `${baseURL}/oss/list`);
    
    interface ApiResponse {
      code?: number;
      message?: string;
      data?: any[];
      files?: any[];
      [key: string]: any;
    }
    
    const response = await apiClient.get<ApiResponse>('/oss/list', {
      params: { prefix }
    });
    
    console.log('获取文件列表响应类型:', typeof response.data);
    console.log('获取文件列表响应结构:', response.data ? Object.keys(response.data) : 'null');
    
    // 检查响应是否是HTML (可能是Vite开发服务器的响应)
    if (typeof response.data === 'string' && (response.data as string).trim().startsWith('<!DOCTYPE html>')) {
      console.error('收到HTML响应而非JSON，可能是请求错误地路由到了前端开发服务器');
      return {
        success: false,
        error: '获取文件列表失败: API请求被误导向前端开发服务器',
        files: []
      };
    }
    
    // 放宽检查条件，处理不同的响应格式
    if (response.data) {
      // 检查是否是标准的ResultDTO格式
      if (response.data.code === 200 && Array.isArray(response.data.data)) {
        return {
          success: true,
          files: response.data.data
        };
      } 
      // 检查是否直接返回了数组
      else if (Array.isArray(response.data)) {
        return {
          success: true,
          files: response.data
        };
      }
      // 如果响应中包含files字段
      else if (response.data.files && Array.isArray(response.data.files)) {
        return {
          success: true,
          files: response.data.files
        };
      }
      // 如果有其他可能的数据结构，记录并抛出格式错误
      else {
        console.error('响应格式不符合预期:', response.data);
        return {
          success: false,
          error: '获取文件列表失败: 返回数据格式不符合预期',
          files: []
        };
      }
    } else {
      throw new Error('获取文件列表失败: 响应数据为空');
    }
  } catch (error) {
    console.error('获取文件列表错误:', error);
    return {
      success: false,
      error: error instanceof Error ? error.message : '未知错误',
      files: []
    };
  }
};

/**
 * 删除OSS文件的响应类型
 */
interface DeleteFileResponse {
  success: boolean;
  message?: string;
  code?: number;
}

/**
 * 删除OSS文件
 * @param key 文件的完整路径
 * @returns 操作结果
 */
export const deleteOSSFile = async (key: string): Promise<{success: boolean, error?: string}> => {
  try {
    if (!key) {
      return { success: false, error: '文件路径不能为空' };
    }
    
    // 获取API基础URL
    const baseUrl = getApiBaseUrl();
    
    // 修正API URL，确保路径正确
    // 使用相对路径，不指定主机和端口，让浏览器和Nginx处理路由
    const apiUrl = `${baseUrl}/oss/delete-file`;
    console.log('使用相对路径构建API URL:', apiUrl);
    
    console.log('要删除的文件路径:', key);
    
    // 获取认证令牌
    const token = localStorage.getItem('token');
    if (!token) {
      console.warn('删除文件失败: 未找到登录令牌');
      // 提示用户需要登录
      return { 
        success: false, 
        error: '您需要登录才能执行此操作，请先登录或刷新页面后重试' 
      };
    }
    
    console.log('认证令牌前10个字符:', token.substring(0, 10) + '...');
    
    // 打印请求信息
    console.log('发送删除请求:', {
      url: apiUrl,
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token.substring(0, 10) + '...'
      },
      data: { objectKey: key }
    });
    
    // 发送删除请求 - 不使用 axiosInstance，而是直接使用 axios
    const response = await axios.post<DeleteFileResponse>(
      apiUrl, 
      { objectKey: key },
      {
        timeout: 30000,
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
          // 添加CORS相关头部
          'X-Requested-With': 'XMLHttpRequest',
          'Access-Control-Allow-Origin': '*',
          'Access-Control-Allow-Headers': 'Origin, X-Requested-With, Content-Type, Accept, Authorization',
          'Accept': 'application/json, text/plain, */*'
        },
        // 添加凭据选项
        withCredentials: true
      }
    );
    console.log('删除文件响应:', response.data);
    
    // 判断响应
    if (response.data && response.data.success) {
      return { success: true };
    } else {
      return { 
        success: false, 
        error: (response.data && response.data.message) || '删除文件失败'
      };
    }
  } catch (error) {
    console.error('删除文件时发生错误:', error);
    
    // 检查HTTP错误状态码
    if (error && typeof error === 'object' && 'response' in error) {
      const errorResponse = (error as any).response;
      console.log('错误响应详情:', {
        status: errorResponse?.status,
        statusText: errorResponse?.statusText,
        data: errorResponse?.data
      });
      
      // 处理401未授权错误
      if (errorResponse && errorResponse.status === 401) {
        console.warn('令牌已过期或无效，需要重新登录');
        
        // 清除已过期的令牌
        localStorage.removeItem('token');
        
        return {
          success: false,
          error: '您的登录会话已过期，请重新登录后再试'
        };
      }
      
      // 处理403禁止访问错误
      if (errorResponse && errorResponse.status === 403) {
        console.error('403 Forbidden 错误 - 访问被拒绝');
        return {
          success: false,
          error: '您没有删除此文件的权限，请联系管理员获取授权'
        };
      }
      
      // 处理404未找到错误
      if (errorResponse && errorResponse.status === 404) {
        return {
          success: false,
          error: '要删除的文件不存在或已被删除'
        };
      }
      
      // 处理其他HTTP错误
      if (errorResponse && errorResponse.status) {
        const statusText = errorResponse.statusText || `错误代码 ${errorResponse.status}`;
        const serverMessage = errorResponse.data && typeof errorResponse.data === 'string' 
          ? errorResponse.data 
          : (errorResponse.data && errorResponse.data.message) || '服务器未提供详细信息';
        
        return {
          success: false,
          error: `服务器返回错误: ${statusText} - ${serverMessage}`
        };
      }
    }
    
    // 处理网络错误或其他未知错误
    return {
      success: false,
      error: error instanceof Error ? error.message : '删除文件时发生未知错误'
    };
  }
};

// 定义下载URL响应接口
interface DownloadUrlResponse {
  success: boolean;
  url?: string;
  error?: string;
}

// 定义下载URL接口
interface SignedUrlResponse {
  signedUrl: string;
}

/**
 * 获取文件下载URL
 * @param objectKey 文件路径
 * @returns 下载URL
 */
export const getFileDownloadUrl = async (objectKey: string): Promise<DownloadUrlResponse> => {
  try {
    const apiBaseUrl = getApiBaseUrl();
    
    // 获取令牌
    const token = localStorage.getItem('token');
    if (!token) {
      console.warn('获取下载URL失败: 未找到登录令牌');
      return {
        success: false,
        error: '您需要登录才能访问此文件，请先登录'
      };
    }
    
    // 创建axios实例
    const axiosInstance = axios.create({
      baseURL: apiBaseUrl,
      timeout: 15000,
      headers: {
        'Authorization': `Bearer ${token}`,
        'Accept': 'application/json'
      }
    });
    
    const response = await axiosInstance.get<SignedUrlResponse>(`/oss/get-signed-url`, {
      params: { objectName: objectKey }
    });
    
    if (response.data && response.data.signedUrl) {
      return {
        success: true,
        url: response.data.signedUrl
      };
    } else {
      throw new Error('获取下载URL失败: 返回数据不完整');
    }
  } catch (error) {
    console.error('获取文件下载URL错误:', error);
    
    // 检查是否是认证错误
    if (error && typeof error === 'object' && 'response' in error) {
      const errorResponse = (error as any).response;
      
      // 处理401未授权错误
      if (errorResponse && errorResponse.status === 401) {
        console.warn('令牌已过期或无效，需要重新登录');
        
        // 清除已过期的令牌
        localStorage.removeItem('token');
        
        return {
          success: false,
          error: '您的登录会话已过期，请重新登录后再试'
        };
      }
      
      // 处理其他HTTP错误
      if (errorResponse && errorResponse.status) {
        const statusText = errorResponse.statusText || `错误代码 ${errorResponse.status}`;
        return {
          success: false,
          error: `服务器返回错误: ${statusText}`
        };
      }
    }
    
    return {
      success: false,
      error: error instanceof Error ? error.message : '未知错误'
    };
  }
}; 