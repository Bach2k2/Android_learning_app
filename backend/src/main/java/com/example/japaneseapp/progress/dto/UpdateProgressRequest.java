package com.example.japaneseapp.progress.dto;

import com.example.japaneseapp.progress.ProgressStatus;

public record UpdateProgressRequest(
        ProgressStatus status,
        Integer quizScore
) {
}
