package com.example.campussysteam.module.oss.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * OSS服务实现类
 * 提供对象存储相关操作
 */
@Service
public class OSSService {
    private static final Logger logger = LoggerFactory.getLogger(OSSService.class);

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
     * 列出OSS中指定前缀的文件
     * @param prefix 文件前缀（相当于目录）
     * @param maxKeys 最大返回数量，默认100
     * @return 文件列表
     */
    public List<Map<String, Object>> listObjects(String prefix, Integer maxKeys) {
        logger.info("列出OSS文件, prefix: {}, maxKeys: {}", prefix, maxKeys);
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 如果前缀为空，使用默认目录
        if (prefix == null || prefix.isEmpty()) {
            prefix = ""; // 允许列出根目录
            logger.info("前缀为空，列出根目录");
        }
        
        // 确保目录格式正确
        if (!prefix.endsWith("/") && !prefix.isEmpty()) {
            prefix = prefix + "/";
            logger.info("调整前缀格式，现在的前缀: {}", prefix);
        }
        
        OSS ossClient = null;
        try {
            // 创建OSSClient实例
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            
            // 设置请求参数
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest(bucket);
            listObjectsRequest.setPrefix(prefix);
            listObjectsRequest.setDelimiter("/"); // 使用分隔符来模拟目录结构
            
            if (maxKeys != null && maxKeys > 0) {
                listObjectsRequest.setMaxKeys(maxKeys);
            }
            
            // 列出文件
            ObjectListing listing = ossClient.listObjects(listObjectsRequest);
            logger.info("OSS查询返回的commonPrefixes数量: {}", listing.getCommonPrefixes().size());
            logger.info("OSS查询返回的objectSummaries数量: {}", listing.getObjectSummaries().size());
            
            // 处理公共前缀（子目录）
            for (String commonPrefix : listing.getCommonPrefixes()) {
                logger.info("处理目录: {}", commonPrefix);
                Map<String, Object> dirObject = new HashMap<>();
                dirObject.put("key", commonPrefix);
                dirObject.put("fileName", getFileName(commonPrefix, prefix));
                dirObject.put("size", 0);
                dirObject.put("lastModified", new Date());
                dirObject.put("isDirectory", true);
                result.add(dirObject);
            }
            
            // 处理文件
            for (OSSObjectSummary objectSummary : listing.getObjectSummaries()) {
                // 跳过列表中包含的目录本身
                if (objectSummary.getKey().equals(prefix)) {
                    logger.info("跳过目录本身: {}", prefix);
                    continue;
                }
                
                logger.info("处理文件: {}, 大小: {}", objectSummary.getKey(), objectSummary.getSize());
                Map<String, Object> fileObject = new HashMap<>();
                fileObject.put("key", objectSummary.getKey());
                fileObject.put("fileName", getFileName(objectSummary.getKey(), prefix));
                fileObject.put("size", objectSummary.getSize());
                fileObject.put("lastModified", objectSummary.getLastModified());
                fileObject.put("isDirectory", false);
                
                // 为文件生成临时URL
                String signedUrl = generateSignedUrl(objectSummary.getKey());
                fileObject.put("url", signedUrl);
                
                result.add(fileObject);
            }
            
            logger.info("列出的文件数量: {}", result.size());
            return result;
        } catch (Exception e) {
            logger.error("列出OSS文件失败", e);
            throw new RuntimeException("列出OSS文件失败: " + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 生成文件签名URL
     * @param objectKey 文件路径
     * @return 签名URL
     */
    public String generateSignedUrl(String objectKey) {
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
            return ossClient.generatePresignedUrl(bucket, objectKey, expiration).toString();
        } catch (Exception e) {
            logger.error("生成文件签名URL失败", e);
            throw new RuntimeException("生成文件签名URL失败: " + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
    
    /**
     * 生成文件下载URL（可在此设置文件下载相关参数）
     * @param objectKey 文件路径
     * @return 下载URL
     */
    public String generateDownloadUrl(String objectKey) {
        return generateSignedUrl(objectKey);
    }
    
    /**
     * 获取不含前缀的文件名
     * @param key 完整的对象键
     * @param prefix 前缀
     * @return 文件名
     */
    private String getFileName(String key, String prefix) {
        if (prefix != null && key.startsWith(prefix)) {
            return key.substring(prefix.length());
        }
        return key;
    }
    
    /**
     * 删除OSS中的文件或目录
     * @param objectKey 文件或目录路径
     * @return 删除结果
     */
    public boolean deleteObject(String objectKey) {
        OSS ossClient = null;
        try {
            logger.info("删除OSS文件或目录: {}", objectKey);
            
            // 创建OSSClient实例
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            
            // 检查是否是目录（以/结尾的路径）
            boolean isDirectory = objectKey.endsWith("/");
            
            if (isDirectory) {
                logger.info("检测到目录删除请求: {}", objectKey);
                return deleteDirectory(ossClient, objectKey);
            } else {
                // 检查文件是否存在
                boolean exists = ossClient.doesObjectExist(bucket, objectKey);
                if (!exists) {
                    logger.warn("要删除的文件不存在: {}", objectKey);
                    return false;
                }
                
                // 删除文件
                ossClient.deleteObject(bucket, objectKey);
                
                // 再次检查确认删除成功
                exists = ossClient.doesObjectExist(bucket, objectKey);
                if (exists) {
                    logger.error("文件删除失败，文件仍然存在: {}", objectKey);
                    return false;
                }
                
                logger.info("文件删除成功: {}", objectKey);
                return true;
            }
        } catch (Exception e) {
            logger.error("删除OSS文件出错", e);
            throw new RuntimeException("删除OSS文件失败: " + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
    
    /**
     * 删除OSS中的目录及其所有内容
     * @param ossClient OSS客户端实例
     * @param dirPath 目录路径
     * @return 删除结果
     */
    private boolean deleteDirectory(OSS ossClient, String dirPath) {
        logger.info("开始删除目录: {}", dirPath);
        boolean success = true;
        
        try {
            // 确保目录路径以/结尾
            if (!dirPath.endsWith("/")) {
                dirPath += "/";
            }
            
            // 列出目录下的所有对象
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest(bucket)
                    .withPrefix(dirPath)
                    .withMaxKeys(1000);
            
            ObjectListing objectListing;
            
            do {
                objectListing = ossClient.listObjects(listObjectsRequest);
                
                // 删除当前页的所有对象
                for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                    String key = objectSummary.getKey();
                    logger.info("正在删除目录中的文件: {}", key);
                    ossClient.deleteObject(bucket, key);
                }
                
                // 设置下一页的起始位置
                listObjectsRequest.setMarker(objectListing.getNextMarker());
            } while (objectListing.isTruncated());
            
            logger.info("目录删除成功: {}", dirPath);
            return true;
        } catch (Exception e) {
            logger.error("删除目录出错: {}", dirPath, e);
            success = false;
            throw new RuntimeException("删除目录失败: " + e.getMessage());
        }
    }
} 