package com.example.japaneseapp.android.data.repository

import com.example.japaneseapp.android.data.api.ApiClient
import com.example.japaneseapp.android.data.api.LoginRequest
import com.example.japaneseapp.android.data.api.LogoutRequest
import com.example.japaneseapp.android.data.api.RegisterRequest
import com.example.japaneseapp.android.data.token.TokenStore
import kotlinx.coroutines.flow.first

class AuthRepository(private val tokenStore: TokenStore) {

    private val api = ApiClient.create(tokenStore)

    suspend fun register(email: String, password: String) {
        val res = api.register(RegisterRequest(email = email, password = password))
        tokenStore.saveTokens(res.accessToken, res.refreshToken)
    }

    suspend fun login(email: String, password: String) {
        val res = api.login(LoginRequest(email = email, password = password))
        tokenStore.saveTokens(res.accessToken, res.refreshToken)
    }

    suspend fun logout() {
        val refresh = tokenStore.refreshToken.first()
        tokenStore.clear()
        if (!refresh.isNullOrBlank()) {
            runCatching { api.logout(LogoutRequest(refreshToken = refresh)) }
        }
    }
}
