package com.util;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {
    @Resource
    private RedisTemplate<String, String> t;
    public void set(String key, String value) {
        t.opsForValue().set(key, value);
    }
    public String get(String key) {
        return t.opsForValue().get(key);
    }
    public Boolean remove(String key) {
        return t.delete(key);
    }

    /**
     * 设置过期时间
     * @param key   键
     * @param timeout 超时时长，long型
     * @param unit 时间单位
     * @return 成功为true
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        return t.expire(key, timeout, unit);
    }

}
