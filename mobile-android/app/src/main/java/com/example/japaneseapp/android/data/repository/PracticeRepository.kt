package com.example.japaneseapp.android.data.repository

import com.example.japaneseapp.android.data.api.ApiClient
import com.example.japaneseapp.android.data.api.CreateConversationRequest
import com.example.japaneseapp.android.data.api.SendMessageRequest
import com.example.japaneseapp.android.data.api.SuggestionsRequest
import com.example.japaneseapp.android.data.api.TranslationRequest
import com.example.japaneseapp.android.data.token.TokenStore

class PracticeRepository(private val tokenStore: TokenStore) {
    private val api = ApiClient.create(tokenStore)

    suspend fun characters() = api.listCharacters()
    suspend fun scenarios(characterId: String) = api.listScenarios(characterId)

    suspend fun createConversation(
        characterId: String,
        scenarioId: String,
        level: String,
        translationLanguage: String,
    ) = api.createConversation(
        CreateConversationRequest(
            characterId = characterId,
            scenarioId = scenarioId,
            level = level,
            translationLanguage = translationLanguage,
        )
    )

    suspend fun messages(conversationId: String) =
        api.listConversationMessages(conversationId)

    suspend fun deleteConversation(conversationId: String) =
        api.deleteConversation(conversationId)

    suspend fun sendMessage(conversationId: String, clientMessageId: String, text: String) =
        api.sendConversationMessage(
            conversationId,
            SendMessageRequest(clientMessageId = clientMessageId, text = text)
        )

    suspend fun suggestions(conversationId: String, count: Int) =
        api.getSuggestions(conversationId, SuggestionsRequest(count))

    suspend fun translate(text: String, targetLanguage: String, context: String?) =
        api.translate(
            TranslationRequest(text = text, targetLanguage = targetLanguage, context = context)
        )
}
