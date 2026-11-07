package com.example.japaneseapp.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.japaneseapp.android.data.api.CharacterDto
import com.example.japaneseapp.android.data.api.ConversationMessageDto
import com.example.japaneseapp.android.data.api.ScenarioDto
import com.example.japaneseapp.android.data.repository.PracticeRepository
import com.example.japaneseapp.android.data.token.TokenStore
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PracticeMessage(
    val id: String,
    val role: String,
    val japanese: String,
    val reading: String?,
    val translations: Map<String, String> = emptyMap(),
)

data class PracticeUiState(
    val language: String = "en",
    val loadingCharacters: Boolean = false,
    val loadingScenarios: Boolean = false,
    val creatingConversation: Boolean = false,
    val loadingMessages: Boolean = false,
    val loadingSuggestions: Boolean = false,
    val sending: Boolean = false,
    val deleting: Boolean = false,
    val characters: List<CharacterDto> = emptyList(),
    val scenarios: List<ScenarioDto> = emptyList(),
    val selectedCharacterId: String? = null,
    val selectedScenarioId: String? = null,
    val level: String = "N5",
    val conversationId: String? = null,
    val messages: List<PracticeMessage> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val draft: String = "",
    val error: String? = null,
    val retryTarget: RetryTarget? = null,
    val deleteConfirmationVisible: Boolean = false,
    val deleteFailed: Boolean = false,
)

enum class RetryTarget { CHARACTERS, SCENARIOS, MESSAGES, SUGGESTIONS, SEND, DELETE }

