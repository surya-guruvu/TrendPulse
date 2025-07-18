package com.trendpulse.gateway_api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class InterestController {
    
    @Autowired
    private final InterestService interestService;

    @PostMapping
    public ResponseEntity<?> follow(@AuthenticationPrincipal Jwt principal, @RequestBody FollowRequest followRequest) {
        // String userId = principal.getSubject();
        String userId = principal.getClaimAsString("preferred_username");
        interestService.follow(userId, followRequest.hashtags());

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> unfollow(@AuthenticationPrincipal Jwt principal, @RequestBody FollowRequest followRequest){
        // String userId = principal.getSubject();
        String userId = principal.getClaimAsString("preferred_username");
        interestService.unfollow(userId, followRequest.hashtags());

        return ResponseEntity.ok().build();
    }    
}
