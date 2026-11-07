package com.example.japaneseapp.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.japaneseapp.android.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoggedIn: () -> Unit,
    onGoRegister: () -> Unit,
) {
    val authState by viewModel.state.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }

    val emailValid = LoginValidation.isValidEmail(authState.email)
    val passwordValid = LoginValidation.isValidPassword(authState.password)
    val formState = LoginFormState(
        email = authState.email,
        password = authState.password,
        emailError = if (submitted && !emailValid) "Hãy nhập địa chỉ email hợp lệ nhé." else null,
        passwordError = if (submitted && !passwordValid) {
            "Mật khẩu cần có ít nhất ${LoginValidation.MIN_PASSWORD_LENGTH} ký tự."
        } else {
            null
        },
        serverError = authState.error,
        loading = authState.loading,
        passwordVisible = passwordVisible
    )
    val actions = LoginFormActions(
        onEmailChange = viewModel::updateEmail,
        onPasswordChange = viewModel::updatePassword,
        onPasswordFocus = { passwordFocused = it },
        onTogglePassword = { passwordVisible = !passwordVisible },
        onLogin = {
            submitted = true
            if (emailValid && passwordValid) viewModel.login(onLoggedIn)
        },
        onGoRegister = onGoRegister
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFE6F0), Color(0xFFE5F8FC), Color(0xFFFFF8D9))
                )
            )
    ) {
        if (maxWidth >= 760.dp) {
            WideLoginLayout(
                formState = formState,
                actions = actions,
                passwordFocused = passwordFocused
            )
        } else {
            CompactLoginLayout(
                formState = formState,
                actions = actions,
                passwordFocused = passwordFocused
            )
        }
    }
}

@Composable
private fun WideLoginLayout(
    formState: LoginFormState,
    actions: LoginFormActions,
    passwordFocused: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(36.dp)
    ) {
        LoginWelcome(
            passwordFocused = passwordFocused,
            modifier = Modifier.weight(1f).fillMaxHeight()
        )
        LoginFormCard(
            state = formState,
            actions = actions,
            modifier = Modifier.weight(1f).widthIn(max = 500.dp)
        )
    }
}

@Composable
private fun CompactLoginLayout(
    formState: LoginFormState,
    actions: LoginFormActions,
    passwordFocused: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LoginWelcome(
            passwordFocused = passwordFocused,
            modifier = Modifier.fillMaxWidth().height(270.dp)
        )
        Spacer(Modifier.height(18.dp))
        LoginFormCard(
            state = formState,
            actions = actions,
            modifier = Modifier.fillMaxWidth().widthIn(max = 500.dp)
        )
        Spacer(Modifier.height(12.dp))
    }
}
