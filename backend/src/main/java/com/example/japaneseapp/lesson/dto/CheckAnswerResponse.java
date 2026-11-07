package com.example.japaneseapp.lesson.dto;

public record CheckAnswerResponse(
        boolean correct,
        int correctIndex,
        String explanation
) {
}
