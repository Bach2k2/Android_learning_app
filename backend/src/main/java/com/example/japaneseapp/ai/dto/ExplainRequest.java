package com.example.japaneseapp.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExplainRequest(
        @NotBlank @Size(max = 2000) String text
) {
}
