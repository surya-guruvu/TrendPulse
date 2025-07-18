package com.trendpulse.gateway_api;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisAvroService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final AvroUtils avroUtils;

    public <T> void pushAvro(String key, T record, Class<T> clazz) {
        byte[] bytes = avroUtils.serializeAvro(record, clazz);
        redisTemplate.opsForList().leftPush(key, bytes);
    }

    public <T> T popAvro(String key, Class<T> clazz) {
        byte[] data = (byte[]) redisTemplate.opsForList().leftPop(key);
        return avroUtils.deserializeAvro(data, clazz);
    }

    public <T> List<T> listAvro(String key, Class<T> clazz) {
        List<Object> raw = redisTemplate.opsForList().range(key, 0, -1);
        return raw.stream()
                  .map(b -> avroUtils.deserializeAvro((byte[]) b, clazz))
                  .toList();
    }

    public <T> void setAvro(String key, T record, Class<T> clazz) {
        byte[] bytes = avroUtils.serializeAvro(record, clazz);
        redisTemplate.opsForValue().set(key, bytes);
    }

    public <T> T getAvro(String key, Class<T> clazz) {
        byte[] bytes = (byte[]) redisTemplate.opsForValue().get(key);
        if (bytes == null) {
            return null; // or throw new NotFoundException(key)
        }
        return avroUtils.deserializeAvro(bytes, clazz);
    }
    
    public void trimList(String key, int maxSize) {
        redisTemplate.opsForList().trim(key, 0, maxSize - 1);
    }
}
