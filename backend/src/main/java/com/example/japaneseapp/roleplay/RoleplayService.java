package com.example.japaneseapp.roleplay;

import com.example.japaneseapp.ai.OllamaClient;
import com.example.japaneseapp.auth.UserRepository;
import com.example.japaneseapp.common.ApiException;
import com.example.japaneseapp.roleplay.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class RoleplayService {
    private static final Duration TRANSCRIPT_RETENTION = Duration.ofDays(30);
    private static final int HISTORY_PAGE_SIZE = 50;

    private final RoleplayCharacterRepository characterRepository;
    private final RoleplayScenarioRepository scenarioRepository;
    private final LearningConversationRepository conversationRepository;
    private final ConversationMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final OllamaClient ollamaClient;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public RoleplayService(RoleplayCharacterRepository characterRepository,
                           RoleplayScenarioRepository scenarioRepository,
                           LearningConversationRepository conversationRepository,
                           ConversationMessageRepository messageRepository,
                           UserRepository userRepository,
                           OllamaClient ollamaClient,
                           ObjectMapper objectMapper,
                           Clock clock) {
        this.characterRepository = characterRepository;
        this.scenarioRepository = scenarioRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.ollamaClient = ollamaClient;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<CharacterDto> listCharacters() {
        return characterRepository.findAll().stream()
                .sorted(Comparator.comparing(RoleplayCharacter::getDisplayName))
                .map(character -> new CharacterDto(
                        character.getId(),
                        character.getDisplayName(),
                        character.getDescription(),
                        parseLevels(character.getSupportedLevelsJson())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ScenarioDto> listScenarios(String characterId) {
        if (!characterRepository.existsById(characterId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Character not found");
        }
        return scenarioRepository.findAllByCharacter_IdOrderByTitleAsc(characterId).stream()
                .map(scenario -> new ScenarioDto(
                        scenario.getId(), characterId, scenario.getTitle(), scenario.getDescription()))
                .toList();
    }

    @Transactional
    public ConversationDto createConversation(UUID userId, CreateConversationRequest request) {
        Instant now = clock.instant();
        conversationRepository.deleteAllByExpiresAtLessThanEqual(now);

        RoleplayCharacter character = characterRepository.findById(request.characterId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Character not found"));
        RoleplayScenario scenario = scenarioRepository.findByIdAndCharacter_Id(
                        request.scenarioId(), request.characterId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Scenario not found"));
        if (!parseLevels(character.getSupportedLevelsJson()).contains(request.level())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Level is not supported by this character");
        }

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
        UUID id = UUID.randomUUID();
        conversationRepository.save(new LearningConversation(
                id, user, character, scenario, request.level(), request.targetLanguage(),
                request.translationLanguage(), now, now.plus(TRANSCRIPT_RETENTION)));
        return new ConversationDto(id, now);
    }

    @Transactional(readOnly = true)
    public MessageHistoryDto getHistory(UUID userId, UUID conversationId, String cursor) {
        LearningConversation conversation = requireOwnedConversation(userId, conversationId);
        if (cursor != null && cursor.length() > 256) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid history cursor");
        }
        List<ConversationMessage> descending;
        if (cursor == null || cursor.isBlank()) {
            descending = messageRepository.findFirst51ByConversation_IdOrderByCreatedAtDescIdDesc(conversation.getId());
        } else {
            Cursor decoded = decodeCursor(cursor);
            descending = messageRepository.findHistoryBefore(
                    conversation.getId(), decoded.createdAt(), decoded.messageId(), PageRequest.of(0, HISTORY_PAGE_SIZE + 1));
        }
        boolean hasMore = descending.size() > HISTORY_PAGE_SIZE;
        List<ConversationMessage> page = new ArrayList<>(
                descending.subList(0, Math.min(HISTORY_PAGE_SIZE, descending.size())));
        String nextCursor = hasMore && !page.isEmpty() ? encodeCursor(page.get(page.size() - 1)) : null;
        Collections.reverse(page);
        return new MessageHistoryDto(page.stream().map(this::toDto).toList(), nextCursor);
    }

    @Transactional
    public void deleteConversation(UUID userId, UUID conversationId) {
        conversationRepository.deleteAllByExpiresAtLessThanEqual(clock.instant());
        LearningConversation conversation = requireOwnedConversation(userId, conversationId);
        conversationRepository.delete(conversation);
    }

    @Transactional
    public MessagePairDto sendMessage(UUID userId, UUID conversationId, SendMessageRequest request) {
        conversationRepository.deleteAllByExpiresAtLessThanEqual(clock.instant());
        LearningConversation conversation = requireOwnedConversation(userId, conversationId);
        Optional<ConversationMessage> priorUser = messageRepository
                .findByConversation_IdAndClientMessageIdAndRole(conversationId, request.clientMessageId(), "USER");
        if (priorUser.isPresent()) {
            ConversationMessage priorTutor = messageRepository
                    .findByConversation_IdAndClientMessageIdAndRole(conversationId, request.clientMessageId(), "TUTOR")
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_GATEWAY, "The prior response is unavailable"));
            return new MessagePairDto(toDto(priorUser.get()), toDto(priorTutor));
        }

        String prompt = buildRoleplayPrompt(conversation, request.text());
        JsonNode response = extractJson(ollamaClient.generate(prompt));
        String japanese = textField(response, "text", 1500, true);
        String reading = textField(response, "reading", 1500, false);
        String translation = textField(response, "translation", 2000, true);

        Instant now = clock.instant();
        ConversationMessage userMessage = new ConversationMessage(
                UUID.randomUUID(), conversation, "USER", request.text().trim(), null, null,
                request.clientMessageId(), now);
        ConversationMessage tutorMessage = new ConversationMessage(
                UUID.randomUUID(), conversation, "TUTOR", japanese, translation, reading,
                request.clientMessageId(), now.plusNanos(1));
        messageRepository.saveAll(List.of(userMessage, tutorMessage));
        return new MessagePairDto(toDto(userMessage), toDto(tutorMessage));
    }

    @Transactional(readOnly = true)
    public SuggestionsResponse getSuggestions(UUID userId, UUID conversationId, int count) {
        if (count < 1 || count > 5) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "count must be between 1 and 5");
        }
        LearningConversation conversation = requireOwnedConversation(userId, conversationId);
        String prompt = "Return only JSON {\"suggestions\":[{\"text\":\"...\"}]}. Generate exactly " + count
                + " distinct short Japanese replies suitable for the following roleplay. Use "
                + conversation.getLevel() + " level Japanese, and do not send the suggestions. "
                + "Scenario: " + conversation.getScenario().getTitle() + ". Recent conversation:\n"
                + recentTranscript(conversationId);
        if (prompt.length() > 9000) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Conversation context exceeded the AI prompt limit");
        }
        JsonNode root = extractJson(ollamaClient.generate(prompt));
        JsonNode values = root.path("suggestions");
        if (!values.isArray() || values.size() != count) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "AI response did not contain the requested suggestions");
        }
        List<SuggestionDto> suggestions = new ArrayList<>();
        Set<String> unique = new HashSet<>();
        for (JsonNode value : values) {
            String text = textField(value, "text", 300, true);
            if (!unique.add(text)) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "AI response contained duplicate suggestions");
            }
            suggestions.add(new SuggestionDto(text));
        }
        return new SuggestionsResponse(suggestions);
    }

    public TranslationResponse translate(TranslationRequest request) {
        String prompt = "Translate the Japanese text below into "
                + (request.targetLanguage().equals("en") ? "English" : "Vietnamese")
                + ". Return only JSON {\"translatedText\":\"...\"}. Preserve meaning and do not add commentary."
                + (request.context() == null || request.context().isBlank() ? "" : "\nContext: " + request.context())
                + "\nJapanese text:\n" + request.text();
        if (prompt.length() > 5000) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Translation input is too long");
        }
        JsonNode root = extractJson(ollamaClient.generate(prompt));
        String translated = textField(root, "translatedText", 2000, true);
        return new TranslationResponse(translated, request.targetLanguage());
    }

    private LearningConversation requireOwnedConversation(UUID userId, UUID conversationId) {
        Instant now = clock.instant();
        return conversationRepository.findByIdAndUser_IdAndExpiresAtAfter(conversationId, userId, now)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Conversation not found"));
    }

    private String buildRoleplayPrompt(LearningConversation conversation, String userText) {
        String prompt = "You are a supportive Japanese tutor roleplaying as "
                + conversation.getCharacter().getDisplayName() + ". Scenario: "
                + conversation.getScenario().getTitle() + " — " + conversation.getScenario().getDescription()
                + ". Learner level: " + conversation.getLevel() + ". Respond naturally in Japanese in 1-3 short sentences. "
                + "Return only JSON with keys text (Japanese response), reading (kana reading, may be empty), and translation "
                + "(" + (conversation.getTranslationLanguage().equals("en") ? "English" : "Vietnamese") + ")."
                + "\nRecent conversation:\n" + recentTranscript(conversation.getId())
                + "\nLearner message:\n" + userText;
        if (prompt.length() > 10000) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Conversation context exceeded the prompt limit");
        }
        return prompt;
    }

    private String recentTranscript(UUID conversationId) {
        List<ConversationMessage> recent = messageRepository
                .findFirst51ByConversation_IdOrderByCreatedAtDescIdDesc(conversationId);
        Collections.reverse(recent);
        StringBuilder transcript = new StringBuilder();
        int start = Math.max(0, recent.size() - 12);
        for (int i = start; i < recent.size(); i++) {
            ConversationMessage message = recent.get(i);
            String line = message.getRole() + ": " + message.getText();
            if (transcript.length() + line.length() > 7000) {
                break;
            }
            transcript.append(line).append('\n');
        }
        return transcript.toString();
    }

    private List<String> parseLevels(String levelsJson) {
        try {
            return objectMapper.readValue(levelsJson, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, String.class));
        } catch (Exception exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Roleplay catalog is unavailable");
        }
    }

    private String textField(JsonNode node, String field, int maxLength, boolean required) {
        JsonNode value = node.get(field);
        String text = value != null && value.isTextual() ? value.asText().trim() : "";
        if ((required && text.isEmpty()) || text.length() > maxLength) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "AI response did not match the expected format");
        }
        return text.isEmpty() ? null : text;
    }

    private JsonNode extractJson(String response) {
        if (response == null || response.isBlank() || response.length() > 12000) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "AI response did not match the expected format");
        }
        try {
            JsonNode root = objectMapper.readTree(response);
            if (root != null && root.isObject()) {
                return root;
            }
        } catch (Exception ignored) {
        }
        int first = response.indexOf('{');
        int last = response.lastIndexOf('}');
        if (first >= 0 && last > first) {
            try {
                JsonNode root = objectMapper.readTree(response.substring(first, last + 1));
                if (root != null && root.isObject()) {
                    return root;
                }
            } catch (Exception ignored) {
            }
        }
        throw new ApiException(HttpStatus.BAD_GATEWAY, "AI response did not match the expected format");
    }

    private MessageDto toDto(ConversationMessage message) {
        return new MessageDto(message.getId(), message.getRole(), message.getText(),
                message.getTranslation(), message.getReading(), message.getCreatedAt());
    }

    private String encodeCursor(ConversationMessage message) {
        String value = message.getCreatedAt() + "|" + message.getId();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private Cursor decodeCursor(String cursor) {
        try {
            String value = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = value.split("\\|", -1);
            return new Cursor(Instant.parse(parts[0]), UUID.fromString(parts[1]));
        } catch (Exception exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid history cursor");
        }
    }

    private record Cursor(Instant createdAt, UUID messageId) {
    }
}
