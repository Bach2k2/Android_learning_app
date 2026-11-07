package com.example.japaneseapp.android.ui.screens

internal object LoginValidation {
    private val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean = email.trim().matches(emailPattern)

    fun isValidPassword(password: String): Boolean = password.length >= MIN_PASSWORD_LENGTH

    const val MIN_PASSWORD_LENGTH = 8
}
