package com.example.japaneseapp.roleplay.dto;

import java.util.List;

public record MessageHistoryDto(List<MessageDto> items, String nextCursor) {
}
