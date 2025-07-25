package com.trendpulse.gateway_api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    
    // @Bean
    // public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory(){
    //     return new LettuceConnectionFactory();
    // }

    @Bean
    public ReactiveRedisTemplate<String, byte[]> reactiveRedisTemplate(ReactiveRedisConnectionFactory redisConnectionFactory) {
        RedisSerializationContext<String, byte[]> serializationContext = RedisSerializationContext
                .<String, byte[]>newSerializationContext(new StringRedisSerializer())
                .value(RedisSerializer.byteArray())
                .build();

        return new ReactiveRedisTemplate<>(redisConnectionFactory, serializationContext);
    }
}