class PracticeViewModel(
    private val repository: PracticeRepository,
    private val tokenStore: TokenStore,
) : ViewModel() {
    private val _state = MutableStateFlow(PracticeUiState())
    val state: StateFlow<PracticeUiState> = _state.asStateFlow()

    private var sendJob: Job? = null
    private var suggestionJob: Job? = null
    private var scenarioJob: Job? = null
    private var messageClientId: String? = null
    private var messageClientText: String? = null
    private var onDeleteComplete: (() -> Unit)? = null

    init {
        viewModelScope.launch {
            tokenStore.translationLanguage.collect { language ->
                _state.value = _state.value.copy(language = language)
                translateMissingMessages(language)
            }
        }
        loadCharacters()
    }

    fun loadCharacters() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loadingCharacters = true, error = null, retryTarget = null)
            try {
                val characters = repository.characters().items
                _state.value = _state.value.copy(
                    loadingCharacters = false,
                    characters = characters,
                    selectedCharacterId = _state.value.selectedCharacterId
                        ?.takeIf { id -> characters.any { it.id == id } }
                        ?: characters.firstOrNull()?.id,
                )
                _state.value.selectedCharacterId?.let(::selectCharacter)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loadingCharacters = false,
                    error = e.message ?: "Could not load characters",
                    retryTarget = RetryTarget.CHARACTERS,
                )
            }
        }
    }

    fun selectCharacter(characterId: String) {
        scenarioJob?.cancel()
        val supportedLevels = _state.value.characters
            .firstOrNull { it.id == characterId }
            ?.supportedLevels
            .orEmpty()
        val nextLevel = when {
            supportedLevels.isEmpty() -> _state.value.level
            _state.value.level in supportedLevels -> _state.value.level
            else -> supportedLevels.first()
        }
        _state.value = _state.value.copy(
            selectedCharacterId = characterId,
            selectedScenarioId = null,
            scenarios = emptyList(),
            loadingScenarios = true,
            level = nextLevel,
            error = null,
            retryTarget = null,
        )
        scenarioJob = viewModelScope.launch {
            try {
                val scenarios = repository.scenarios(characterId).items
                if (_state.value.selectedCharacterId != characterId) return@launch
                _state.value = _state.value.copy(
                    loadingScenarios = false,
                    scenarios = scenarios,
                    selectedScenarioId = scenarios.firstOrNull()?.id,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (_state.value.selectedCharacterId != characterId) return@launch
                _state.value = _state.value.copy(
                    loadingScenarios = false,
                    error = e.message ?: "Could not load scenarios",
                    retryTarget = RetryTarget.SCENARIOS,
                )
            }
        }
    }

    fun selectScenario(scenarioId: String) {
        _state.value = _state.value.copy(selectedScenarioId = scenarioId)
    }

    fun selectLevel(level: String) {
        _state.value = _state.value.copy(level = level)
    }

    fun setLanguage(language: String) {
        viewModelScope.launch { tokenStore.saveTranslationLanguage(language) }
    }

    fun startConversation() {
        val snapshot = _state.value
        val characterId = snapshot.selectedCharacterId ?: return
        val scenarioId = snapshot.selectedScenarioId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(
                creatingConversation = true,
                error = null,
                retryTarget = null,
            )
            try {
                val created = repository.createConversation(
                    characterId,
                    scenarioId,
                    snapshot.level,
                    snapshot.language,
                )
                _state.value = _state.value.copy(
                    creatingConversation = false,
                    conversationId = created.conversationId,
                    messages = emptyList(),
                    suggestions = emptyList(),
                )
                loadMessages(created.conversationId)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    creatingConversation = false,
                    error = e.message ?: "Could not start conversation",
                    retryTarget = null,
                )
            }
        }
    }

    fun loadMessages(conversationId: String? = _state.value.conversationId) {
        val id = conversationId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(loadingMessages = true, error = null, retryTarget = null)
            try {
                val messages = repository.messages(id).items.map {
                    it.toPracticeMessage()
                }
                _state.value = _state.value.copy(loadingMessages = false, messages = messages)
                translateMissingMessages(_state.value.language)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loadingMessages = false,
                    error = e.message ?: "Could not load conversation",
                    retryTarget = RetryTarget.MESSAGES,
                )
            }
        }
    }

    fun updateDraft(text: String) {
        if (text != messageClientText) messageClientId = null
        _state.value = _state.value.copy(draft = text)
    }

    fun chooseSuggestion(text: String) {
        updateDraft(text)
    }

    fun sendMessage() {
        val conversationId = _state.value.conversationId ?: return
        val text = _state.value.draft.trim()
        if (text.isBlank() || _state.value.sending) return
        if (messageClientText != text || messageClientId == null) {
            messageClientText = text
            messageClientId = UUID.randomUUID().toString()
        }
        val clientId = messageClientId ?: return
        sendJob = viewModelScope.launch {
            _state.value = _state.value.copy(sending = true, error = null, retryTarget = null)
            try {
                val response = repository.sendMessage(conversationId, clientId, text)
                val language = _state.value.language
                val userMessage = response.userMessage.toPracticeMessage()
                val tutorMessage = response.tutorMessage.toPracticeMessage()
                _state.value = _state.value.copy(
                    sending = false,
                    draft = "",
                    messages = _state.value.messages + userMessage + tutorMessage,
                    suggestions = emptyList(),
                )
                messageClientText = null
                messageClientId = null
                translateMessage(tutorMessage, language)
            } catch (e: CancellationException) {
                _state.value = _state.value.copy(sending = false)
                throw e
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    sending = false,
                    error = e.message ?: "Message was not sent",
                    retryTarget = RetryTarget.SEND,
                )
            }
        }
    }

    fun cancelSend() {
        sendJob?.cancel()
    }

    fun loadSuggestions() {
        val conversationId = _state.value.conversationId ?: return
        suggestionJob?.cancel()
        suggestionJob = viewModelScope.launch {
            _state.value = _state.value.copy(
                loadingSuggestions = true,
                error = null,
                retryTarget = null,
            )
            try {
                val suggestions = repository.suggestions(conversationId, 3).suggestions.map { it.text }
                _state.value = _state.value.copy(
                    loadingSuggestions = false,
                    suggestions = suggestions,
                )
            } catch (e: CancellationException) {
                _state.value = _state.value.copy(loadingSuggestions = false)
                throw e
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loadingSuggestions = false,
                    error = e.message ?: "Could not load suggestions",
                    retryTarget = RetryTarget.SUGGESTIONS,
                )
            }
        }
    }

    fun cancelSuggestions() {
        suggestionJob?.cancel()
    }

    fun showDeleteConfirmation() {
        _state.value = _state.value.copy(deleteConfirmationVisible = true, deleteFailed = false)
    }

    fun dismissDeleteConfirmation() {
        _state.value = _state.value.copy(deleteConfirmationVisible = false)
    }

    fun deleteConversation(onDeleted: () -> Unit) {
        val conversationId = _state.value.conversationId ?: return
        onDeleteComplete = onDeleted
        viewModelScope.launch {
            _state.value = _state.value.copy(
                deleting = true,
                deleteConfirmationVisible = false,
                deleteFailed = false,
                error = null,
                retryTarget = null,
            )
            try {
                repository.deleteConversation(conversationId)
                _state.value = PracticeUiState(language = _state.value.language)
                onDeleteComplete?.invoke()
                onDeleteComplete = null
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    deleting = false,
                    error = "Could not delete conversation. You can try again.",
                    retryTarget = RetryTarget.DELETE,
                    deleteFailed = true,
                )
            }
        }
    }

    fun retry() {
        when (_state.value.retryTarget) {
            RetryTarget.CHARACTERS -> loadCharacters()
            RetryTarget.SCENARIOS -> _state.value.selectedCharacterId?.let(::selectCharacter)
            RetryTarget.MESSAGES -> loadMessages()
            RetryTarget.SUGGESTIONS -> loadSuggestions()
            RetryTarget.SEND -> sendMessage()
            RetryTarget.DELETE -> deleteConversation(onDeleteComplete ?: {})
            null -> Unit
        }
    }

    private fun translateMissingMessages(language: String) {
        val messages = _state.value.messages
        viewModelScope.launch {
            messages.filter { it.role.isTutor() && it.translations[language].isNullOrBlank() }
                .forEach { translateMessage(it, language) }
        }
    }

    private suspend fun translateMessage(message: PracticeMessage, language: String) {
        if (!message.role.isTutor() || language !in setOf("en", "vi")) return
        try {
            val translated = repository.translate(message.japanese, language, message.japanese)
                .translatedText
            _state.value = _state.value.copy(
                messages = _state.value.messages.map { current ->
                    if (current.id == message.id) {
                        current.copy(translations = current.translations + (language to translated))
                    } else current
                },
            )
        } catch (_: Exception) {
            // Keep the Japanese response visible when translation is unavailable.
        }
    }

    private fun ConversationMessageDto.toPracticeMessage() =
        PracticeMessage(
            id = id,
            role = role,
            japanese = text,
            reading = reading,
            translations = emptyMap(),
        )

    private fun String.isTutor(): Boolean =
        equals("assistant", ignoreCase = true) || equals("tutor", ignoreCase = true)

    companion object {
        fun factory(repository: PracticeRepository, tokenStore: TokenStore): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    PracticeViewModel(repository, tokenStore) as T
            }
    }
}
