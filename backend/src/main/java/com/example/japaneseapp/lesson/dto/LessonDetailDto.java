package com.example.japaneseapp.lesson.dto;

import java.util.List;

public record LessonDetailDto(
        Long id,
        int lessonNumber,
        String title,
        String overview,
        List<VocabularyDto> vocabulary,
        List<GrammarPointDto> grammarPoints,
        List<ExampleSentenceDto> exampleSentences,
        List<QuizQuestionDto> quiz
) {
}
