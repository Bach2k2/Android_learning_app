package com.example.japaneseapp.lesson.dto;

import java.util.UUID;

public record ExampleSentenceDto(
        UUID id,
        String japanese,
        String english
) {
}
