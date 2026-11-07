package com.example.japaneseapp.android.data.repository

import com.example.japaneseapp.android.data.api.*
import com.example.japaneseapp.android.data.token.TokenStore

class ProgressRepository(tokenStore: TokenStore) {
    private val api = ApiClient.create(tokenStore)

    suspend fun listMyProgress(): List<ProgressDto> = api.listProgress()

    suspend fun upsert(lessonId: Long, status: ProgressStatus?, quizScore: Int?): ProgressDto {
        return api.upsertProgress(lessonId, UpdateProgressRequest(status = status, quizScore = quizScore))
    }
}
