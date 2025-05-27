package com.example.campussysteam.module.file.controller;

import com.example.campussysteam.common.log.Log;
import com.example.campussysteam.common.log.OperationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/file")
public class FileDownloadController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileDownloadController.class);
    
    @Value("${file.upload.dir:uploads}")
    private String uploadDir;
    
    /**
     * 支持断点续传的文件下载
     */
    @GetMapping("/download/{fileName}")
    @Log(module = "文件管理", type = OperationType.EXPORT, description = "下载文件")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String fileName,
            HttpServletRequest request) {
        
        logger.info("下载文件: {}", fileName);
        
        try {
            // 构建文件路径
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists()) {
                logger.warn("文件不存在: {}", fileName);
                return ResponseEntity.notFound().build();
            }
            
            // 获取文件的MIME类型
            String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            // 处理文件名编码，确保中文名称正常显示
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            
            // 读取HTTP Range头
            String rangeHeader = request.getHeader("Range");
            long fileLength = resource.contentLength();
            
            if (rangeHeader == null || rangeHeader.isEmpty()) {
                // 正常下载，无断点续传
                logger.info("正常下载文件: {}, 大小: {}", fileName, fileLength);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .contentLength(fileLength)
                        .body(resource);
            } else {
                // 断点续传
                logger.info("断点续传下载: {}, Range头: {}", fileName, rangeHeader);
                long start, end;
                
                // 解析Range头
                // 格式：bytes=start-end
                String[] ranges = rangeHeader.substring("bytes=".length()).split("-");
                start = Long.parseLong(ranges[0]);
                
                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    end = Long.parseLong(ranges[1]);
                } else {
                    end = fileLength - 1;
                }
                
                if (start >= fileLength) {
                    // 请求范围超出文件大小
                    logger.warn("请求范围超出文件大小: start={}, fileLength={}", start, fileLength);
                    return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                            .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileLength)
                            .build();
                }
                
                // 计算实际数据大小
                long contentLength = end - start + 1;
                logger.info("断点续传: 返回数据范围 {}-{}, 大小: {}", start, end, contentLength);
                
                // 创建自定义资源对象，只返回请求范围的数据
                InputStreamResource partialResource = new InputStreamResource(getRangedStream(resource, start, end));
                
                // 返回206状态码和部分内容
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength)
                        .contentLength(contentLength)
                        .body(partialResource);
            }
        } catch (Exception e) {
            logger.error("下载文件失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 创建范围输入流
     */
    private InputStream getRangedStream(Resource resource, long start, long end) throws IOException {
        InputStream inputStream = resource.getInputStream();
        // 跳过开头不需要的部分
        inputStream.skip(start);
        
        // 限制流的长度，确保只读取请求的范围
        return new LimitedInputStream(inputStream, end - start + 1);
    }
    
    /**
     * 限制输入流的字节数
     */
    private static class LimitedInputStream extends FilterInputStream {
        private long remaining;
        
        public LimitedInputStream(InputStream in, long limit) {
            super(in);
            this.remaining = limit;
        }
        
        @Override
        public int read() throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            
            int result = super.read();
            if (result != -1) {
                remaining--;
            }
            return result;
        }
        
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            
            int maxLen = (int) Math.min(len, remaining);
            int result = super.read(b, off, maxLen);
            if (result != -1) {
                remaining -= result;
            }
            return result;
        }
        
        @Override
        public long skip(long n) throws IOException {
            long skipped = super.skip(Math.min(n, remaining));
            remaining -= skipped;
            return skipped;
        }
        
        @Override
        public int available() throws IOException {
            return (int) Math.min(super.available(), remaining);
        }
    }
} 