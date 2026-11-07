package com.example.japaneseapp.progress;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserProgressRepository extends JpaRepository<UserProgress, UUID> {
    List<UserProgress> findAllByUser_IdOrderByLessonLessonNumberAsc(UUID userId);
    Optional<UserProgress> findByUser_IdAndLesson_Id(UUID userId, Long lessonId);
}
