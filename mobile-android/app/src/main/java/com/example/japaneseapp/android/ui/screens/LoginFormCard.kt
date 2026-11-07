package com.example.japaneseapp.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal data class LoginFormState(
    val email: String,
    val password: String,
    val emailError: String?,
    val passwordError: String?,
    val serverError: String?,
    val loading: Boolean,
    val passwordVisible: Boolean
)

internal data class LoginFormActions(
    val onEmailChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onPasswordFocus: (Boolean) -> Unit,
    val onTogglePassword: () -> Unit,
    val onLogin: () -> Unit,
    val onGoRegister: () -> Unit
)

@Composable
internal fun LoginFormCard(
    state: LoginFormState,
    actions: LoginFormActions,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
        color = Color(0xF9FFFFFF),
        shadowElevation = 18.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 30.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LoginBrand()
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Đăng Nhập Học Tập",
                color = LoginPalette.ink,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Nhập thông tin để tiếp tục hành trình nhé!",
                color = LoginPalette.muted,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(4.dp))
            LoginEmailField(
                email = state.email,
                error = state.emailError,
                onEmailChange = actions.onEmailChange
            )
            LoginPasswordField(
                password = state.password,
                error = state.passwordError,
                visible = state.passwordVisible,
                onPasswordChange = actions.onPasswordChange,
                onFocusChanged = actions.onPasswordFocus,
                onToggleVisibility = actions.onTogglePassword,
                onDone = actions.onLogin
            )
            state.serverError?.let { ValidationMessage(it) }
            SessionPersistenceNote()
            LoginSubmitButton(
                loading = state.loading,
                onClick = actions.onLogin
            )
            TextButton(
                onClick = actions.onGoRegister,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Chưa có tài khoản?  Tạo tài khoản mới", color = LoginPalette.violet)
            }
            Text(
                text = "Học vui mỗi ngày, tiến bộ từng chút một ♡",
                modifier = Modifier.fillMaxWidth(),
                color = LoginPalette.muted,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LoginBrand() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(LoginPalette.palePink, RoundedCornerShape(17.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("✿", color = LoginPalette.violet, fontSize = 29.sp)
        }
        Column {
            Text(
                text = "KOTO STUDY",
                color = LoginPalette.violet,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Góc học tập của bạn",
                color = LoginPalette.muted,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun LoginEmailField(
    email: String,
    error: String?,
    onEmailChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Tài khoản / Email", color = LoginPalette.ink, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = { Text("hello@example.com", color = Color(0xFFAAA1B3)) },
            leadingIcon = { Text("✉️", fontSize = 20.sp) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = error != null,
            shape = RoundedCornerShape(18.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            colors = loginFieldColors()
        )
        error?.let { ValidationMessage(it) }
    }
}

@Composable
private fun LoginPasswordField(
    password: String,
    error: String?,
    visible: Boolean,
    onPasswordChange: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    onToggleVisibility: () -> Unit,
    onDone: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Mật khẩu", color = LoginPalette.ink, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = { Text("Ít nhất ${LoginValidation.MIN_PASSWORD_LENGTH} ký tự", color = Color(0xFFAAA1B3)) },
            leadingIcon = { Text("🔒", fontSize = 19.sp) },
            trailingIcon = {
                TextButton(onClick = onToggleVisibility) {
                    Text(if (visible) "👁️" else "🙈", fontSize = 19.sp)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { onFocusChanged(it.isFocused) },
            singleLine = true,
            isError = error != null,
            shape = RoundedCornerShape(18.dp),
            visualTransformation = if (visible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            colors = loginFieldColors()
        )
        error?.let { ValidationMessage(it) }
    }
}

@Composable
private fun SessionPersistenceNote() {
    Surface(
        color = Color(0xFFF8F2FF),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("🔒", fontSize = 16.sp)
            Text(
                text = "Phiên đăng nhập được lưu tự động",
                color = LoginPalette.muted,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun LoginSubmitButton(loading: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Button(
        onClick = onClick,
        enabled = !loading,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .scale(if (pressed) 0.98f else 1f),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = LoginPalette.violet),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 2.dp
        )
    ) {
        Text(
            text = if (loading) "Đang đăng nhập..." else "Bắt đầu học ngay!  ✨",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun ValidationMessage(message: String) {
    Text(
        text = "♡  $message",
        color = LoginPalette.errorPink,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = LoginPalette.violet,
    unfocusedBorderColor = Color(0xFFE8DFED),
    errorBorderColor = LoginPalette.errorPink,
    focusedLabelColor = LoginPalette.violet,
    cursorColor = LoginPalette.violet,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White
)
