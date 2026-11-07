package com.example.japaneseapp.lesson;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, UUID> {
    List<QuizQuestion> findAllByLesson_IdOrderByPromptAsc(Long lessonId);
}
