package com.example.japaneseapp.progress;

import com.example.japaneseapp.auth.UserRepository;
import com.example.japaneseapp.common.ApiException;
import com.example.japaneseapp.lesson.LessonRepository;
import com.example.japaneseapp.progress.dto.ProgressDto;
import com.example.japaneseapp.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProgressService {

    private final UserProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;

    public ProgressService(UserProgressRepository progressRepository, UserRepository userRepository, LessonRepository lessonRepository) {
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
    }

    public List<ProgressDto> listMyProgress() {
        UUID userId = SecurityUtils.requireUserId();
        return progressRepository.findAllByUser_IdOrderByLessonLessonNumberAsc(userId).stream()
                .map(p -> new ProgressDto(p.getLesson().getId(), p.getStatus(), p.getLastViewedAt(), p.getQuizScore()))
                .toList();
    }

    @Transactional
    public ProgressDto upsertMyProgress(Long lessonId, ProgressStatus status, Integer quizScore) {
        UUID userId = SecurityUtils.requireUserId();

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));

        var lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Lesson not found"));

        var progress = progressRepository.findByUser_IdAndLesson_Id(userId, lessonId)
                .orElseGet(() -> new UserProgress(UUID.randomUUID(), user, lesson, ProgressStatus.NOT_STARTED, null, null));

        progress.update(status, quizScore);
        progressRepository.save(progress);

        return new ProgressDto(lessonId, progress.getStatus(), progress.getLastViewedAt(), progress.getQuizScore());
    }
}
