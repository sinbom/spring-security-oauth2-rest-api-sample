package me.nuguri.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate redisTemplate;

    /**
     * 레디스 캐시 key, value 저장
     * @param key
     * @param value
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 레디스 캐시 지정 시간 key, value 저장
     * @param key
     * @param value
     * @param expireSecond
     */
    public void set(String key, Object value, Long expireSecond) {
        redisTemplate.opsForValue().set(key, value, expireSecond, TimeUnit.SECONDS);
    }

    /**
     * 레디스 캐시 데이터 추출
     * @param key
     * @return
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

}
