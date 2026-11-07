package com.example.japaneseapp.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.japaneseapp.android.data.api.ProgressStatus
import com.example.japaneseapp.android.data.repository.AuthRepository
import com.example.japaneseapp.android.data.repository.LessonRepository
import com.example.japaneseapp.android.data.repository.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LessonListItem(
    val id: Long,
    val lessonNumber: Int,
    val title: String,
    val overview: String,
    val status: ProgressStatus? = null,
)

data class LessonsUiState(
    val loading: Boolean = false,
    val loggingOut: Boolean = false,
    val error: String? = null,
    val lessons: List<LessonListItem> = emptyList(),
)

class LessonsViewModel(
    private val lessonRepo: LessonRepository,
    private val progressRepo: ProgressRepository,
    private val authRepo: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LessonsUiState(loading = true))
    val state: StateFlow<LessonsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                val lessons = lessonRepo.listLessons()
                val progress = runCatching { progressRepo.listMyProgress() }.getOrDefault(emptyList())
                val progressByLesson = progress.associateBy { it.lessonId }
                _state.value = LessonsUiState(
                    lessons = lessons.map {
                        LessonListItem(
                            id = it.id,
                            lessonNumber = it.lessonNumber,
                            title = it.title,
                            overview = it.overview,
                            status = progressByLesson[it.id]?.status,
                        )
                    },
                )
            } catch (e: Exception) {
                _state.value = LessonsUiState(
                    error = e.message ?: "Failed to load lessons",
                )
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loggingOut = true, error = null)
            try {
                authRepo.logout()
                _state.value = _state.value.copy(loggingOut = false)
                onLoggedOut()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loggingOut = false,
                    error = e.message ?: "Could not clear the local session",
                )
            }
        }
    }

    companion object {
        fun factory(
            lessonRepo: LessonRepository,
            progressRepo: ProgressRepository,
            authRepo: AuthRepository,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    LessonsViewModel(lessonRepo, progressRepo, authRepo) as T
            }
    }
}
