package com.example.japaneseapp.progress.dto;

import com.example.japaneseapp.progress.ProgressStatus;

import java.time.Instant;

public record ProgressDto(
        Long lessonId,
        ProgressStatus status,
        Instant lastViewedAt,
        Integer quizScore
) {
}
