package com.trendpulse.post_ingest;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trendpulse.avro.PostCreated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/posts")
public class PostIngestController {

    private final KafkaTemplate<String, PostCreated> kafkaTemplate;

    public PostIngestController(KafkaTemplate<String, PostCreated> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping
    public ResponseEntity<?> postMethodName(@RequestBody PostRequest postRequest) {
        PostCreated event = PostCreated.newBuilder()
                .setPostId(UUID.randomUUID().toString())
                .setUserId(postRequest.userId())
                .setText(postRequest.text())
                .setTimestamp(System.currentTimeMillis())
                .build();

        kafkaTemplate.send("post.raw", event.getPostId().toString(), event);

        return ResponseEntity.accepted().build();
    }
    

}
