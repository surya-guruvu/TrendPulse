package com.trendpulse.post_ingest;


public record PostRequest(
    String userId,
    String text
)
{
    public PostRequest {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or blank");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text cannot be null or blank");
        }
    }
}
