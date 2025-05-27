package com.example.campussysteam.module.oss.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.*;
import com.aliyun.oss.HttpMethod;
import com.example.campussysteam.common.ResultCode;
import com.example.campussysteam.common.ResultDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 阿里云OSS分片上传控制器
 * 实现大文件分片上传功能
 */
@RestController
@RequestMapping("/api/oss/multipart")
public class MultipartUploadController {
    private static final Logger logger = LoggerFactory.getLogger(MultipartUploadController.class);

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucket}")
    private String bucket;

    @Value("${aliyun.oss.dir}")
    private String dir;

    /**
     * 初始化分片上传任务
     * @param objectKey 文件完整路径和名称
     * @param authorization 认证信息
     * @return 上传ID和文件路径
     */
    @PostMapping("/init")
    public ResponseEntity<ResultDTO<Map<String, String>>> initMultipartUpload(
            @RequestParam String objectKey,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        logger.info("初始化分片上传: {}, 认证信息: {}", objectKey, authorization != null ? "已提供" : "未提供");
        OSS ossClient = null;
        try {
            // 检查object路径，尊重用户指定的完整路径
            String finalObjectKey = objectKey;
            
            // 判断是否为根目录上传请求 - 根目录上传不添加默认目录前缀
            boolean isRootUpload = !objectKey.contains("/");
            
            if (isRootUpload && !dir.isEmpty()) {
                // 这里是关键修改：不再自动添加目录前缀
                // 保持原始文件名，确保上传到根目录
                logger.info("使用根目录上传文件: {}", finalObjectKey);
            } else if (objectKey.contains("/")) {
                // 用户已指定了目录结构，保持不变
                logger.info("使用用户指定的目录结构: {}", finalObjectKey);
            } else if (!dir.isEmpty()) {
                // 如果用户没有指定目录也没有明确要求根目录，才使用默认目录
                finalObjectKey = dir + objectKey;
                logger.info("使用默认目录上传文件: {}", finalObjectKey);
            }
            
            // 输出关键配置信息用于调试
            logger.info("OSS配置: endpoint={}, bucket={}, 使用密钥ID={}", endpoint, bucket, accessKeyId);
            
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            // 初始化分片上传
            InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucket, finalObjectKey);
            
            // 可以设置存储类型等
            // request.setStorageClass(StorageClass.Standard);
            
            InitiateMultipartUploadResult result = ossClient.initiateMultipartUpload(request);
            String uploadId = result.getUploadId();
            
            // 返回上传ID和对象路径
            Map<String, String> data = new HashMap<>();
            data.put("uploadId", uploadId);
            data.put("objectKey", finalObjectKey);
            
            logger.info("初始化分片上传成功: uploadId={}, objectKey={}", uploadId, finalObjectKey);
            return ResponseEntity.ok(new ResultDTO<>(ResultCode.SUCCESS.getCode(), "初始化成功", data));
        } catch (Exception e) {
            logger.error("初始化分片上传失败", e);
            return ResponseEntity.ok(new ResultDTO<>(ResultCode.FAIL.getCode(), "初始化失败: " + e.getMessage(), null));
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 获取分片上传URL (预签名)
     * @param objectKey 文件路径
     * @param uploadId 上传ID
     * @param partNumber 分片编号
     * @param authorization 认证信息
     * @return 分片上传URL
     */
    @GetMapping("/upload-url")
    public ResponseEntity<ResultDTO<Map<String, String>>> getUploadPartUrl(
            @RequestParam String objectKey,
            @RequestParam String uploadId,
            @RequestParam int partNumber,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        logger.info("获取分片上传URL: objectKey={}, uploadId={}, partNumber={}, 认证信息: {}", 
                objectKey, uploadId, partNumber, authorization != null ? "已提供" : "未提供");
        OSS ossClient = null;
        try {
            // 输出关键配置信息用于调试
            logger.info("OSS配置: endpoint={}, bucket={}, 使用密钥ID={}", endpoint, bucket, accessKeyId);
            
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            
            // 设置URL过期时间为1小时
            Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
            
            // 创建GeneratePresignedUrlRequest对象
            GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucket, objectKey);
            generatePresignedUrlRequest.setExpiration(expiration);
            generatePresignedUrlRequest.setMethod(HttpMethod.PUT);
            generatePresignedUrlRequest.setContentType("application/octet-stream");
            
            // 创建查询参数
            Map<String, String> queryParams = new HashMap<>();
            
            // OSS要求上传ID和分片号作为单独的查询参数
            queryParams.put("uploadId", uploadId);
            queryParams.put("partNumber", String.valueOf(partNumber));
            
            // 设置查询参数
            generatePresignedUrlRequest.setQueryParameter(queryParams);
            
            // 尝试生成URL，增加错误日志
            try {
                String uploadUrl = ossClient.generatePresignedUrl(generatePresignedUrlRequest).toString();
                logger.info("生成的预签名URL: {}", uploadUrl);
                
                // 验证URL格式
                if (!uploadUrl.contains("uploadId=") || !uploadUrl.contains("partNumber=")) {
                    logger.warn("生成的URL可能缺少必要参数: {}", uploadUrl);
                }
                
                Map<String, String> data = new HashMap<>();
                data.put("uploadUrl", uploadUrl);
                data.put("partNumber", String.valueOf(partNumber));
                
                logger.info("获取分片上传URL成功");
                return ResponseEntity.ok(new ResultDTO<>(ResultCode.SUCCESS.getCode(), "获取成功", data));
            } catch (Exception e) {
                logger.error("生成预签名URL时发生错误", e);
                throw e;
            }
        } catch (Exception e) {
            logger.error("获取分片上传URL失败", e);
            return ResponseEntity.ok(new ResultDTO<>(ResultCode.FAIL.getCode(), "获取失败: " + e.getMessage(), null));
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 完成分片上传
     * @param objectKey 文件路径
     * @param uploadId 上传ID
     * @param parts 分片信息列表 [{partNumber: 1, eTag: "xxx"}, ...]
     * @param authorization 认证信息
     * @return 完成结果
     */
    @PostMapping("/complete")
    public ResponseEntity<ResultDTO<Map<String, String>>> completeMultipartUpload(
            @RequestParam String objectKey,
            @RequestParam String uploadId,
            @RequestBody List<Map<String, String>> parts,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        logger.info("完成分片上传: objectKey={}, uploadId={}, parts数量={}, 认证信息: {}", 
                objectKey, uploadId, parts.size(), authorization != null ? "已提供" : "未提供");
        
        // 记录分片详情
        if (logger.isDebugEnabled()) {
            for (int i = 0; i < parts.size(); i++) {
                logger.debug("分片 {}: partNumber={}, eTag={}", 
                        i, parts.get(i).get("partNumber"), parts.get(i).get("eTag"));
            }
        }
        
        OSS ossClient = null;
        try {
            // 输出关键配置信息用于调试
            logger.info("OSS配置: endpoint={}, bucket={}, 使用密钥ID={}", endpoint, bucket, accessKeyId);
            
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            
            // 构建分片信息列表
            List<PartETag> partETags = new ArrayList<>();
            for (Map<String, String> part : parts) {
                int partNumber = Integer.parseInt(part.get("partNumber"));
                String eTag = part.get("eTag");
                partETags.add(new PartETag(partNumber, eTag));
            }
            
            // 排序分片
            partETags.sort(Comparator.comparingInt(PartETag::getPartNumber));
            
            // 构建完成请求
            CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(
                    bucket, objectKey, uploadId, partETags);
            
            // 完成上传
            logger.info("开始合并分片...");
            CompleteMultipartUploadResult result = ossClient.completeMultipartUpload(completeRequest);
            
            // 返回结果
            Map<String, String> data = new HashMap<>();
            data.put("etag", result.getETag());
            data.put("fileUrl", "/" + objectKey); // 使用相对路径，前端可以根据需要构建完整URL
            
            logger.info("完成分片上传成功: objectKey={}, fileUrl={}, ETag={}", 
                    objectKey, data.get("fileUrl"), data.get("etag"));
            return ResponseEntity.ok(new ResultDTO<>(ResultCode.SUCCESS.getCode(), "上传成功", data));
        } catch (Exception e) {
            logger.error("完成分片上传失败", e);
            return ResponseEntity.ok(new ResultDTO<>(ResultCode.FAIL.getCode(), "上传失败: " + e.getMessage(), null));
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 取消分片上传
     * @param objectKey 文件路径
     * @param uploadId 上传ID
     * @param authorization 认证信息
     * @return 取消结果
     */
    @PostMapping("/abort")
    public ResponseEntity<ResultDTO<Boolean>> abortMultipartUpload(
            @RequestParam String objectKey,
            @RequestParam String uploadId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        logger.info("取消分片上传: objectKey={}, uploadId={}, 认证信息: {}", 
                objectKey, uploadId, authorization != null ? "已提供" : "未提供");
        OSS ossClient = null;
        try {
            // 输出关键配置信息用于调试
            logger.info("OSS配置: endpoint={}, bucket={}, 使用密钥ID={}", endpoint, bucket, accessKeyId);
            
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            
            // 取消上传
            AbortMultipartUploadRequest abortRequest = new AbortMultipartUploadRequest(
                    bucket, objectKey, uploadId);
            ossClient.abortMultipartUpload(abortRequest);
            
            logger.info("取消分片上传成功: objectKey={}", objectKey);
            return ResponseEntity.ok(new ResultDTO<>(ResultCode.SUCCESS.getCode(), "取消成功", true));
        } catch (Exception e) {
            logger.error("取消分片上传失败", e);
            return ResponseEntity.ok(new ResultDTO<>(ResultCode.FAIL.getCode(), "取消失败: " + e.getMessage(), false));
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 列出已上传的分片
     * @param objectKey 文件路径
     * @param uploadId 上传ID
     * @return 分片列表
     */
    @GetMapping("/list-parts")
    public ResponseEntity<ResultDTO<List<Map<String, Object>>>> listParts(
            @RequestParam String objectKey,
            @RequestParam String uploadId) {
        logger.info("列出已上传分片: objectKey={}, uploadId={}", objectKey, uploadId);
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            
            // 列出已上传的分片
            ListPartsRequest listPartsRequest = new ListPartsRequest(bucket, objectKey, uploadId);
            PartListing partListing = ossClient.listParts(listPartsRequest);
            
            List<Map<String, Object>> parts = new ArrayList<>();
            for (PartSummary part : partListing.getParts()) {
                Map<String, Object> partInfo = new HashMap<>();
                partInfo.put("partNumber", part.getPartNumber());
                partInfo.put("size", part.getSize());
                partInfo.put("eTag", part.getETag());
                partInfo.put("lastModified", part.getLastModified());
                parts.add(partInfo);
            }
            
            logger.info("列出已上传分片成功: count={}", parts.size());
            return ResponseEntity.ok(new ResultDTO<>(ResultCode.SUCCESS.getCode(), "获取成功", parts));
        } catch (Exception e) {
            logger.error("列出已上传分片失败", e);
            return ResponseEntity.ok(new ResultDTO<>(ResultCode.FAIL.getCode(), "获取失败: " + e.getMessage(), null));
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
} 