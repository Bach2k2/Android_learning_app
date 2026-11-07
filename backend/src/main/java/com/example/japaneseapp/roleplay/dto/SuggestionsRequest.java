package com.example.japaneseapp.roleplay.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SuggestionsRequest(@Min(1) @Max(5) int count) {
}
