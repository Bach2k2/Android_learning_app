package com.example.japaneseapp.android.data.api

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val email: String,
    val password: String,
)

data class LoginRequest(
    val email: String,
    val password: String,
)

data class RefreshRequest(
    val refreshToken: String,
)

data class LogoutRequest(
    val refreshToken: String,
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
)

data class LessonSummaryDto(
    val id: Long,
    val lessonNumber: Int,
    val title: String,
    val overview: String,
)

data class VocabularyDto(
    val id: String,
    val kana: String,
    val kanji: String?,
    val meaning: String,
    val exampleSentence: String?,
)

data class GrammarPointDto(
    val id: String,
    val title: String,
    val explanation: String,
    val exampleSentence: String?,
)

data class ExampleSentenceDto(
    val id: String,
    val japanese: String,
    val english: String,
)

data class QuizQuestionDto(
    val id: String,
    val prompt: String,
    val options: List<String>,
)

data class LessonDetailDto(
    val id: Long,
    val lessonNumber: Int,
    val title: String,
    val overview: String,
    val vocabulary: List<VocabularyDto>,
    val grammarPoints: List<GrammarPointDto>,
    val exampleSentences: List<ExampleSentenceDto>,
    val quiz: List<QuizQuestionDto>,
)

data class CheckAnswerRequest(
    val selectedIndex: Int,
)

data class CheckAnswerResponse(
    val correct: Boolean,
    val correctIndex: Int,
    val explanation: String?,
)

enum class ProgressStatus {
    @SerializedName("NOT_STARTED") NOT_STARTED,
    @SerializedName("IN_PROGRESS") IN_PROGRESS,
    @SerializedName("COMPLETED") COMPLETED,
}

data class ProgressDto(
    val lessonId: Long,
    val status: ProgressStatus,
    val lastViewedAt: String?,
    val quizScore: Int?,
)

data class UpdateProgressRequest(
    val status: ProgressStatus?,
    val quizScore: Int?,
)

data class GenerateQuizResponse(
    val lessonId: Long,
    val questions: List<QuizQuestionDto>,
)

data class ExplainRequest(
    val text: String,
)

data class ExplainResponse(
    val explanation: String,
)

data class CharacterCatalogResponse(val items: List<CharacterDto>)

data class CharacterDto(
    val id: String,
    val displayName: String,
    val description: String,
    val supportedLevels: List<String>,
)

data class ScenarioCatalogResponse(val items: List<ScenarioDto>)

data class ScenarioDto(
    val id: String,
    val title: String,
    val description: String,
)

data class CreateConversationRequest(
    val characterId: String,
    val scenarioId: String,
    val level: String,
    val targetLanguage: String = "ja",
    val translationLanguage: String,
)

data class CreateConversationResponse(
    val conversationId: String,
    val createdAt: String? = null,
)

data class ConversationMessagesResponse(
    val items: List<ConversationMessageDto>,
    val nextCursor: String? = null,
)

data class ConversationMessageDto(
    val id: String,
    val role: String,
    val text: String,
    val translation: String? = null,
    val reading: String? = null,
    val createdAt: String? = null,
)

data class SendMessageRequest(
    val clientMessageId: String,
    val text: String,
)

data class SendMessageResponse(
    val userMessage: ConversationMessageDto,
    val tutorMessage: ConversationMessageDto,
)

data class SuggestionsRequest(val count: Int)

data class SuggestionsResponse(val suggestions: List<SuggestionDto>)

data class SuggestionDto(val text: String)

data class TranslationRequest(
    val text: String,
    val sourceLanguage: String = "ja",
    val targetLanguage: String,
    val context: String? = null,
)

data class TranslationResponse(
    val translatedText: String,
    val targetLanguage: String,
)
