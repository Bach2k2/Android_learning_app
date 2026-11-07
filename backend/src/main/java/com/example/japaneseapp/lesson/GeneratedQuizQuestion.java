package com.example.japaneseapp.lesson;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "generated_quiz_question")
public class GeneratedQuizQuestion {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "prompt", nullable = false)
    private String prompt;

    @Column(name = "options_json", nullable = false)
    private String optionsJson;

    @Column(name = "correct_index", nullable = false)
    private int correctIndex;

    @Column(name = "explanation")
    private String explanation;

    protected GeneratedQuizQuestion() {
    }

    public GeneratedQuizQuestion(UUID id, Lesson lesson, String prompt, String optionsJson, int correctIndex, String explanation) {
        this.id = id;
        this.lesson = lesson;
        this.prompt = prompt;
        this.optionsJson = optionsJson;
        this.correctIndex = correctIndex;
        this.explanation = explanation;
    }

    public UUID getId() {
        return id;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getOptionsJson() {
        return optionsJson;
    }

    public int getCorrectIndex() {
        return correctIndex;
    }

    public String getExplanation() {
        return explanation;
    }
}
