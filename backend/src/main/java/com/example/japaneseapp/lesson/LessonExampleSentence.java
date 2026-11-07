package com.example.japaneseapp.lesson;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "lesson_example_sentence")
public class LessonExampleSentence {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "japanese", nullable = false)
    private String japanese;

    @Column(name = "english", nullable = false)
    private String english;

    protected LessonExampleSentence() {
    }

    public LessonExampleSentence(UUID id, Lesson lesson, String japanese, String english) {
        this.id = id;
        this.lesson = lesson;
        this.japanese = japanese;
        this.english = english;
    }

    public UUID getId() {
        return id;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public String getJapanese() {
        return japanese;
    }

    public String getEnglish() {
        return english;
    }
}
