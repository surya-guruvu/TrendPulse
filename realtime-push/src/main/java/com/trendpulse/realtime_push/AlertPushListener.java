package com.trendpulse.realtime_push;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.trendpulse.avro.TrendAlert;

@Service
public class AlertPushListener {

    private final SseAlertHub sseAlertHub;

    public AlertPushListener(SseAlertHub sseAlertHub) {
        this.sseAlertHub = sseAlertHub;
    }

    @KafkaListener(
        topics = "alert.trend.spike",
        groupId = "realtime-push",
        containerFactory = "alertKafkaListenerContainerFactory"
    )
    public void onAlert(@Header(KafkaHeaders.RECEIVED_KEY) String userId, TrendAlert alert) {
        if (userId == null || alert == null) {
            return;
        }
        sseAlertHub.sendToUser(userId, alert);
    }
}
