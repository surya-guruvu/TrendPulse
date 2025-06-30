package com.trendpulse.post_ingest;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.trendpulse.avro.PostCreated;

@Service
public class PostService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, PostCreated> kafkaTemplate;

    private static final Duration DEDUPE_TTL = Duration.ofMinutes(10);

    public PostService(@Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate, KafkaTemplate<String, PostCreated> kafkaTemplate) {
        this.redisTemplate = redisTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void handlePost(PostRequest postRequest) {
        String redisKey = "DEDUPE:" + postRequest.userId() + ":" + postRequest.text().hashCode();

        Boolean firstSeen = redisTemplate.opsForValue().setIfAbsent(redisKey, "1", DEDUPE_TTL);

        if(!Boolean.TRUE.equals(firstSeen)) {
            throw new DuplicatePostException(postRequest.text());
        }

        PostCreated event = PostCreated.newBuilder()
                .setPostId(UUID.randomUUID().toString())
                .setUserId(postRequest.userId())
                .setText(postRequest.text())
                .setTimestamp(System.currentTimeMillis())
                .build();

        kafkaTemplate.send("post.raw", event.getPostId().toString(), event);
    }

    
}
