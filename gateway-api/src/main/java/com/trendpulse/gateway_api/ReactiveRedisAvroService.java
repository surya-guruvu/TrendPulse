package com.trendpulse.gateway_api;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ReactiveRedisAvroService {

    private final ReactiveRedisTemplate<String, byte[]> reactiveRedisTemplate;
    private final AvroUtils avroUtils;

    public <T> Mono<Long> pushAvro(String key, T record, Class<T> clazz) {
        byte[] bytes = avroUtils.serializeAvro(record, clazz);
        return reactiveRedisTemplate.opsForList().leftPush(key, bytes);
    }

    public <T> Mono<T> popAvro(String key, Class<T> clazz) {
        return reactiveRedisTemplate.opsForList().leftPop(key)
                .map(data -> avroUtils.deserializeAvro(data, clazz));
    }

    public <T> Flux<T> listAvro(String key, Class<T> clazz) {
        return reactiveRedisTemplate.opsForList().range(key, 0, -1)
                .map(data -> avroUtils.deserializeAvro(data, clazz));
    }

    public <T> Mono<Boolean> setAvro(String key, T record, Class<T> clazz) {
        byte[] bytes = avroUtils.serializeAvro(record, clazz);
        return reactiveRedisTemplate.opsForValue().set(key, bytes);
    }

    public <T> Mono<T> getAvro(String key, Class<T> clazz) {
        return reactiveRedisTemplate.opsForValue().get(key)
                .map(bytes -> avroUtils.deserializeAvro(bytes, clazz));
    }

    public Mono<Boolean> trimList(String key, int maxSize) {
        return reactiveRedisTemplate.opsForList().trim(key, 0, maxSize - 1);
    }
}
