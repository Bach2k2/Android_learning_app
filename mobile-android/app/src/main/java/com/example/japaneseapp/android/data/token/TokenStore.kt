package com.example.japaneseapp.android.data.token

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "tokens")

class TokenStore(private val context: Context) {

    private val accessKey = stringPreferencesKey("access")
    private val refreshKey = stringPreferencesKey("refresh")
    private val translationLanguageKey = stringPreferencesKey("translation_language")

    val accessToken: Flow<String?> = context.dataStore.data.map { it[accessKey] }
    val refreshToken: Flow<String?> = context.dataStore.data.map { it[refreshKey] }
    val translationLanguage: Flow<String> =
        context.dataStore.data.map { it[translationLanguageKey] ?: "en" }

    suspend fun saveTokens(access: String, refresh: String) {
        context.dataStore.edit {
            it[accessKey] = access
            it[refreshKey] = refresh
        }
    }

    suspend fun clear() {
        context.dataStore.edit {
            it.remove(accessKey)
            it.remove(refreshKey)
        }
    }

    suspend fun saveTranslationLanguage(language: String) {
        require(language == "en" || language == "vi")
        context.dataStore.edit { it[translationLanguageKey] = language }
    }

    companion object {
        // Simple service-locator access for this MVP.
        @Volatile private var instance: TokenStore? = null

        fun init(context: Context) {
            instance = TokenStore(context.applicationContext)
        }

        fun current(): TokenStore {
            return instance ?: error("TokenStore not initialized. Call TokenStore.init() from Application.")
        }
    }
}
