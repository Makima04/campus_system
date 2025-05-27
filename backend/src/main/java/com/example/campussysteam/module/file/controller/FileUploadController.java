package com.example.campussysteam.module.file.controller;

import com.example.campussysteam.common.log.Log;
import com.example.campussysteam.common.log.OperationType;
import com.example.campussysteam.model.upload.FileCheckRequest;
import com.example.campussysteam.model.upload.FileMergeRequest;
import com.example.campussysteam.model.upload.FileUploadStatus;
import com.example.campussysteam.module.file.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);
    
    @Autowired
    private FileUploadService fileUploadService;
    
    /**
     * 检查文件上传状态
     */
    @PostMapping("/check")
    @Log(module = "文件管理", type = OperationType.QUERY, description = "检查文件上传状态")
    public ResponseEntity<?> checkFileStatus(@RequestBody FileCheckRequest request) {
        try {
            logger.info("检查文件上传状态: fileMd5={}, fileName={}, fileSize={}", 
                request.getFileMd5(), request.getFileName(), request.getFileSize());
                
            FileUploadStatus status = fileUploadService.checkFileStatus(
                request.getFileMd5(), 
                request.getFileName(), 
                request.getFileSize(), 
                request.getChunkCount()
            );
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            logger.error("检查文件上传状态失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 上传文件分片
     */
    @PostMapping("/chunk")
    @Log(module = "文件管理", type = OperationType.IMPORT, description = "上传文件分片")
    public ResponseEntity<?> uploadChunk(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileMd5") String fileMd5,
            @RequestParam("chunkIndex") int chunkIndex,
            @RequestParam("chunkCount") int chunkCount,
            @RequestParam("fileName") String fileName) {
        
        logger.info("上传文件分片: fileMd5={}, fileName={}, chunkIndex={}/{}", 
            fileMd5, fileName, chunkIndex, chunkCount);
            
        try {
            boolean success = fileUploadService.saveChunk(file, fileMd5, chunkIndex, chunkCount, fileName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            logger.warn("文件安全检查未通过", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "文件安全检查未通过: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (Exception e) {
            logger.error("上传分片失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 合并文件分片
     */
    @PostMapping("/merge")
    @Log(module = "文件管理", type = OperationType.IMPORT, description = "合并文件分片")
    public ResponseEntity<?> mergeChunks(@RequestBody FileMergeRequest request) {
        logger.info("合并文件分片: fileMd5={}, fileName={}, fileSize={}, chunkCount={}", 
            request.getFileMd5(), request.getFileName(), request.getFileSize(), request.getChunkCount());
            
        try {
            String fileUrl = fileUploadService.mergeChunks(
                request.getFileMd5(), 
                request.getFileName(), 
                request.getFileSize(), 
                request.getChunkCount()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("fileUrl", fileUrl);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("合并分片失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 