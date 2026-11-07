package com.example.japaneseapp.lesson;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LessonExampleSentenceRepository extends JpaRepository<LessonExampleSentence, UUID> {
    List<LessonExampleSentence> findAllByLesson_IdOrderByJapaneseAsc(Long lessonId);
}
