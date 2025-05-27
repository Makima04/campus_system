package com.example.campussysteam.module.redis.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis服务接口
 */
public interface RedisService {
    
    /**
     * 获取所有缓存键
     * @return 带有键名、类型和过期时间的键信息列表
     */
    List<Map<String, Object>> getAllKeys();
    
    /**
     * 获取指定键的值
     * @param key 键名
     * @return 键值
     */
    <T> T getValue(String key);
    
    /**
     * 设置键值对
     * @param key 键名
     * @param value 键值
     * @param ttl 过期时间（秒），0表示永不过期
     */
    <T> void setValue(String key, T value, long ttl);
    
    /**
     * 删除指定键
     * @param key 键名
     * @return 是否成功删除
     */
    boolean deleteKey(String key);
    
    /**
     * 获取键的剩余过期时间
     * @param key 键名
     * @return 剩余秒数，-1表示永不过期，-2表示键不存在
     */
    long getTTL(String key);
    
    /**
     * 清空所有缓存
     */
    void flushAll();
    
    /**
     * 防止缓存击穿的获取值方法
     * 使用分布式锁和双重检查机制避免缓存击穿问题
     * 
     * @param key 缓存键
     * @param lockKey 分布式锁键
     * @param dbFallback 当缓存未命中时从数据库获取数据的回调函数
     * @param ttl 缓存过期时间（秒）
     * @param <T> 返回值类型
     * @return 缓存值或数据库查询结果
     */
    <T> T getWithCachePenetrationProtection(String key, String lockKey, Supplier<T> dbFallback, long ttl);
    
    /**
     * 执行缓存击穿测试
     * @param key 测试的键
     * @param concurrentRequests 并发请求数
     * @return 测试结果信息
     */
    Map<String, Object> testCachePenetration(String key, int concurrentRequests);
    
    /**
     * 获取缓存
     */
    <T> T get(String key);
    
    /**
     * 删除缓存
     */
    void delete(String key);
    
    /**
     * 判断key是否存在
     */
    boolean hasKey(String key);
    
    /**
     * 设置过期时间
     */
    boolean expire(String key, long timeout, TimeUnit unit);
    
    /**
     * 获取过期时间
     */
    long getExpire(String key, TimeUnit unit);
    
    /**
     * 递增
     */
    long increment(String key, long delta);
    
    /**
     * 递减
     */
    long decrement(String key, long delta);
    
    /**
     * 获取分布式锁
     */
    boolean tryLock(String key, String value, long timeout, TimeUnit unit);
    
    /**
     * 释放分布式锁
     */
    void unlock(String key);

    /**
     * 设置缓存
     */
    void set(String key, String value, long timeout);
    
    /**
     * 判断key是否存在
     */
    boolean exists(String key);
} 