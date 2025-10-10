package com.trendpulse.realtime_push;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.trendpulse.avro.TrendAlert;

@Component
public class SseAlertHub {

    private static final long DEFAULT_TIMEOUT_MS = 30 * 60 * 1000L; // 30 minutes

    private final Map<String, Set<SseEmitter>> userIdToEmitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(String userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT_MS);
        userIdToEmitters.computeIfAbsent(userId, key -> new CopyOnWriteArraySet<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        return emitter;
    }

    public void sendToUser(String userId, TrendAlert alert) {
        Set<SseEmitter> emitters = userIdToEmitters.get(userId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .name("trend-alert")
                        .data(alert)
                        .id(String.valueOf(alert.getWindowStart()));
                emitter.send(event);
            } catch (IOException e) {
                removeEmitter(userId, emitter);
            }
        }
    }

    private void removeEmitter(String userId, SseEmitter emitter) {
        Set<SseEmitter> emitters = userIdToEmitters.get(userId);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            userIdToEmitters.remove(userId);
        }
    }
}
