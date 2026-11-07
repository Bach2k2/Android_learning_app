package com.example.japaneseapp.android.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): AuthResponse

    @POST("auth/logout")
    suspend fun logout(@Body body: LogoutRequest)

    @GET("lessons")
    suspend fun listLessons(): List<LessonSummaryDto>

    @GET("lessons/{lessonId}")
    suspend fun getLesson(@Path("lessonId") lessonId: Long): LessonDetailDto

    @POST("lessons/{lessonId}/quiz/{questionId}/check")
    suspend fun checkAnswer(
        @Path("lessonId") lessonId: Long,
        @Path("questionId") questionId: String,
        @Body body: CheckAnswerRequest,
    ): CheckAnswerResponse

    @GET("progress")
    suspend fun listProgress(): List<ProgressDto>

    @PUT("progress/{lessonId}")
    suspend fun upsertProgress(
        @Path("lessonId") lessonId: Long,
        @Body body: UpdateProgressRequest,
    ): ProgressDto

    @POST("ai/lessons/{lessonId}/generate-quiz")
    suspend fun generateQuiz(
        @Path("lessonId") lessonId: Long,
        @Query("count") count: Int = 5,
    ): GenerateQuizResponse

    @POST("ai/grammar/explain")
    suspend fun explainGrammar(@Body body: ExplainRequest): ExplainResponse

    @POST("ai/vocab/review")
    suspend fun reviewVocab(@Body body: ExplainRequest): ExplainResponse

    @GET("v1/characters")
    suspend fun listCharacters(@Query("language") language: String = "ja"): CharacterCatalogResponse

    @GET("v1/scenarios")
    suspend fun listScenarios(@Query("characterId") characterId: String): ScenarioCatalogResponse

    @POST("v1/conversations")
    suspend fun createConversation(@Body body: CreateConversationRequest): CreateConversationResponse

    @GET("v1/conversations/{conversationId}/messages")
    suspend fun listConversationMessages(
        @Path("conversationId") conversationId: String,
        @Query("cursor") cursor: String? = null,
    ): ConversationMessagesResponse

    @DELETE("v1/conversations/{conversationId}")
    suspend fun deleteConversation(@Path("conversationId") conversationId: String)

    @POST("v1/conversations/{conversationId}/messages")
    suspend fun sendConversationMessage(
        @Path("conversationId") conversationId: String,
        @Body body: SendMessageRequest,
    ): SendMessageResponse

    @POST("v1/conversations/{conversationId}/suggestions")
    suspend fun getSuggestions(
        @Path("conversationId") conversationId: String,
        @Body body: SuggestionsRequest,
    ): SuggestionsResponse

    @POST("v1/translations")
    suspend fun translate(@Body body: TranslationRequest): TranslationResponse
}
