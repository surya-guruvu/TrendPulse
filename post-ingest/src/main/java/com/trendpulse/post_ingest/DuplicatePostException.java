package com.trendpulse.post_ingest;

public class DuplicatePostException extends RuntimeException {
    public DuplicatePostException(String id) {
        super("Duplicate post detected with ID: " + id);
    }
}
