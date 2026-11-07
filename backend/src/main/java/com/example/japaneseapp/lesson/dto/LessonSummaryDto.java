package com.example.japaneseapp.lesson.dto;

public record LessonSummaryDto(
        Long id,
        int lessonNumber,
        String title,
        String overview
) {
}
