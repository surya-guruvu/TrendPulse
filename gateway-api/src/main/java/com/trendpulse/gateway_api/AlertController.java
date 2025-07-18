package com.trendpulse.gateway_api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trendpulse.avro.TrendAlert;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.security.oauth2.jwt.Jwt;



@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {
    
    @Autowired
    private final AlertService alertService;

    @GetMapping
    public List<TrendAlert> getAlerts(@AuthenticationPrincipal Jwt principal) {
        // String userId = principal.getSubject();
        String userId = principal.getClaimAsString("preferred_username");
        return alertService.fetchRecentAlerts(userId);
    }
}
