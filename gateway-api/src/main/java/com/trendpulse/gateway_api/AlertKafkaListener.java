package com.trendpulse.gateway_api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.trendpulse.avro.TrendAlert;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlertKafkaListener {

    @Autowired
    private final ReactiveRedisTemplate<String, byte[]> redisTemplate;

    @Autowired
    private final ReactiveRedisAvroService redisAvroService;

    @KafkaListener(
        topics = "alert.trend.spike",
        groupId = "gateway-api",
        containerFactory = "alertKafkaListenerContainerFactory"
    )
    public void onAlert(@Header(KafkaHeaders.RECEIVED_KEY) String userId, TrendAlert trendAlert){

        String redisKey = "alerts:" + userId;

        redisAvroService.pushAvro(redisKey, trendAlert, TrendAlert.class).block();
        redisTemplate.opsForList().trim(redisKey, 0, 19);
    }

}
