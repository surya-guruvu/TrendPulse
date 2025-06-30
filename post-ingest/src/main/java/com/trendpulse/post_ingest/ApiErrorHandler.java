package com.trendpulse.post_ingest;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice
public class ApiErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String first = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest().body(Map.of("error", first));
    }

    @ExceptionHandler(DuplicatePostException.class)
    ResponseEntity<Map<String, String>> handleDuplicatePost(DuplicatePostException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
