package com.example.campussysteam.module.redis.service.impl;

import com.example.campussysteam.module.redis.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Redis服务实现类
 */
@Service
public class RedisServiceImpl implements RedisService {

    private static final Logger logger = LoggerFactory.getLogger(RedisServiceImpl.class);
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        logger.info("RedisServiceImpl初始化，redisTemplate: {}", redisTemplate);
    }

    @Override
    public List<Map<String, Object>> getAllKeys() {
        logger.debug("获取所有Redis键");
        try {
            List<Map<String, Object>> result = redisTemplate.keys("*").stream()
                    .map(key -> {
                        Map<String, Object> keyInfo = new java.util.HashMap<>();
                        keyInfo.put("key", key);
                        keyInfo.put("type", redisTemplate.type(key));
                        keyInfo.put("ttl", redisTemplate.getExpire(key, TimeUnit.SECONDS));
                        return keyInfo;
                    })
                    .collect(Collectors.toList());
            logger.debug("获取到 {} 个Redis键", result.size());
            return result;
        } catch (Exception e) {
            logger.error("获取所有Redis键失败", e);
            throw e;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getValue(String key) {
        logger.debug("获取Redis键值: key={}", key);
        try {
            T value = (T) redisTemplate.opsForValue().get(key);
            logger.debug("获取Redis键值结果: key={}, 值{}存在", key, value != null ? "" : "不");
            return value;
        } catch (Exception e) {
            logger.error("获取Redis键值失败: key={}", key, e);
            throw e;
        }
    }

    @Override
    public <T> void setValue(String key, T value, long ttl) {
        logger.debug("设置Redis键值: key={}, ttl={}", key, ttl);
        try {
            if (ttl > 0) {
                redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
                logger.debug("设置Redis键值成功(带过期时间): key={}, ttl={}", key, ttl);
            } else {
                redisTemplate.opsForValue().set(key, value);
                logger.debug("设置Redis键值成功(永不过期): key={}", key);
            }
        } catch (Exception e) {
            logger.error("设置Redis键值失败: key={}, ttl={}", key, ttl, e);
            throw e;
        }
    }

    @Override
    public boolean deleteKey(String key) {
        logger.debug("删除Redis键: key={}", key);
        try {
            boolean result = Boolean.TRUE.equals(redisTemplate.delete(key));
            logger.debug("删除Redis键结果: key={}, 成功={}", key, result);
            return result;
        } catch (Exception e) {
            logger.error("删除Redis键失败: key={}", key, e);
            throw e;
        }
    }

    @Override
    public long getTTL(String key) {
        logger.debug("获取Redis键过期时间: key={}", key);
        try {
            long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            logger.debug("获取Redis键过期时间结果: key={}, ttl={}", key, ttl);
            return ttl;
        } catch (Exception e) {
            logger.error("获取Redis键过期时间失败: key={}", key, e);
            throw e;
        }
    }

    @Override
    public void flushAll() {
        logger.warn("清空所有Redis缓存");
        try {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
            logger.info("清空所有Redis缓存成功");
        } catch (Exception e) {
            logger.error("清空所有Redis缓存失败", e);
            throw e;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getWithCachePenetrationProtection(String key, String lockKey, Supplier<T> dbFallback, long ttl) {
        logger.debug("带缓存击穿保护获取值: key={}, lockKey={}, ttl={}", key, lockKey, ttl);
        // 1. 先尝试从缓存获取
        T value = getValue(key);
        if (value != null) {
            logger.debug("缓存命中: key={}", key);
            return value;
        }
        logger.debug("缓存未命中，尝试获取分布式锁: key={}, lockKey={}", key, lockKey);
        
        // 2. 尝试获取分布式锁
        boolean locked = tryLock(lockKey, "1", 10, TimeUnit.SECONDS);
        if (!locked) {
            logger.debug("获取分布式锁失败，等待重试: lockKey={}", lockKey);
            // 如果获取锁失败，等待一段时间后重试
            try {
                Thread.sleep(100);
                return getWithCachePenetrationProtection(key, lockKey, dbFallback, ttl);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("获取缓存时被中断: key={}", key, e);
                throw new RuntimeException("获取缓存时被中断", e);
            }
        }
        
        logger.debug("获取分布式锁成功: lockKey={}", lockKey);
        try {
            // 3. 双重检查
            value = getValue(key);
            if (value != null) {
                logger.debug("双重检查缓存命中: key={}", key);
                return value;
            }
            
            logger.debug("双重检查缓存未命中，从数据源获取数据: key={}", key);
            // 4. 从数据库获取数据
            value = dbFallback.get();
            if (value != null) {
                logger.debug("从数据源获取数据成功，存入缓存: key={}, ttl={}", key, ttl);
                // 5. 将数据存入缓存
                setValue(key, value, ttl);
            } else {
                logger.debug("从数据源获取数据为空: key={}", key);
            }
            return value;
        } finally {
            // 6. 释放锁
            logger.debug("释放分布式锁: lockKey={}", lockKey);
            unlock(lockKey);
        }
    }

    @Override
    public Map<String, Object> testCachePenetration(String key, int concurrentRequests) {
        logger.info("执行缓存击穿测试: key={}, concurrentRequests={}", key, concurrentRequests);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("key", key);
        result.put("concurrentRequests", concurrentRequests);
        result.put("startTime", System.currentTimeMillis());

        // 模拟并发请求
        List<Thread> threads = new java.util.ArrayList<>();
        for (int i = 0; i < concurrentRequests; i++) {
            Thread thread = new Thread(() -> {
                getWithCachePenetrationProtection(
                    key,
                    "lock:" + key,
                    () -> {
                        try {
                            Thread.sleep(100); // 模拟数据库查询延迟
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return "test-value";
                    },
                    60
                );
            });
            threads.add(thread);
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        result.put("endTime", System.currentTimeMillis());
        result.put("duration", (long) result.get("endTime") - (long) result.get("startTime"));
        logger.info("缓存击穿测试完成: key={}, duration={}ms", key, result.get("duration"));
        return result;
    }
    
    /**
     * 获取缓存
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        logger.debug("获取缓存: key={}", key);
        try {
            T value = (T) redisTemplate.opsForValue().get(key);
            logger.debug("获取缓存结果: key={}, 值{}存在", key, value != null ? "" : "不");
            return value;
        } catch (Exception e) {
            logger.error("获取缓存失败: key={}", key, e);
            throw e;
        }
    }
    
    /**
     * 删除缓存
     */
    @Override
    public void delete(String key) {
        logger.debug("删除缓存: key={}", key);
        try {
            redisTemplate.delete(key);
            logger.debug("删除缓存成功: key={}", key);
        } catch (Exception e) {
            logger.error("删除缓存失败: key={}", key, e);
            throw e;
        }
    }
    
    /**
     * 判断key是否存在
     */
    @Override
    public boolean hasKey(String key) {
        logger.debug("判断键是否存在: key={}", key);
        try {
            boolean exists = Boolean.TRUE.equals(redisTemplate.hasKey(key));
            logger.debug("判断键是否存在结果: key={}, exists={}", key, exists);
            return exists;
        } catch (Exception e) {
            logger.error("判断键是否存在失败: key={}", key, e);
            throw e;
        }
    }
    
    /**
     * 设置过期时间
     */
    @Override
    public boolean expire(String key, long timeout, TimeUnit unit) {
        logger.debug("设置过期时间: key={}, timeout={}, unit={}", key, timeout, unit);
        try {
            boolean result = Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
            logger.debug("设置过期时间结果: key={}, success={}", key, result);
            return result;
        } catch (Exception e) {
            logger.error("设置过期时间失败: key={}, timeout={}, unit={}", key, timeout, unit, e);
            throw e;
        }
    }
    
    /**
     * 获取过期时间
     */
    @Override
    public long getExpire(String key, TimeUnit unit) {
        logger.debug("获取过期时间: key={}, unit={}", key, unit);
        try {
            long expireTime = redisTemplate.getExpire(key, unit);
            logger.debug("获取过期时间结果: key={}, expireTime={}", key, expireTime);
            return expireTime;
        } catch (Exception e) {
            logger.error("获取过期时间失败: key={}, unit={}", key, unit, e);
            throw e;
        }
    }
    
    /**
     * 递增
     */
    @Override
    public long increment(String key, long delta) {
        logger.debug("递增操作: key={}, delta={}", key, delta);
        try {
            long result = redisTemplate.opsForValue().increment(key, delta);
            logger.debug("递增操作结果: key={}, delta={}, result={}", key, delta, result);
            return result;
        } catch (Exception e) {
            logger.error("递增操作失败: key={}, delta={}", key, delta, e);
            throw e;
        }
    }
    
    /**
     * 递减
     */
    @Override
    public long decrement(String key, long delta) {
        logger.debug("递减操作: key={}, delta={}", key, delta);
        try {
            long result = redisTemplate.opsForValue().decrement(key, delta);
            logger.debug("递减操作结果: key={}, delta={}, result={}", key, delta, result);
            return result;
        } catch (Exception e) {
            logger.error("递减操作失败: key={}, delta={}", key, delta, e);
            throw e;
        }
    }
    
    /**
     * 获取分布式锁
     */
    @Override
    public boolean tryLock(String key, String value, long timeout, TimeUnit unit) {
        logger.debug("尝试获取分布式锁: key={}, value={}, timeout={}, unit={}", key, value, timeout, unit);
        try {
            boolean acquired = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit));
            logger.debug("获取分布式锁结果: key={}, acquired={}", key, acquired);
            return acquired;
        } catch (Exception e) {
            logger.error("获取分布式锁失败: key={}", key, e);
            throw e;
        }
    }
    
    /**
     * 释放分布式锁
     */
    @Override
    public void unlock(String key) {
        logger.debug("释放分布式锁: key={}", key);
        try {
            redisTemplate.delete(key);
            logger.debug("释放分布式锁成功: key={}", key);
        } catch (Exception e) {
            logger.error("释放分布式锁失败: key={}", key, e);
            throw e;
        }
    }

    @Override
    public void set(String key, String value, long timeout) {
        logger.debug("设置字符串缓存: key={}, timeout={}", key, timeout);
        try {
            redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
            logger.debug("设置字符串缓存成功: key={}, timeout={}", key, timeout);
        } catch (Exception e) {
            logger.error("设置字符串缓存失败: key={}, timeout={}", key, timeout, e);
            throw e;
        }
    }

    @Override
    public boolean exists(String key) {
        logger.debug("检查键是否存在: key={}", key);
        try {
            boolean exists = Boolean.TRUE.equals(redisTemplate.hasKey(key));
            logger.debug("检查键是否存在结果: key={}, exists={}", key, exists);
            return exists;
        } catch (Exception e) {
            logger.error("检查键是否存在失败: key={}", key, e);
            throw e;
        }
    }
} 