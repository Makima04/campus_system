package com.example.campussysteam.module.oss.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.example.campussysteam.config.ServerConfig;
import com.example.campussysteam.common.ResultDTO;
import com.example.campussysteam.common.ResultCode;
import com.example.campussysteam.module.oss.service.OSSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/oss")
public class OSSController {

    private static final Logger logger = LoggerFactory.getLogger(OSSController.class);

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
    
    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private OSSService ossService;

    @GetMapping("/signature")
    public ResponseEntity<?> getSignature() {
        logger.info("获取OSS上传签名");
        try {
            // 设置过期时间较长以确保足够的上传时间
            long expireTime = 300; // 5分钟
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            
            // 限制文件大小最大为100MB
            long maxSize = 100 * 1024 * 1024;
            
            // 创建OSSClient实例
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            // 创建上传策略
            PolicyConditions policyConds = new PolicyConditions();
            // 添加文件大小限制
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, maxSize);
            
            // 允许上传到任何位置（根目录或子目录）
            // 移除对 dir 的限制
            // policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            // 生成上传策略
            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes(StandardCharsets.UTF_8);
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            // 构造返回结果
            Map<String, String> respMap = new LinkedHashMap<>();
            respMap.put("accessId", accessKeyId);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir); // 继续提供默认目录作为参考
            respMap.put("host", "https://" + bucket + "." + endpoint);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            
            logger.info("OSS上传签名生成成功: {}", respMap);
            logger.info("现在允许上传到任何目录位置，不限于默认目录 {}", dir);
            ossClient.shutdown();
            return ResponseEntity.ok(respMap);
        } catch (Exception e) {
            logger.error("获取OSS上传签名失败", e);
            return ResponseEntity.status(500).body("获取OSS上传签名失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/get-signed-url")
    public ResponseEntity<?> getSignedUrl(@RequestParam String objectName) {
        logger.info("获取OSS文件签名URL: {}", objectName);
        try {
            // 创建OSSClient实例
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            
            // 设置签名URL过期时间为12小时
            Date expiration = new Date(System.currentTimeMillis() + 12 * 3600 * 1000);
            
            // 生成签名URL
            String signedUrl = ossClient.generatePresignedUrl(bucket, objectName, expiration).toString();
            
            // 关闭OSSClient
            ossClient.shutdown();
            
            Map<String, String> result = new LinkedHashMap<>();
            result.put("signedUrl", signedUrl);
            result.put("expire", String.valueOf(expiration.getTime() / 1000));
            
            logger.info("OSS签名URL生成成功: {}", signedUrl);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取OSS签名URL失败", e);
            return ResponseEntity.status(500).body("获取OSS签名URL失败: " + e.getMessage());
        }
    }

    /**
     * 检查OSS连接和配置
     */
    @GetMapping("/check-config")
    public ResponseEntity<?> checkOSSConfig() {
        logger.info("检查OSS配置");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("endpoint", endpoint);
        result.put("bucket", bucket);
        result.put("dir", dir);

        try {
            // 创建OSSClient实例
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            
            // 检查Bucket是否存在
            boolean exists = ossClient.doesBucketExist(bucket);
            result.put("bucketExists", exists);
            
            if (exists) {
                // 检查Bucket访问权限
                String acl = ossClient.getBucketAcl(bucket).toString();
                result.put("bucketAcl", acl);
                
                // 添加测试对象，用于验证
                String testKey = dir + "test-" + System.currentTimeMillis() + ".txt";
                ossClient.putObject(bucket, testKey, new java.io.ByteArrayInputStream("Test Content".getBytes()));
                
                // 生成该对象的签名URL
                Date expiration = new Date(System.currentTimeMillis() + 60 * 1000); // 1分钟
                String testUrl = ossClient.generatePresignedUrl(bucket, testKey, expiration).toString();
                result.put("testUrl", testUrl);
                
                // 删除测试对象
                ossClient.deleteObject(bucket, testKey);
            }
            
            ossClient.shutdown();
            result.put("status", "success");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("OSS配置检查失败", e);
            result.put("status", "error");
            result.put("message", e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 列出OSS文件
     * @param prefix 文件前缀（目录路径）
     * @param maxKeys 最大返回数量
     * @return 文件列表
     */
    @GetMapping("/list")
    public ResponseEntity<ResultDTO<List<Map<String, Object>>>> listFiles(
            @RequestParam(required = false) String prefix,
            @RequestParam(required = false, defaultValue = "100") Integer maxKeys) {
        logger.info("列出OSS文件, prefix: {}, maxKeys: {}", prefix, maxKeys);
        try {
            checkOSSConfig();
            List<Map<String, Object>> files = ossService.listObjects(prefix, maxKeys);
            return ResponseEntity.ok(new ResultDTO<>(ResultCode.SUCCESS.getCode(), "获取成功", files));
        } catch (Exception e) {
            logger.error("列出OSS文件失败", e);
            return ResponseEntity.ok(new ResultDTO<>(ResultCode.FAIL.getCode(), "获取失败: " + e.getMessage(), null));
        }
    }
    
    /**
     * 获取文件下载URL
     * @param objectKey 文件路径
     * @return 下载URL
     */
    @GetMapping("/download-url")
    public ResponseEntity<ResultDTO<String>> getDownloadUrl(@RequestParam String objectKey) {
        logger.info("获取文件下载URL: {}", objectKey);
        try {
            checkOSSConfig();
            String downloadUrl = ossService.generateDownloadUrl(objectKey);
            return ResponseEntity.ok(new ResultDTO<>(ResultCode.SUCCESS.getCode(), "获取成功", downloadUrl));
        } catch (Exception e) {
            logger.error("获取文件下载URL失败", e);
            return ResponseEntity.ok(new ResultDTO<>(ResultCode.FAIL.getCode(), "获取失败: " + e.getMessage(), null));
        }
    }
    
    /**
     * 删除OSS文件
     * @param objectKey 文件路径
     * @return 操作结果
     */
    @PostMapping("/delete-file")
    public ResponseEntity<ResultDTO<Boolean>> deleteFile(@RequestBody Map<String, String> request,
                                                       @RequestHeader("Authorization") String authHeader) {
        String objectKey = request.get("objectKey");
        logger.info("-------------- 删除OSS文件请求开始 --------------");
        logger.info("接收到删除OSS文件请求，文件路径: {}", objectKey);
        logger.info("请求头Authorization: {}", authHeader.substring(0, Math.min(15, authHeader.length())) + "...");
        
        if (objectKey == null || objectKey.isEmpty()) {
            logger.warn("文件路径为空，返回400错误");
            return ResponseEntity.ok(new ResultDTO<>(ResultCode.PARAM_ERROR.getCode(), "文件路径不能为空", false));
        }
        
        // 从Authorization头中提取JWT令牌并验证用户权限
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("认证头无效，缺少Bearer前缀，返回401错误");
            return ResponseEntity.ok(new ResultDTO<>(ResultCode.UNAUTHORIZED.getCode(), "用户未认证", false));
        }
        
        // 提取令牌
        String token = authHeader.substring(7);
        logger.info("成功提取令牌: {}...", token.substring(0, Math.min(10, token.length())));
        
        // 添加详细的令牌验证和用户权限日志
        try {
            // 从当前安全上下文中获取用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                logger.info("当前用户: {}", authentication.getName());
                logger.info("用户权限: {}", authentication.getAuthorities());
                logger.info("认证详情: {}", authentication.getDetails());
                logger.info("认证主体: {}", authentication.getPrincipal());
                
                // 修改检查用户角色的逻辑，同时接受 ADMIN 和 ROLE_ADMIN 两种格式
                boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> {
                        String authority = a.getAuthority();
                        logger.info("检查权限: {}", authority);
                        return authority.equals("ROLE_ADMIN") || authority.equals("ADMIN");
                    });
                
                if (!isAdmin) {
                    logger.warn("用户 {} 尝试删除文件，但没有管理员权限", authentication.getName());
                    return ResponseEntity.ok(new ResultDTO<>(ResultCode.FORBIDDEN.getCode(), "只有管理员可以删除文件", false));
                }
                
                logger.info("用户 {} 有管理员权限，允许删除文件", authentication.getName());
            } else {
                logger.warn("无法获取认证信息，可能是因为过滤器链配置问题");
                return ResponseEntity.ok(new ResultDTO<>(ResultCode.UNAUTHORIZED.getCode(), "无法验证用户身份", false));
            }
        } catch (Exception e) {
            logger.error("验证用户权限时出错", e);
            return ResponseEntity.ok(new ResultDTO<>(ResultCode.SERVER_ERROR.getCode(), "验证用户权限时出错: " + e.getMessage(), false));
        }
        
        try {
            boolean result = ossService.deleteObject(objectKey);
            logger.info("文件删除操作完成，结果: {}", result ? "成功" : "失败");
            logger.info("-------------- 删除OSS文件请求结束 --------------");
            return ResponseEntity.ok(new ResultDTO<>(ResultCode.SUCCESS.getCode(), "删除成功", result));
        } catch (Exception e) {
            logger.error("删除OSS文件失败", e);
            logger.info("-------------- 删除OSS文件请求结束(出错) --------------");
            return ResponseEntity.ok(new ResultDTO<>(ResultCode.FAIL.getCode(), "删除失败: " + e.getMessage(), false));
        }
    }
} 