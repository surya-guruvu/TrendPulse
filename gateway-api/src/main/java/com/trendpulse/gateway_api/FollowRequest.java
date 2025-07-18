package com.trendpulse.gateway_api;

import java.util.List;

public record FollowRequest(List<String> hashtags) {

    public FollowRequest{
        if (hashtags == null || hashtags.isEmpty()) {
            throw new IllegalArgumentException("Hashtags are not empty");           
        }
    }
} 

