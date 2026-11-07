package com.example.japaneseapp.lesson.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CheckAnswerRequest(
        @Min(0) @Max(10) int selectedIndex
) {
}
