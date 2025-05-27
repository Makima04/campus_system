import SparkMD5 from 'spark-md5';
import axios from 'axios';
import { getApiBaseUrl } from '@/utils/api';

// 定义API响应类型
interface ApiResponse<T = any> {
  success: boolean;
  message?: string;
  data?: T;
  fileUrl?: string;
  uploaded?: boolean;
  uploadedChunks?: number[];
}

interface ChunkInfo {
  index: number;
  file: Blob;
  size: number;
  uploadedSize?: number;
  progress?: number;
  status: 'pending' | 'uploading' | 'success' | 'error';
}

interface FileUploadInfo {
  fileId: string;
  fileName: string;
  fileSize: number;
  fileType: string;
  fileMd5: string;
  chunkSize: number;
  chunkCount: number;
  chunks: ChunkInfo[];
  uploadedChunks: number[];
  progress: number;
}

export class ChunkUploader {
  private chunkSize: number = 2 * 1024 * 1024; // 2MB分片大小
  private retryCount: number = 3; // 重试次数
  private uploadingFile: FileUploadInfo | null = null;
  private isUploading: boolean = false;
  private isPaused: boolean = false;

  /**
   * 计算文件MD5，用于唯一标识文件
   */
  private async calculateFileMd5(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const blobSlice = File.prototype.slice;
      const chunks = Math.ceil(file.size / this.chunkSize);
      let currentChunk = 0;
      const spark = new SparkMD5.ArrayBuffer();
      const fileReader = new FileReader();

      fileReader.onload = (e: ProgressEvent<FileReader>) => {
        if (e.target?.result) {
          spark.append(e.target.result as ArrayBuffer);
        }
        currentChunk++;

        if (currentChunk < chunks) {
          loadNext();
        } else {
          const md5 = spark.end();
          resolve(md5);
        }
      };

      fileReader.onerror = () => {
        reject(new Error('文件读取失败'));
      };

      const loadNext = () => {
        const start = currentChunk * this.chunkSize;
        const end = Math.min(start + this.chunkSize, file.size);
        fileReader.readAsArrayBuffer(blobSlice.call(file, start, end));
      };

      loadNext();
    });
  }

  /**
   * 初始化上传：检查是否有未完成的上传任务
   */
  public async initUpload(file: File): Promise<FileUploadInfo> {
    const fileMd5 = await this.calculateFileMd5(file);
    const fileId = `${fileMd5}_${file.name}`;
    
    // 从localStorage获取上传记录
    const uploadRecord = localStorage.getItem(`upload_${fileId}`);
    
    if (uploadRecord) {
      // 恢复之前的上传
      this.uploadingFile = JSON.parse(uploadRecord);
      // 确保返回的是非空对象
      return this.uploadingFile!;
    }

    // 创建新的上传任务
    const chunkCount = Math.ceil(file.size / this.chunkSize);
    const chunks: ChunkInfo[] = [];

    for (let i = 0; i < chunkCount; i++) {
      const start = i * this.chunkSize;
      const end = Math.min(start + this.chunkSize, file.size);
      chunks.push({
        index: i,
        file: file.slice(start, end),
        size: end - start,
        status: 'pending'
      });
    }

    this.uploadingFile = {
      fileId,
      fileName: file.name,
      fileSize: file.size,
      fileType: file.type,
      fileMd5,
      chunkSize: this.chunkSize,
      chunkCount,
      chunks,
      uploadedChunks: [],
      progress: 0
    };

    // 保存上传信息到localStorage
    this.saveUploadProgress();
    
    // 检查文件是否已经上传过
    try {
      const response = await axios.post<ApiResponse>(`${getApiBaseUrl()}/upload/check`, {
        fileMd5,
        fileName: file.name,
        fileSize: file.size,
        chunkCount
      });
      
      if (response.data.uploaded) {
        // 文件已完全上传过
        this.uploadingFile.progress = 100;
        this.uploadingFile.uploadedChunks = Array.from(Array(chunkCount).keys());
        this.saveUploadProgress();
        return this.uploadingFile;
      }
      
      if (response.data.uploadedChunks && response.data.uploadedChunks.length > 0) {
        // 部分分片已上传，更新状态
        this.uploadingFile.uploadedChunks = response.data.uploadedChunks;
        for (const index of response.data.uploadedChunks) {
          if (index < this.uploadingFile.chunks.length) {
            this.uploadingFile.chunks[index].status = 'success';
          }
        }
        this.calculateProgress();
        this.saveUploadProgress();
      }
    } catch (error) {
      console.error('检查文件上传状态失败:', error);
      // 继续上传流程，假设文件未上传
    }

    return this.uploadingFile;
  }

  /**
   * 上传所有分片
   */
  public async uploadChunks(): Promise<{success: boolean, fileUrl?: string, error?: string}> {
    if (!this.uploadingFile) {
      throw new Error('未初始化上传任务');
    }

    if (this.isUploading) {
      return { success: false, error: '上传任务已在进行中' };
    }

    try {
      this.isUploading = true;
      this.isPaused = false;

      // 上传未完成的分片
      const uploadPromises = this.uploadingFile.chunks
        .filter(chunk => !this.uploadingFile?.uploadedChunks.includes(chunk.index))
        .map(chunk => this.uploadChunk(chunk));

      await Promise.all(uploadPromises);

      // 检查是否所有分片都已上传
      if (this.uploadingFile.uploadedChunks.length === this.uploadingFile.chunkCount) {
        // 通知后端合并分片
        const response = await axios.post(`${getApiBaseUrl()}/upload/merge`, {
          fileMd5: this.uploadingFile.fileMd5,
          fileName: this.uploadingFile.fileName,
          fileSize: this.uploadingFile.fileSize,
          chunkCount: this.uploadingFile.chunkCount
        });

        if ((response.data as any).success) {
          // 清除上传记录
          localStorage.removeItem(`upload_${this.uploadingFile.fileId}`);
          this.isUploading = false;
          return { 
            success: true, 
            fileUrl: (response.data as any).fileUrl 
          };
        } else {
          this.isUploading = false;
          return { 
            success: false, 
            error: (response.data as any).message || '文件合并失败' 
          };
        }
      }

      this.isUploading = false;
      return { success: false, error: '上传未完成' };
    } catch (error) {
      this.isUploading = false;
      console.error('上传失败:', error);
      return { 
        success: false, 
        error: error instanceof Error ? error.message : '上传过程发生未知错误' 
      };
    }
  }

  /**
   * 上传单个分片
   */
  private async uploadChunk(chunk: ChunkInfo): Promise<void> {
    if (!this.uploadingFile) return;

    let retries = 0;
    let success = false;

    while (retries < this.retryCount && !success && !this.isPaused) {
      try {
        chunk.status = 'uploading';
        this.saveUploadProgress();

        const formData = new FormData();
        formData.append('file', chunk.file);
        formData.append('fileMd5', this.uploadingFile.fileMd5);
        formData.append('chunkIndex', chunk.index.toString());
        formData.append('chunkCount', this.uploadingFile.chunkCount.toString());
        formData.append('fileName', this.uploadingFile.fileName);

        const response = await axios.post(`${getApiBaseUrl()}/upload/chunk`, formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });

        if ((response.data as any).success) {
          chunk.status = 'success';
          if (!this.uploadingFile.uploadedChunks.includes(chunk.index)) {
            this.uploadingFile.uploadedChunks.push(chunk.index);
          }
          this.calculateProgress();
          this.saveUploadProgress();
          success = true;
        } else {
          throw new Error((response.data as any).message || '上传分片失败');
        }
      } catch (error) {
        retries++;
        chunk.status = 'error';
        console.error(`分片${chunk.index}上传失败, 重试次数: ${retries}`, error);
        
        // 等待一段时间再重试
        await new Promise(resolve => setTimeout(resolve, 1000));
      }
    }

    if (!success && !this.isPaused) {
      throw new Error(`分片${chunk.index}上传失败，已达到最大重试次数`);
    }
  }

  /**
   * 暂停上传
   */
  public pauseUpload(): void {
    this.isPaused = true;
  }

  /**
   * 恢复上传
   */
  public async resumeUpload(): Promise<{success: boolean, fileUrl?: string, error?: string}> {
    if (!this.uploadingFile) {
      throw new Error('没有可恢复的上传任务');
    }
    
    this.isPaused = false;
    return this.uploadChunks();
  }

  /**
   * 取消上传
   */
  public cancelUpload(): void {
    if (this.uploadingFile) {
      localStorage.removeItem(`upload_${this.uploadingFile.fileId}`);
      this.uploadingFile = null;
    }
    this.isPaused = true;
    this.isUploading = false;
  }

  /**
   * 计算总进度
   */
  private calculateProgress(): void {
    if (!this.uploadingFile) return;

    const totalChunks = this.uploadingFile.chunkCount;
    const uploadedChunks = this.uploadingFile.uploadedChunks.length;
    
    // 计算已完成分片的进度
    let completedProgress = (uploadedChunks / totalChunks) * 100;
    
    // 计算正在上传分片的进度
    let uploadingProgress = 0;
    const uploadingChunks = this.uploadingFile.chunks.filter(
      chunk => chunk.status === 'uploading' && chunk.progress !== undefined
    );
    
    if (uploadingChunks.length > 0) {
      const chunkProgressSum = uploadingChunks.reduce(
        (sum, chunk) => sum + (chunk.progress || 0), 0
      );
      uploadingProgress = (chunkProgressSum / totalChunks / 100) * (uploadingChunks.length / totalChunks) * 100;
    }
    
    this.uploadingFile.progress = Math.min(Math.round(completedProgress + uploadingProgress), 99);
    
    // 如果所有分片都上传完成，设置为100%
    if (uploadedChunks === totalChunks) {
      this.uploadingFile.progress = 100;
    }
  }

  /**
   * 保存上传进度到localStorage
   */
  private saveUploadProgress(): void {
    if (this.uploadingFile) {
      localStorage.setItem(
        `upload_${this.uploadingFile.fileId}`, 
        JSON.stringify(this.uploadingFile)
      );
    }
  }
}

// 导出实例
export const chunkUploader = new ChunkUploader(); 