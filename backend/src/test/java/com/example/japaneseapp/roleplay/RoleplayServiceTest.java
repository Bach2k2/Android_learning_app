package com.example.japaneseapp.roleplay;

import com.example.japaneseapp.ai.OllamaClient;
import com.example.japaneseapp.auth.AppUser;
import com.example.japaneseapp.auth.UserRepository;
import com.example.japaneseapp.common.ApiException;
import com.example.japaneseapp.roleplay.dto.CreateConversationRequest;
import com.example.japaneseapp.roleplay.dto.SendMessageRequest;
import com.example.japaneseapp.roleplay.dto.TranslationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RoleplayServiceTest {
    private static final Instant NOW = Instant.parse("2026-10-31T09:00:00Z");
    private static final UUID OWNER = UUID.randomUUID();
    private static final UUID OTHER = UUID.randomUUID();

    private RoleplayCharacterRepository characterRepository;
    private RoleplayScenarioRepository scenarioRepository;
    private LearningConversationRepository conversationRepository;
    private ConversationMessageRepository messageRepository;
    private UserRepository userRepository;
    private OllamaClient ollamaClient;
    private RoleplayService service;

    @BeforeEach
    void setUp() {
        characterRepository = mock(RoleplayCharacterRepository.class);
        scenarioRepository = mock(RoleplayScenarioRepository.class);
        conversationRepository = mock(LearningConversationRepository.class);
        messageRepository = mock(ConversationMessageRepository.class);
        userRepository = mock(UserRepository.class);
        ollamaClient = mock(OllamaClient.class);
        service = new RoleplayService(characterRepository, scenarioRepository, conversationRepository,
                messageRepository, userRepository, ollamaClient, new ObjectMapper(),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsConversationWithThirtyDayExpiry() {
        RoleplayCharacter character = mock(RoleplayCharacter.class);
        RoleplayScenario scenario = mock(RoleplayScenario.class);
        AppUser user = new AppUser(OWNER, "owner@example.test", "hash", NOW);
        when(characterRepository.findById("aiko-cafe-guide")).thenReturn(Optional.of(character));
        when(character.getSupportedLevelsJson()).thenReturn("[\"beginner\",\"elementary\"]");
        when(scenarioRepository.findByIdAndCharacter_Id("ordering-at-the-cafe", "aiko-cafe-guide"))
                .thenReturn(Optional.of(scenario));
        when(userRepository.findById(OWNER)).thenReturn(Optional.of(user));

        service.createConversation(OWNER, new CreateConversationRequest(
                "aiko-cafe-guide", "ordering-at-the-cafe", "beginner", "ja", "en"));

        ArgumentCaptor<LearningConversation> captor = ArgumentCaptor.forClass(LearningConversation.class);
        verify(conversationRepository).save(captor.capture());
        assertEquals(NOW, captor.getValue().getCreatedAt());
        assertEquals(NOW.plusSeconds(30 * 24 * 60 * 60L), captor.getValue().getExpiresAt());
        verify(conversationRepository).deleteAllByExpiresAtLessThanEqual(NOW);
    }

    @Test
    void ownerCanDeleteTranscriptImmediately() {
        LearningConversation conversation = conversation(OWNER);
        UUID id = conversation.getId();
        when(conversationRepository.findByIdAndUser_IdAndExpiresAtAfter(id, OWNER, NOW))
                .thenReturn(Optional.of(conversation));

        service.deleteConversation(OWNER, id);

        verify(conversationRepository).delete(conversation);
    }

    @Test
    void nonOwnerCannotReadOrDeleteConversationAndDoesNotLearnExistence() {
        UUID id = UUID.randomUUID();
        when(conversationRepository.findByIdAndUser_IdAndExpiresAtAfter(id, OTHER, NOW))
                .thenReturn(Optional.empty());

        ApiException historyError = assertThrows(ApiException.class, () -> service.getHistory(OTHER, id, null));
        ApiException deleteError = assertThrows(ApiException.class, () -> service.deleteConversation(OTHER, id));

        assertEquals(404, historyError.getStatus().value());
        assertEquals("Conversation not found", historyError.getMessage());
        assertEquals(404, deleteError.getStatus().value());
        verify(conversationRepository, never()).delete(any(LearningConversation.class));
        verifyNoInteractions(ollamaClient);
    }

    @Test
    void expiredConversationIsUnavailableAndCleanupDeletesIt() {
        UUID id = UUID.randomUUID();
        when(conversationRepository.findByIdAndUser_IdAndExpiresAtAfter(id, OWNER, NOW))
                .thenReturn(Optional.empty());

        ApiException error = assertThrows(ApiException.class, () -> service.getHistory(OWNER, id, null));

        assertEquals(404, error.getStatus().value());
        verify(conversationRepository).findByIdAndUser_IdAndExpiresAtAfter(id, OWNER, NOW);
    }

    @Test
    void malformedTutorOutputDoesNotPersistMessages() {
        LearningConversation conversation = conversation(OWNER);
        when(conversationRepository.findByIdAndUser_IdAndExpiresAtAfter(
                conversation.getId(), OWNER, NOW)).thenReturn(Optional.of(conversation));
        when(messageRepository.findFirst51ByConversation_IdOrderByCreatedAtDescIdDesc(conversation.getId()))
                .thenReturn(java.util.List.of());
        when(messageRepository.findByConversation_IdAndClientMessageIdAndRole(
                any(), any(), anyString())).thenReturn(Optional.empty());
        when(ollamaClient.generate(anyString())).thenReturn("{not-json");

        ApiException error = assertThrows(ApiException.class, () -> service.sendMessage(
                OWNER, conversation.getId(), new SendMessageRequest(UUID.randomUUID(), "こんにちは")));

        assertEquals(502, error.getStatus().value());
        verify(messageRepository, never()).saveAll(anyList());
    }

    @Test
    void validRoleplayResponsePersistsUserAndTutorMessages() {
        LearningConversation conversation = conversation(OWNER);
        UUID clientMessageId = UUID.randomUUID();
        when(conversationRepository.findByIdAndUser_IdAndExpiresAtAfter(
                conversation.getId(), OWNER, NOW)).thenReturn(Optional.of(conversation));
        when(messageRepository.findByConversation_IdAndClientMessageIdAndRole(
                conversation.getId(), clientMessageId, "USER")).thenReturn(Optional.empty());
        when(messageRepository.findByConversation_IdAndClientMessageIdAndRole(
                conversation.getId(), clientMessageId, "TUTOR")).thenReturn(Optional.empty());
        when(messageRepository.findFirst51ByConversation_IdOrderByCreatedAtDescIdDesc(conversation.getId()))
                .thenReturn(java.util.List.of());
        when(ollamaClient.generate(anyString())).thenReturn(
                "{\"text\":\"こんにちは。\",\"reading\":\"こんにちは。\",\"translation\":\"Hello.\"}");

        var result = service.sendMessage(conversation.getUser().getId(), conversation.getId(),
                new SendMessageRequest(clientMessageId, "こんにちは"));

        assertEquals("USER", result.userMessage().role());
        assertEquals("こんにちは。", result.tutorMessage().text());
        assertEquals("Hello.", result.tutorMessage().translation());
        verify(messageRepository).saveAll(argThat(messages -> ((java.util.Collection<?>) messages).size() == 2));
    }

    @Test
    void suggestionsAreValidatedAndReturnedWithoutPersistingMessages() {
        LearningConversation conversation = conversation(OWNER);
        when(conversationRepository.findByIdAndUser_IdAndExpiresAtAfter(
                conversation.getId(), OWNER, NOW)).thenReturn(Optional.of(conversation));
        when(messageRepository.findFirst51ByConversation_IdOrderByCreatedAtDescIdDesc(conversation.getId()))
                .thenReturn(java.util.List.of());
        when(ollamaClient.generate(anyString())).thenReturn(
                "{\"suggestions\":[{\"text\":\"はい、お願いします。\"},{\"text\":\"ありがとう。\"}]}");

        var result = service.getSuggestions(OWNER, conversation.getId(), 2);

        assertEquals(2, result.suggestions().size());
        verify(messageRepository, never()).saveAll(anyList());
    }

    @Test
    void translationsSupportEnglishAndVietnameseOnly() {
        when(ollamaClient.generate(contains("English"))).thenReturn("{\"translatedText\":\"Hello\"}");
        when(ollamaClient.generate(contains("Vietnamese"))).thenReturn("{\"translatedText\":\"Xin chào\"}");

        var english = service.translate(new TranslationRequest("こんにちは", "ja", "en", null));
        var vietnamese = service.translate(new TranslationRequest("こんにちは", "ja", "vi", null));

        assertEquals("Hello", english.translatedText());
        assertEquals("en", english.targetLanguage());
        assertEquals("Xin chào", vietnamese.translatedText());
        assertEquals("vi", vietnamese.targetLanguage());
        verify(ollamaClient).generate(contains("Translate the Japanese text below into English"));
        verify(ollamaClient).generate(contains("Translate the Japanese text below into Vietnamese"));
    }

    private LearningConversation conversation(UUID ownerId) {
        RoleplayCharacter character = mock(RoleplayCharacter.class);
        RoleplayScenario scenario = mock(RoleplayScenario.class);
        AppUser owner = new AppUser(ownerId, "owner@example.test", "hash", NOW);
        return new LearningConversation(UUID.randomUUID(), owner, character, scenario,
                "beginner", "ja", "en", NOW, NOW.plusSeconds(30 * 24 * 60 * 60L));
    }
}
