package com.trendpulse.gateway_api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trendpulse.avro.TrendAlert;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlertService {

    @Autowired
    private final ReactiveRedisAvroService redisAvroService;

    public List<TrendAlert> fetchRecentAlerts(String userId) {
        // Seek to recent offsets, filter by key = userId
        // OR: Store last N alerts per user in Redis and fetch from there
        // For now, assume Redis is used
        List<TrendAlert> rawList = redisAvroService.listAvro("alerts:" + userId, TrendAlert.class).collectList().block();

        if(rawList == null){
            return List.of();
        }

        return rawList;
    }
    
}
