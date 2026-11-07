package com.example.japaneseapp.progress;

import com.example.japaneseapp.auth.AppUser;
import com.example.japaneseapp.lesson.Lesson;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_progress", uniqueConstraints = {
        @UniqueConstraint(name = "uk_progress_user_lesson", columnNames = {"user_id", "lesson_id"})
})
public class UserProgress {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProgressStatus status;

    @Column(name = "last_viewed_at")
    private Instant lastViewedAt;

    @Column(name = "quiz_score")
    private Integer quizScore;

    protected UserProgress() {
    }

    public UserProgress(UUID id, AppUser user, Lesson lesson, ProgressStatus status, Instant lastViewedAt, Integer quizScore) {
        this.id = id;
        this.user = user;
        this.lesson = lesson;
        this.status = status;
        this.lastViewedAt = lastViewedAt;
        this.quizScore = quizScore;
    }

    public UUID getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public ProgressStatus getStatus() {
        return status;
    }

    public Instant getLastViewedAt() {
        return lastViewedAt;
    }

    public Integer getQuizScore() {
        return quizScore;
    }

    public void update(ProgressStatus status, Integer quizScore) {
        if (status != null) {
            this.status = status;
        }
        if (quizScore != null) {
            this.quizScore = quizScore;
        }
        this.lastViewedAt = Instant.now();
    }
}
