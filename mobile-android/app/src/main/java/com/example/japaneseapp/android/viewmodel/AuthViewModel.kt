package com.example.japaneseapp.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.japaneseapp.android.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
)

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun updateEmail(email: String) {
        _state.value = _state.value.copy(email = email, error = null)
    }

    fun updatePassword(password: String) {
        _state.value = _state.value.copy(password = password, error = null)
    }

    fun login(onSuccess: () -> Unit) {
        val snapshot = _state.value
        viewModelScope.launch {
            _state.value = snapshot.copy(loading = true, error = null)
            try {
                repo.login(snapshot.email.trim(), snapshot.password)
                _state.value = _state.value.copy(loading = false)
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = e.message ?: "Login failed")
            }
        }
    }

    fun register(onSuccess: () -> Unit) {
        val snapshot = _state.value
        viewModelScope.launch {
            _state.value = snapshot.copy(loading = true, error = null)
            try {
                repo.register(snapshot.email.trim(), snapshot.password)
                _state.value = _state.value.copy(loading = false)
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = e.message ?: "Registration failed")
            }
        }
    }

    companion object {
        fun factory(repo: AuthRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(repo) as T
                }
            }
        }
    }
}
