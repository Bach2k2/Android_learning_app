package com.example.japaneseapp.lesson;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VocabularyRepository extends JpaRepository<Vocabulary, UUID> {
    List<Vocabulary> findAllByLesson_IdOrderByKanaAsc(Long lessonId);
}
