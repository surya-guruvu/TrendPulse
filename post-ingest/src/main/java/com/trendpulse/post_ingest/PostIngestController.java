package com.trendpulse.post_ingest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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
        postService.handlePost(postRequest);

        return ResponseEntity.accepted().build();
    }
    

}
