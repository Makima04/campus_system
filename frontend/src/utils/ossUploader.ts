import axios from 'axios';
import api from '@/api/index'; // 引入配置好的API实例，包含认证令牌处理

interface OSSConfig {
  accessId: string;
  host: string;
  policy: string;
  signature: string;
  expire: number;
  dir: string;
}

interface UploadResult {
  url: string;
  name: string;
  success: boolean;
  message?: string;
}

/**
 * 阿里云OSS上传工具类
 */
export class OSSUploader {
  private config: OSSConfig | null = null;
  private serverUrl: string;

  /**
   * 构造函数
   * @param serverUrl 获取签名的服务端地址
   */
  constructor(serverUrl: string) {
    this.serverUrl = serverUrl;
  }

  /**
   * 从服务端获取OSS签名信息
   */
  private async getSignature(): Promise<OSSConfig> {
    try {
      console.log('正在请求OSS签名...');
      
      // 使用api实例而不是原始axios
      const response = await api.get('/api/oss/signature');
      console.log('获取到OSS签名:', response.data);
      
      if (response.status !== 200) {
        throw new Error(`获取签名失败: ${response.statusText}`);
      }
      
      return response.data;
    } catch (error) {
      console.error('获取OSS签名失败:', error);
      if (axios.isAxiosError(error)) {
        console.error('错误详情:', {
          status: error.response?.status,
          statusText: error.response?.statusText,
          data: error.response?.data
        });
      }
      throw error;
    }
  }

  /**
   * 上传文件到OSS
   * @param file 要上传的文件对象
   * @param path 存储路径，默认为config中的dir
   * @returns 上传结果
   */
  public async uploadFile(file: File, path?: string): Promise<UploadResult> {
    try {
      console.log('开始上传文件:', file.name);
      
      // 如果没有配置或配置已过期，重新获取
      if (!this.config || this.config.expire < Date.now() / 1000) {
        console.log('获取新的OSS签名...');
        this.config = await this.getSignature();
      }

      if (!this.config) {
        throw new Error('获取OSS配置失败');
      }
      
      // 检查OSS配置
      if (!this.config.host || !this.config.policy || !this.config.signature) {
        throw new Error('OSS配置不完整，请检查服务端返回的签名信息');
      }

      // 文件存储路径
      const uploadPath = path || this.config.dir;
      const fileName = `${uploadPath}/${Date.now()}-${file.name}`;
      
      console.log('准备上传到路径:', fileName);
      
      // 创建FormData
      const formData = new FormData();
      
      // 添加必要的字段
      formData.append('key', fileName);
      formData.append('policy', this.config.policy);
      formData.append('OSSAccessKeyId', this.config.accessId);
      formData.append('success_action_status', '200');
      formData.append('signature', this.config.signature);
      formData.append('file', file);
      
      // 使用XMLHttpRequest上传
      return new Promise((resolve) => {
        const xhr = new XMLHttpRequest();
        
        xhr.onload = () => {
          if (xhr.status === 200) {
            const fileUrl = `${this.config!.host}/${fileName}`;
            console.log('上传成功，文件URL:', fileUrl);
            resolve({
              url: fileUrl,
              name: fileName,
              success: true
            });
          } else {
            console.error('上传失败:', xhr.statusText);
            resolve({
              url: '',
              name: fileName,
              success: false,
              message: `上传失败: ${xhr.statusText}`
            });
          }
        };

        xhr.onerror = () => {
          console.error('上传请求失败');
          resolve({
            url: '',
            name: fileName,
            success: false,
            message: '上传请求失败'
          });
        };

        // 发送请求
        xhr.open('POST', this.config!.host);
        xhr.send(formData);
      });
    } catch (error) {
      console.error('OSS上传文件失败:', error);
      return {
        url: '',
        name: file.name,
        success: false,
        message: `上传出错: ${error}`
      };
    }
  }
}

// 修改默认实例的创建方式，使用相对路径
export const ossUploader = new OSSUploader('/api/oss/signature');

export default ossUploader;

/**
 * 获取OSS上传签名
 * 使用配置好的API实例，自动处理认证
 */
const getOSSSignature = async () => {
  try {
    // 使用API模块，确保包含认证信息
    const response = await api.get('/api/oss/signature');
    console.log('获取OSS上传签名成功:', response.data);
    return response.data;
  } catch (error) {
    console.error('获取OSS上传签名失败:', error);
    throw new Error('获取上传凭证失败，请检查网络或重新登录');
  }
};

/**
 * 上传文件到阿里云OSS
 * @param file 要上传的文件
 * @param customPath 自定义路径，如folder/subfolder/
 * @returns 上传成功后的文件URL和其它信息
 */
export const uploadToOSS = async (file: File, customPath: string = '') => {
  try {
    // 获取上传签名
    const signature = await getOSSSignature();
    
    if (!signature || !signature.host) {
      throw new Error('获取上传签名数据不完整');
    }

    // 构建文件名：使用时间戳和随机数确保唯一性
    const fileName = `${Date.now()}-${Math.floor(Math.random() * 10000)}${file.name.substring(file.name.lastIndexOf('.'))}`;
    
    // 完整的文件路径
    const filePath = customPath 
      ? `${signature.dir}${customPath}${fileName}` 
      : `${signature.dir}${fileName}`;
    
    // 构建表单数据
    const formData = new FormData();
    formData.append('key', filePath);
    formData.append('policy', signature.policy);
    formData.append('OSSAccessKeyId', signature.accessId);
    formData.append('signature', signature.signature);
    formData.append('success_action_status', '200');
    formData.append('file', file);
    
    console.log('开始上传文件:', {
      host: signature.host,
      filePath,
      fileSize: file.size,
      fileType: file.type
    });

    // 使用axios直接请求OSS服务器，无需认证
    const uploadResponse = await axios.post(signature.host, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        if (progressEvent.total) {
          const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          console.log(`上传进度: ${percentCompleted}%`);
        }
      },
    });

    console.log('上传成功:', uploadResponse);
    
    // 构建文件完整URL并返回
    const fileUrl = `${signature.host}/${filePath}`;
    return {
      url: fileUrl,
      key: filePath,
      bucket: signature.host.split('.')[0].replace('https://', ''),
      success: true
    };
    
  } catch (error) {
    console.error('上传失败:', error);
    // 返回详细的错误信息
    return {
      success: false,
      error: error instanceof Error ? error.message : '未知错误',
      details: error
    };
  }
};

/**
 * 获取上传文件的签名URL (用于查看已上传文件)
 * @param objectKey 文件在OSS中的对象键名
 */
export const getFileSignedUrl = async (objectKey: string) => {
  try {
    const response = await api.get('/api/oss/get-signed-url', {
      params: { objectName: objectKey }
    });
    
    if (response.data && response.data.signedUrl) {
      return response.data.signedUrl;
    } else {
      throw new Error('获取签名URL失败: 返回数据不完整');
    }
  } catch (error) {
    console.error('获取签名URL失败:', error);
    // 备用方案：直接使用OSS URL
    return `https://campus-system1122.oss-cn-chengdu.aliyuncs.com/${objectKey}`;
  }
}; 