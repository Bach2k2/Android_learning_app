package com.example.japaneseapp.lesson;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "vocabulary")
public class Vocabulary {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "kana", nullable = false)
    private String kana;

    @Column(name = "kanji")
    private String kanji;

    @Column(name = "meaning", nullable = false)
    private String meaning;

    @Column(name = "example_sentence")
    private String exampleSentence;

    protected Vocabulary() {
    }

    public Vocabulary(UUID id, Lesson lesson, String kana, String kanji, String meaning, String exampleSentence) {
        this.id = id;
        this.lesson = lesson;
        this.kana = kana;
        this.kanji = kanji;
        this.meaning = meaning;
        this.exampleSentence = exampleSentence;
    }

    public UUID getId() {
        return id;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public String getKana() {
        return kana;
    }

    public String getKanji() {
        return kanji;
    }

    public String getMeaning() {
        return meaning;
    }

    public String getExampleSentence() {
        return exampleSentence;
    }
}
