package com.example.japaneseapp.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.japaneseapp.android.data.api.LessonDetailDto
import com.example.japaneseapp.android.data.api.ProgressStatus
import com.example.japaneseapp.android.data.repository.LessonRepository
import com.example.japaneseapp.android.data.repository.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LessonDetailUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val lesson: LessonDetailDto? = null,
    val feedback: String? = null,
    val aiText: String? = null,
)

class LessonDetailViewModel(
    private val lessonRepo: LessonRepository,
    private val progressRepo: ProgressRepository,
    private val lessonId: Long,
) : ViewModel() {

    private val _state = MutableStateFlow(LessonDetailUiState(loading = true))
    val state: StateFlow<LessonDetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = LessonDetailUiState(loading = true)
            try {
                val lesson = lessonRepo.getLesson(lessonId)
                runCatching { progressRepo.upsert(lessonId, ProgressStatus.IN_PROGRESS, null) }
                _state.value = LessonDetailUiState(loading = false, lesson = lesson)
            } catch (e: Exception) {
                _state.value = LessonDetailUiState(loading = false, error = e.message ?: "Failed to load lesson")
            }
        }
    }

    fun checkAnswer(questionId: String, selectedIndex: Int) {
        viewModelScope.launch {
            try {
                val res = lessonRepo.checkAnswer(lessonId, questionId, selectedIndex)
                val msg = if (res.correct) {
                    "Correct"
                } else {
                    "Incorrect (answer: ${res.correctIndex})"
                }
                _state.value = _state.value.copy(feedback = buildString {
                    append(msg)
                    if (!res.explanation.isNullOrBlank()) {
                        append("\n")
                        append(res.explanation)
                    }
                })
            } catch (e: Exception) {
                _state.value = _state.value.copy(feedback = e.message ?: "Failed to check answer")
            }
        }
    }

    fun generateQuiz(count: Int = 5) {
        viewModelScope.launch {
            try {
                lessonRepo.generateQuiz(lessonId, count)
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(feedback = e.message ?: "AI quiz generation failed")
            }
        }
    }

    fun explainGrammar(text: String) {
        viewModelScope.launch {
            try {
                val res = lessonRepo.explainGrammar(text)
                _state.value = _state.value.copy(aiText = res.explanation)
            } catch (e: Exception) {
                _state.value = _state.value.copy(aiText = e.message ?: "AI explanation failed")
            }
        }
    }

    fun reviewVocab(text: String) {
        viewModelScope.launch {
            try {
                val res = lessonRepo.reviewVocab(text)
                _state.value = _state.value.copy(aiText = res.explanation)
            } catch (e: Exception) {
                _state.value = _state.value.copy(aiText = e.message ?: "AI review failed")
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(feedback = null, aiText = null)
    }

    companion object {
        fun factory(
            lessonRepo: LessonRepository,
            progressRepo: ProgressRepository,
            lessonId: Long,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LessonDetailViewModel(lessonRepo, progressRepo, lessonId) as T
                }
            }
        }
    }
}
