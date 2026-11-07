package com.example.japaneseapp.lesson.dto;

import java.util.UUID;

public record GrammarPointDto(
        UUID id,
        String title,
        String explanation,
        String exampleSentence
) {
}
