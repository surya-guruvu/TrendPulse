package com.trendpulse.post_ingest;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trendpulse.avro.PostCreated;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/posts")
public class PostIngestController {

    private final PostService postService;

    public PostIngestController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<?> postMethodName(@Valid @RequestBody PostRequest postRequest) {
        PostCreated event = PostCreated.newBuilder()
                .setPostId(UUID.randomUUID().toString())
                .setUserId(postRequest.userId())
                .setText(postRequest.text())
                .setTimestamp(System.currentTimeMillis())
                .build();

        postService.handlePost(postRequest);

        return ResponseEntity.accepted().build();
    }
    

}
