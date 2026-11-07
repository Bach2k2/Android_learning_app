package com.example.japaneseapp.lesson.dto;

import java.util.UUID;

public record VocabularyDto(
        UUID id,
        String kana,
        String kanji,
        String meaning,
        String exampleSentence
) {
}
