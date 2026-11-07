package com.example.japaneseapp.ai;

import com.example.japaneseapp.lesson.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AiQuizIsolationTest {
    @Test
    void generatedQuestionsAreSavedSeparatelyFromCuratedQuizRows() {
        OllamaClient ollama = mock(OllamaClient.class);
        LessonRepository lessons = mock(LessonRepository.class);
        VocabularyRepository vocabulary = mock(VocabularyRepository.class);
        GrammarPointRepository grammar = mock(GrammarPointRepository.class);
        LessonExampleSentenceRepository examples = mock(LessonExampleSentenceRepository.class);
        GeneratedQuizQuestionRepository generatedQuestions = mock(GeneratedQuizQuestionRepository.class);
        Lesson lesson = mock(Lesson.class);
        when(lessons.findById(1L)).thenReturn(Optional.of(lesson));
        when(vocabulary.findAllByLesson_IdOrderByKanaAsc(1L)).thenReturn(List.of());
        when(grammar.findAllByLesson_IdOrderByTitleAsc(1L)).thenReturn(List.of());
        when(examples.findAllByLesson_IdOrderByJapaneseAsc(1L)).thenReturn(List.of());
        when(ollama.generate(anyString())).thenReturn("""
                {"questions":[{"prompt":"What does こんにちは mean?","options":["Hello","Goodbye"],"correctIndex":0,"explanation":"A daytime greeting."}]}
                """);
        AiService service = new AiService(ollama, lessons, vocabulary, grammar, examples,
                generatedQuestions, new ObjectMapper());

        var result = service.generateQuizForLesson(1L, 1);

        assertEquals(1, result.size());
        verify(generatedQuestions).saveAll(anyList());
    }
}
