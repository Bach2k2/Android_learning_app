package com.example.japaneseapp.ai.dto;

import com.example.japaneseapp.lesson.dto.QuizQuestionDto;

import java.util.List;

public record GenerateQuizResponse(
        Long lessonId,
        List<QuizQuestionDto> questions
) {
}
