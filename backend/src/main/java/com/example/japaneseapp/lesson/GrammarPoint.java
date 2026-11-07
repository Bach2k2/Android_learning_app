package com.example.japaneseapp.lesson;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "grammar_point")
public class GrammarPoint {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "explanation", nullable = false)
    private String explanation;

    @Column(name = "example_sentence")
    private String exampleSentence;

    protected GrammarPoint() {
    }

    public GrammarPoint(UUID id, Lesson lesson, String title, String explanation, String exampleSentence) {
        this.id = id;
        this.lesson = lesson;
        this.title = title;
        this.explanation = explanation;
        this.exampleSentence = exampleSentence;
    }

    public UUID getId() {
        return id;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public String getTitle() {
        return title;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getExampleSentence() {
        return exampleSentence;
    }
}
