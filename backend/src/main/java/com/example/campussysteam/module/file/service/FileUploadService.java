package com.example.campussysteam.module.file.service;

import com.example.campussysteam.model.upload.FileUploadStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 文件上传服务类
 * 实现大文件分片上传、断点续传和文件安全检查功能
 */
@Service
public class FileUploadService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);
    
    @Value("${file.upload.dir:uploads}")
    private String uploadDir;
    
    @Value("${file.temp.dir:temp}")
    private String tempDir;
    
    /**
     * 构造函数，在服务启动时确保目录存在
     */
    public FileUploadService() {
        // 注意：配置值在构造函数中尚未注入，因此实际的目录创建在第一次使用时执行
    }
    
    /**
     * 确保上传目录和临时目录存在
     */
    private void ensureDirectoriesExist() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            Path tempPath = Paths.get(tempDir);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("创建上传目录: {}", uploadPath.toAbsolutePath());
            }
            
            if (!Files.exists(tempPath)) {
                Files.createDirectories(tempPath);
                logger.info("创建临时目录: {}", tempPath.toAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("创建目录失败", e);
            // 不抛出异常，让后续操作根据实际情况处理
        }
    }
    
    /**
     * 检查文件上传状态，用于断点续传
     * 
     * @param fileMd5 文件的MD5值，用于唯一标识文件
     * @param fileName 文件名
     * @param fileSize 文件总大小
     * @param chunkCount 分片总数
     * @return 文件上传状态，包括已上传的分片信息
     */
    public FileUploadStatus checkFileStatus(String fileMd5, String fileName, long fileSize, int chunkCount) {
        // 此处省略实现...
        return new FileUploadStatus();
    }
    
    /**
     * 保存文件分片
     * 
     * @param file 文件分片
     * @param fileMd5 文件MD5值
     * @param chunkIndex 分片索引
     * @param chunkCount 总分片数
     * @param fileName 文件名
     * @return 是否成功保存
     */
    public boolean saveChunk(MultipartFile file, String fileMd5, int chunkIndex, int chunkCount, String fileName) {
        ensureDirectoriesExist();
        
        // 创建分片目录
        String chunkDir = tempDir + File.separator + fileMd5;
        Path chunkDirPath = Paths.get(chunkDir);
        try {
            if (!Files.exists(chunkDirPath)) {
                Files.createDirectories(chunkDirPath);
            }
            
            // 保存分片文件
            String chunkFilePath = chunkDir + File.separator + chunkIndex;
            try (InputStream inputStream = file.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(chunkFilePath)) {
                
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                
                logger.info("保存分片成功: {}, 大小: {}", chunkFilePath, file.getSize());
                return true;
            }
        } catch (IOException e) {
            logger.error("保存分片失败", e);
            return false;
        }
    }
    
    /**
     * 合并文件分片
     * 
     * @param fileMd5 文件MD5值
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @param chunkCount 总分片数
     * @return 合并后的文件URL
     * @throws IOException 如果合并过程中发生错误
     */
    public String mergeChunks(String fileMd5, String fileName, long fileSize, int chunkCount) throws IOException {
        ensureDirectoriesExist();
        
        // 检查所有分片是否已上传
        String chunkDir = tempDir + File.separator + fileMd5;
        File chunkDirFile = new File(chunkDir);
        if (!chunkDirFile.exists() || !chunkDirFile.isDirectory()) {
            throw new IOException("分片目录不存在: " + chunkDir);
        }
        
        File[] chunkFiles = chunkDirFile.listFiles();
        if (chunkFiles == null || chunkFiles.length < chunkCount) {
            throw new IOException("分片文件不完整，预期" + chunkCount + "个，实际找到" + 
                (chunkFiles == null ? 0 : chunkFiles.length) + "个");
        }
        
        // 准备目标文件
        String targetFilename = fileName;
        String targetPath = uploadDir + File.separator + targetFilename;
        File targetFile = new File(targetPath);
        
        // 如果文件已存在，添加时间戳避免重名
        if (targetFile.exists()) {
            String fileNameWithoutExt = fileName;
            String extension = "";
            int lastDotIndex = fileName.lastIndexOf(".");
            if (lastDotIndex > 0) {
                fileNameWithoutExt = fileName.substring(0, lastDotIndex);
                extension = fileName.substring(lastDotIndex);
            }
            targetFilename = fileNameWithoutExt + "_" + System.currentTimeMillis() + extension;
            targetPath = uploadDir + File.separator + targetFilename;
            targetFile = new File(targetPath);
        }
        
        // 创建目标文件的父目录
        Files.createDirectories(Paths.get(targetFile.getParent()));
        
        // 按索引排序分片文件
        List<File> sortedChunkFiles = new ArrayList<>();
        for (int i = 0; i < chunkCount; i++) {
            File chunkFile = new File(chunkDir + File.separator + i);
            if (!chunkFile.exists()) {
                throw new IOException("分片文件不存在: " + chunkFile.getPath());
            }
            sortedChunkFiles.add(chunkFile);
        }
        
        // 合并分片文件
        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            for (File chunk : sortedChunkFiles) {
                try (FileInputStream inputStream = new FileInputStream(chunk)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
            }
        }
        
        // 验证合并后的文件大小
        if (targetFile.length() != fileSize) {
            targetFile.delete();
            throw new IOException("文件大小不匹配，预期" + fileSize + "字节，实际为" + targetFile.length() + "字节");
        }
        
        // 清理分片文件
        for (File chunk : sortedChunkFiles) {
            if (!chunk.delete()) {
                logger.warn("无法删除分片文件: {}", chunk.getPath());
            }
        }
        if (!chunkDirFile.delete()) {
            logger.warn("无法删除分片目录: {}", chunkDir);
        }
        
        // 返回文件URL（相对路径）
        String fileUrl = "/uploads/" + targetFilename;
        logger.info("文件合并成功: {}, URL: {}", targetPath, fileUrl);
        return fileUrl;
    }
    
    // 省略其他方法...
} 