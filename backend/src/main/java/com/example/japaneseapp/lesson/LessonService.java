package com.example.japaneseapp.lesson;

import com.example.japaneseapp.common.ApiException;
import com.example.japaneseapp.lesson.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final VocabularyRepository vocabularyRepository;
    private final GrammarPointRepository grammarPointRepository;
    private final LessonExampleSentenceRepository exampleSentenceRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final GeneratedQuizQuestionRepository generatedQuizQuestionRepository;
    private final ObjectMapper objectMapper;

    public LessonService(
            LessonRepository lessonRepository,
            VocabularyRepository vocabularyRepository,
            GrammarPointRepository grammarPointRepository,
            LessonExampleSentenceRepository exampleSentenceRepository,
            QuizQuestionRepository quizQuestionRepository,
            GeneratedQuizQuestionRepository generatedQuizQuestionRepository,
            ObjectMapper objectMapper
    ) {
        this.lessonRepository = lessonRepository;
        this.vocabularyRepository = vocabularyRepository;
        this.grammarPointRepository = grammarPointRepository;
        this.exampleSentenceRepository = exampleSentenceRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.generatedQuizQuestionRepository = generatedQuizQuestionRepository;
        this.objectMapper = objectMapper;
    }

    public List<LessonSummaryDto> listLessons() {
        return lessonRepository.findAll().stream()
                .sorted((a, b) -> Integer.compare(a.getLessonNumber(), b.getLessonNumber()))
                .map(l -> new LessonSummaryDto(l.getId(), l.getLessonNumber(), l.getTitle(), l.getOverview()))
                .toList();
    }

    public LessonDetailDto getLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Lesson not found"));

        var vocab = vocabularyRepository.findAllByLesson_IdOrderByKanaAsc(lessonId).stream()
                .map(v -> new VocabularyDto(v.getId(), v.getKana(), v.getKanji(), v.getMeaning(), v.getExampleSentence()))
                .toList();

        var grammar = grammarPointRepository.findAllByLesson_IdOrderByTitleAsc(lessonId).stream()
                .map(g -> new GrammarPointDto(g.getId(), g.getTitle(), g.getExplanation(), g.getExampleSentence()))
                .toList();

        var examples = exampleSentenceRepository.findAllByLesson_IdOrderByJapaneseAsc(lessonId).stream()
                .map(e -> new ExampleSentenceDto(e.getId(), e.getJapanese(), e.getEnglish()))
                .toList();

        var quiz = new java.util.ArrayList<QuizQuestionDto>();
        quizQuestionRepository.findAllByLesson_IdOrderByPromptAsc(lessonId).stream()
                .map(q -> new QuizQuestionDto(q.getId(), q.getPrompt(), parseOptions(q.getOptionsJson())))
                .forEach(quiz::add);
        generatedQuizQuestionRepository.findAllByLesson_IdOrderByPromptAsc(lessonId).stream()
                .map(q -> new QuizQuestionDto(q.getId(), q.getPrompt(), parseOptions(q.getOptionsJson())))
                .forEach(quiz::add);

        return new LessonDetailDto(
                lesson.getId(),
                lesson.getLessonNumber(),
                lesson.getTitle(),
                lesson.getOverview(),
                vocab,
                grammar,
                examples,
                quiz
        );
    }

    public CheckAnswerResponse checkAnswer(Long lessonId, java.util.UUID questionId, int selectedIndex) {
        var question = quizQuestionRepository.findById(questionId)
                .<QuizAnswer>map(q -> new QuizAnswer(q.getLesson().getId(), q.getCorrectIndex(), q.getExplanation()))
                .orElseGet(() -> generatedQuizQuestionRepository.findById(questionId)
                        .map(q -> new QuizAnswer(q.getLesson().getId(), q.getCorrectIndex(), q.getExplanation()))
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Question not found")));

        if (!question.lessonId().equals(lessonId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Question does not belong to this lesson");
        }

        boolean correct = question.correctIndex() == selectedIndex;
        return new CheckAnswerResponse(correct, question.correctIndex(), question.explanation());
    }

    private record QuizAnswer(Long lessonId, int correctIndex, String explanation) {
    }

    private List<String> parseOptions(String optionsJson) {
        try {
            return objectMapper.readValue(optionsJson, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }
}
