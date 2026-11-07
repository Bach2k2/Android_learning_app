package com.example.japaneseapp.lesson;

import com.example.japaneseapp.lesson.dto.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @GetMapping
    public List<LessonSummaryDto> list() {
        return lessonService.listLessons();
    }

    @GetMapping("/{lessonId}")
    public LessonDetailDto get(@PathVariable Long lessonId) {
        return lessonService.getLesson(lessonId);
    }

    @PostMapping("/{lessonId}/quiz/{questionId}/check")
    public CheckAnswerResponse check(
            @PathVariable Long lessonId,
            @PathVariable UUID questionId,
            @Valid @RequestBody CheckAnswerRequest request
    ) {
        return lessonService.checkAnswer(lessonId, questionId, request.selectedIndex());
    }
}
