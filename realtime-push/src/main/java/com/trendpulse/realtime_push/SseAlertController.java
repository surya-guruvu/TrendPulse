package com.trendpulse.realtime_push;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/sse/alerts")
public class SseAlertController {

    private final SseAlertHub sseAlertHub;

    public SseAlertController(SseAlertHub sseAlertHub) {
        this.sseAlertHub = sseAlertHub;
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @CrossOrigin
    public SseEmitter streamAlerts(@RequestParam("userId") String userId) {
        return sseAlertHub.createEmitter(userId);
    }
}
