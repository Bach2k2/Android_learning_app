package com.example.japaneseapp.roleplay.dto;

import java.util.List;

public record CharacterDto(String id, String displayName, String description, List<String> supportedLevels) {
}
