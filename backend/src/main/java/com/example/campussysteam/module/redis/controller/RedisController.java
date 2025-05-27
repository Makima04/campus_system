package com.example.campussysteam.module.redis.controller;

import com.example.campussysteam.common.ApiResult;
import com.example.campussysteam.module.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.lang.StringBuilder;
import java.util.HashMap;

/**
 * Redis缓存管理控制器
 */
@RestController
@RequestMapping("/api/redis")
@RequiredArgsConstructor
public class RedisController {

    private static final Logger logger = LoggerFactory.getLogger(RedisController.class);

    private final RedisService redisService;

    /**
     * 获取所有缓存键
     * @return 带有键名、类型和过期时间的键信息列表
     */
    @GetMapping("/keys")
    public ApiResult<List<Map<String, Object>>> getAllKeys() {
        try {
            List<Map<String, Object>> keys = redisService.getAllKeys();
            return ApiResult.success(keys);
        } catch (Exception e) {
            return ApiResult.error("获取缓存键失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定键的值
     * @param key 键名
     * @return 键值
     */
    @GetMapping("/get")
    public ResponseEntity<Map<String, Object>> getValue(@RequestParam String key) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String value = redisService.getValue(key);
            response.put("status", "success");
            response.put("key", key);
            response.put("value", value);
            response.put("exists", value != null);
        } catch (Exception e) {
            logger.error("获取键值失败", e);
            response.put("status", "error");
            response.put("message", "获取键值失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 设置键值对
     * @param key 键名
     * @param value 键值
     * @param ttl 过期时间（秒），0表示永不过期
     * @return 设置结果
     */
    @PostMapping("/set")
    public ResponseEntity<Map<String, Object>> setValue(
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam(defaultValue = "60") long timeout) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            redisService.setValue(key, value, timeout);
            response.put("status", "success");
            response.put("message", "键值对设置成功");
            response.put("key", key);
            response.put("timeout", timeout);
        } catch (Exception e) {
            logger.error("设置键值对失败", e);
            response.put("status", "error");
            response.put("message", "设置键值对失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 删除指定键
     * @param key 键名
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteKey(@RequestParam String key) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            redisService.deleteKey(key);
            response.put("status", "success");
            response.put("message", "键删除成功");
            response.put("key", key);
        } catch (Exception e) {
            logger.error("删除键失败", e);
            response.put("status", "error");
            response.put("message", "删除键失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取键的剩余过期时间
     * @param key 键名
     * @return 剩余秒数
     */
    @GetMapping("/ttl")
    public ApiResult<Long> getTTL(@RequestParam String key) {
        try {
            long ttl = redisService.getTTL(key);
            if (ttl == -2) {
                return ApiResult.error("键不存在");
            }
            return ApiResult.success(ttl);
        } catch (Exception e) {
            return ApiResult.error("获取过期时间失败: " + e.getMessage());
        }
    }

    /**
     * 清空所有缓存
     * @return 清空结果
     */
    @DeleteMapping("/flush")
    public ApiResult<Void> flushAll() {
        try {
            redisService.flushAll();
            return ApiResult.success();
        } catch (Exception e) {
            return ApiResult.error("清空缓存失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行缓存击穿测试
     * @param userId 测试用户ID
     * @param concurrency 并发请求数
     * @return 测试结果
     */
    @GetMapping("/test/penetration")
    public ApiResult<Map<String, Object>> testCachePenetration(
            @RequestParam(defaultValue = "1") String userId,
            @RequestParam(defaultValue = "5") int concurrency) {
        Map<String, Object> result;
        try {
            result = redisService.testCachePenetration("user:" + userId, concurrency);
        } catch (Exception e) {
            logger.error("缓存击穿测试失败", e);
            return ApiResult.error("缓存击穿测试失败: " + e.getMessage());
        }

        // 检查结果中是否包含错误
        if (result.containsKey("error")) {
            return ApiResult.error(result.get("error").toString());
        }

        // 格式化开始和结束时间
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        long startTime = (long) result.get("testStartTime");
        long endTime = (long) result.get("testEndTime");
        result.put("formattedStartTime", sdf.format(new Date(startTime)));
        result.put("formattedEndTime", sdf.format(new Date(endTime)));
        
        // 分析缓存防护机制的有效性
        long dbHits = ((Number) result.get("estimatedDbHits")).longValue();
        long totalRequests = ((Number) result.get("concurrentRequests")).longValue();
        String cacheHitRate = (String) result.get("cacheHitRate");
        
        String effectiveness;
        if (dbHits == 1) {
            effectiveness = "有效 - 仅首次请求需要访问数据库";
        } else if (dbHits <= 3) {
            effectiveness = "大部分有效 - 少数请求需要访问数据库";
        } else if (dbHits < totalRequests / 2) {
            effectiveness = "部分有效 - 多个请求访问了数据库";
        } else {
            effectiveness = "效果不佳 - 大部分请求都访问了数据库";
        }
        result.put("cacheEffectiveness", effectiveness);
        
        // 添加并发效率评估
        if (result.containsKey("concurrencyEfficiency")) {
            String concurrencyMessage = (String) result.get("concurrencyEfficiency");
            result.put("concurrencyMessage", concurrencyMessage);
        }
        
        // 构建测试结果消息
        StringBuilder message = new StringBuilder();
        message.append("缓存击穿测试完成，并发请求数: ").append(totalRequests);
        message.append("，数据库访问次数: ").append(dbHits);
        message.append("，缓存命中率: ").append(cacheHitRate);
        
        if (result.containsKey("warning")) {
            message.append("\n警告: ").append(result.get("warning"));
        }
        
        result.put("message", message.toString());
        
        // 移除不必要的详细数据以减少响应大小
        result.remove("threadResults");
        
        return ApiResult.success(result);
    }

    /**
     * 健康检查接口
     * 用于测试Redis连接是否正常
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 尝试设置一个测试键
            String testKey = "health:check:" + System.currentTimeMillis();
            String testValue = "OK-" + System.currentTimeMillis();
            
            logger.info("尝试设置Redis测试键: {}", testKey);
            redisService.setValue(testKey, testValue, 60); // 60秒过期
            
            // 尝试获取刚刚设置的键
            String retrievedValue = redisService.getValue(testKey);
            
            if (testValue.equals(retrievedValue)) {
                logger.info("Redis连接正常，键值匹配: {} = {}", testKey, retrievedValue);
                response.put("status", "UP");
                response.put("message", "Redis连接正常");
                response.put("testKey", testKey);
                response.put("testValue", testValue);
                response.put("retrievedValue", retrievedValue);
            } else {
                logger.warn("Redis连接异常，键值不匹配: {} != {}", testValue, retrievedValue);
                response.put("status", "DEGRADED");
                response.put("message", "Redis连接异常，键值不匹配");
                response.put("testValue", testValue);
                response.put("retrievedValue", retrievedValue);
            }
        } catch (Exception e) {
            logger.error("Redis连接失败", e);
            response.put("status", "DOWN");
            response.put("message", "Redis连接失败: " + e.getMessage());
            response.put("errorType", e.getClass().getName());
        }
        
        return ResponseEntity.ok(response);
    }
} 