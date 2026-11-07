package com.example.japaneseapp.roleplay;

import com.example.japaneseapp.roleplay.dto.*;
import com.example.japaneseapp.security.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Validated
public class RoleplayController {
    private final RoleplayService roleplayService;

    public RoleplayController(RoleplayService roleplayService) {
        this.roleplayService = roleplayService;
    }

    @GetMapping("/characters")
    public CharacterCatalogDto characters(
            @RequestParam(defaultValue = "ja") @Pattern(regexp = "ja") String language
    ) {
        SecurityUtils.requireUserId();
        return new CharacterCatalogDto(roleplayService.listCharacters());
    }

    @GetMapping("/scenarios")
    public ScenarioCatalogDto scenarios(@RequestParam @NotBlank @Size(max = 64) String characterId) {
        SecurityUtils.requireUserId();
        return new ScenarioCatalogDto(roleplayService.listScenarios(characterId));
    }

    @PostMapping("/conversations")
    public ConversationDto createConversation(@Valid @RequestBody CreateConversationRequest request) {
        return roleplayService.createConversation(SecurityUtils.requireUserId(), request);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public MessageHistoryDto history(
            @PathVariable UUID conversationId,
            @RequestParam(required = false) @Size(max = 256) String cursor
    ) {
        return roleplayService.getHistory(SecurityUtils.requireUserId(), conversationId, cursor);
    }

    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable UUID conversationId) {
        roleplayService.deleteConversation(SecurityUtils.requireUserId(), conversationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public MessagePairDto sendMessage(
            @PathVariable UUID conversationId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        return roleplayService.sendMessage(SecurityUtils.requireUserId(), conversationId, request);
    }

    @PostMapping("/conversations/{conversationId}/suggestions")
    public SuggestionsResponse suggestions(
            @PathVariable UUID conversationId,
            @Valid @RequestBody SuggestionsRequest request
    ) {
        return roleplayService.getSuggestions(
                SecurityUtils.requireUserId(), conversationId, request.count());
    }

    @PostMapping("/translations")
    public TranslationResponse translate(@Valid @RequestBody TranslationRequest request) {
        SecurityUtils.requireUserId();
        return roleplayService.translate(request);
    }
}
