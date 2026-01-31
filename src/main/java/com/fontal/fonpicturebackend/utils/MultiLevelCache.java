package com.fontal.fonpicturebackend.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 多级缓存工具类（使用 Jackson 序列化，支持 Jackson 注解）
 * - 一级缓存：Caffeine 本地缓存
 * - 二级缓存：Redis 分布式缓存
 *
 * 查询顺序：本地缓存 → Redis → 数据库
 */
@Component
public class MultiLevelCache {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 本地缓存实例
     */
    private final Cache<String, String> localCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    /**
     * 获取缓存
     *
     * @param key 缓存 key
     * @return 缓存值
     */
    public String get(String key) {
        // 1. 查本地缓存
        String value = localCache.getIfPresent(key);
        if (value != null) {
            return value;
        }

        // 2. 查 Redis
        value = stringRedisTemplate.opsForValue().get(key);
        if (value != null) {
            // 回写本地缓存
            localCache.put(key, value);
        }

        return value;
    }

    /**
     * 获取缓存并反序列化
     *
     * @param key   缓存 key
     * @param type 目标类型
     * @return 缓存值
     */
    public <T> T get(String key, Class<T> type) {
        String value = get(key);
        if (value == null) {
            return null;
        }
        if (type == String.class) {
            return (T) value;
        }
        try {
            return objectMapper.readValue(value, type);
        } catch (Exception e) {
            throw new RuntimeException("JSON 反序列化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取缓存并反序列化（支持泛型，如 Page<PictureVO>）
     *
     * @param key         缓存 key
     * @param entityClass 实体类类型（用于 Page<PictureVO> 中的 PictureVO）
     * @return Page 对象
     */
    @SuppressWarnings("unchecked")
    public <T> Page<T> getPage(String key, Class<T> entityClass) {
        String value = get(key);
        if (value == null) {
            return null;
        }
        try {
            // 使用 TypeReference 构造 Page<PictureVO> 类型
            return (Page<T>) objectMapper.readValue(value,
                    objectMapper.getTypeFactory().constructParametricType(Page.class, entityClass));
        } catch (Exception e) {
            throw new RuntimeException("JSON 反序列化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 设置缓存
     *
     * @param key      缓存 key
     * @param value    缓存值
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     */
    public void set(String key, String value, long timeout, TimeUnit timeUnit) {
        // 1. 设置本地缓存
        localCache.put(key, value);

        // 2. 设置 Redis
        stringRedisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * 设置缓存（对象序列化，使用 Jackson 支持 @JsonSerialize 等注解）
     *
     * @param key      缓存 key
     * @param value    缓存对象
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit timeUnit) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            set(key, jsonValue, timeout, timeUnit);
        } catch (Exception e) {
            throw new RuntimeException("JSON 序列化失败", e);
        }
    }

    /**
     * 设置缓存（秒）
     */
    public void set(String key, String value, long seconds) {
        set(key, value, seconds, TimeUnit.SECONDS);
    }

    /**
     * 删除缓存
     *
     * @param key 缓存 key
     */
    public void delete(String key) {
        // 1. 删除本地缓存
        localCache.invalidate(key);

        // 2. 删除 Redis
        stringRedisTemplate.delete(key);
    }

    /**
     * 批量删除缓存（按前缀）
     *
     * @param prefix key 前缀
     */
    public void deleteByPrefix(String prefix) {
        // 1. 清空本地缓存（简单粗暴，Caffeine 不支持按前缀删除）
        localCache.invalidateAll();

        // 2. 删除 Redis 中匹配的 key
        stringRedisTemplate.delete(stringRedisTemplate.keys(prefix + "*"));
    }

    /**
     * 缓存穿透保护：获取缓存，不存在则从数据库加载
     *
     * @param key       缓存 key
     * @param loader    数据加载函数
     * @param timeout   过期时间
     * @param timeUnit  时间单位
     * @param <T>       返回类型
     * @return 缓存值或加载的值
     */
    public <T> T getOrLoad(String key, Function<String, T> loader, long timeout, TimeUnit timeUnit) {
        // 1. 查本地缓存
        String value = localCache.getIfPresent(key);
        if (value != null) {
            return parseValue(value, loader);
        }

        // 2. 查 Redis
        value = stringRedisTemplate.opsForValue().get(key);
        if (value != null) {
            // 回写本地缓存
            localCache.put(key, value);
            return parseValue(value, loader);
        }

        // 3. 加载数据
        T result = loader.apply(key);
        if (result != null) {
            // 写入缓存（使用 Jackson 序列化）
            set(key, result, timeout, timeUnit);
        }

        return result;
    }

    /**
     * 解析缓存值（处理空值标记）
     */
    @SuppressWarnings("unchecked")
    private <T> T parseValue(String value, Function<String, T> loader) {
        if ("EMPTY".equals(value)) {
            return null;
        }
        try {
            return (T) objectMapper.readValue(value, Object.class);
        } catch (Exception e) {
            throw new RuntimeException("JSON 解析失败", e);
        }
    }

    /**
     * 设置空值缓存（防止缓存穿透）
     *
     * @param key 缓存 key
     */
    public void setEmpty(String key) {
        set(key, "EMPTY", 5, TimeUnit.MINUTES);
    }

    /**
     * 判断是否为空值缓存
     */
    public boolean isEmpty(String value) {
        return "EMPTY".equals(value);
    }
}
