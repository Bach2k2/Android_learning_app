package com.example.japaneseapp.android.data.repository

import com.example.japaneseapp.android.data.api.*
import com.example.japaneseapp.android.data.token.TokenStore

class LessonRepository(tokenStore: TokenStore) {
    private val api = ApiClient.create(tokenStore)

    suspend fun listLessons(): List<LessonSummaryDto> = api.listLessons()

    suspend fun getLesson(lessonId: Long): LessonDetailDto = api.getLesson(lessonId)

    suspend fun checkAnswer(lessonId: Long, questionId: String, selectedIndex: Int): CheckAnswerResponse {
        return api.checkAnswer(lessonId, questionId, CheckAnswerRequest(selectedIndex))
    }

    suspend fun generateQuiz(lessonId: Long, count: Int): GenerateQuizResponse {
        return api.generateQuiz(lessonId, count)
    }

    suspend fun explainGrammar(text: String): ExplainResponse = api.explainGrammar(ExplainRequest(text))

    suspend fun reviewVocab(text: String): ExplainResponse = api.reviewVocab(ExplainRequest(text))
}
