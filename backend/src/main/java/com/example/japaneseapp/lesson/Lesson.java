package com.example.japaneseapp.lesson;

import jakarta.persistence.*;

@Entity
@Table(name = "lesson")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lesson_number", nullable = false)
    private int lessonNumber;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "overview", nullable = false)
    private String overview;

    protected Lesson() {
    }

    public Lesson(int lessonNumber, String title, String overview) {
        this.lessonNumber = lessonNumber;
        this.title = title;
        this.overview = overview;
    }

    public Long getId() {
        return id;
    }

    public int getLessonNumber() {
        return lessonNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }
}
