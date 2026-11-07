package com.example.japaneseapp.lesson;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GrammarPointRepository extends JpaRepository<GrammarPoint, UUID> {
    List<GrammarPoint> findAllByLesson_IdOrderByTitleAsc(Long lessonId);
}
