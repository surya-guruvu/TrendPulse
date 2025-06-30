package com.trendpulse.post_ingest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PostRequest(

    @NotBlank(message = "User ID cannot be blank")
    String userId,

    @NotBlank(message = "Text cannot be blank")
    @Size(max = 280, message = "Text cannot exceed 280 characters")
    @Size(min = 1, message = "Text must be at least 1 character long")
    @Pattern(
        regexp = "^[\\p{L}\\p{N}\\p{P}\\p{Z}]+$",
        message = "Text can only contain letters, numbers, punctuation, and whitespace"
    )
    String text
)
{
}
