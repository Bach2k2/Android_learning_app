package com.example.japaneseapp.android.ui.navigation

sealed class Route(val value: String) {
    data object Welcome : Route("welcome")
    data object Login : Route("login")
    data object Register : Route("register")
    data object Lessons : Route("lessons")
    data object LessonDetail : Route("lesson/{lessonId}") {
        fun create(lessonId: Long) = "lesson/$lessonId"
    }
    data object Practice : Route("practice")
}
