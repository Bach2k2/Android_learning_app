package com.example.japaneseapp.lesson.dto;

import java.util.List;
import java.util.UUID;

public record QuizQuestionDto(
        UUID id,
        String prompt,
        List<String> options
) {
}
