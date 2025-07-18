package com.trendpulse.gateway_api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.trendpulse.avro.TrendAlert;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlertService {

    @Autowired
    private final RedisTemplate<String,Object> redisTemplate;

    public List<TrendAlert> fetchRecentAlerts(String userId) {
        // Seek to recent offsets, filter by key = userId
        // OR: Store last N alerts per user in Redis and fetch from there
        // For now, assume Redis is used
        List<Object> rawList = redisTemplate.opsForList().range("alerts:" + userId, 0, 19);

        if(rawList == null){
            return List.of();
        }

        return rawList.stream().filter(TrendAlert.class::isInstance)
                                .map(TrendAlert.class::cast)
                                .toList();
    }
    
}
