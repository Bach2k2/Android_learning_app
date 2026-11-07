package com.example.japaneseapp.android.data.api

import com.example.japaneseapp.android.BuildConfig
import com.example.japaneseapp.android.data.token.TokenStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private val refreshLock = Any()

    fun create(tokenStore: TokenStore): ApiService {
        val base = BuildConfig.API_BASE_URL.trimEnd('/') + "/api/"
        val refreshClient = OkHttpClient.Builder().build()

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            if (isPublicAuthRequest(original)) {
                return@Interceptor chain.proceed(original)
            }

            val failedAccess = runBlocking { tokenStore.accessToken.first() }
            val response = chain.proceed(original.withBearer(failedAccess))
            if (response.code != 401) return@Interceptor response

            val newAccess = refreshAccessToken(base, tokenStore, failedAccess, refreshClient)
                ?: return@Interceptor response
            response.close()
            chain.proceed(original.withBearer(newAccess))
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(base)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }

    private fun Request.withBearer(accessToken: String?): Request =
        newBuilder().apply {
            if (!accessToken.isNullOrBlank()) {
                header("Authorization", "Bearer $accessToken")
            } else {
                removeHeader("Authorization")
            }
        }.build()

    private fun isPublicAuthRequest(request: Request): Boolean =
        request.url.encodedPath.let { path ->
            path.endsWith("/auth/login") ||
                path.endsWith("/auth/register") ||
                path.endsWith("/auth/refresh") ||
                path.endsWith("/auth/logout")
        }

    private fun refreshAccessToken(
        baseUrl: String,
        tokenStore: TokenStore,
        failedAccessToken: String?,
        client: OkHttpClient,
    ): String? = synchronized(refreshLock) {
        runBlocking {
            val currentAccess = tokenStore.accessToken.first()
            if (!currentAccess.isNullOrBlank() && currentAccess != failedAccessToken) {
                return@runBlocking currentAccess
            }

            val refreshToken = tokenStore.refreshToken.first()
            if (refreshToken.isNullOrBlank()) {
                tokenStore.clear()
                return@runBlocking null
            }

            try {
                val body = Gson().toJson(RefreshRequest(refreshToken))
                    .toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url("${baseUrl}auth/refresh")
                    .post(body)
                    .build()
                client.newCall(request).execute().use { response ->
                    if (response.code == 401) {
                        tokenStore.clear()
                        return@runBlocking null
                    }
                    if (!response.isSuccessful) return@runBlocking null

                    val json = response.body?.string() ?: return@runBlocking null
                    val tokens = Gson().fromJson(json, AuthResponse::class.java)
                        ?: return@runBlocking null
                    if (tokens.accessToken.isBlank() || tokens.refreshToken.isBlank()) {
                        return@runBlocking null
                    }
                    tokenStore.saveTokens(tokens.accessToken, tokens.refreshToken)
                    tokens.accessToken
                }
            } catch (_: Exception) {
                null
            }
        }
    }
}
