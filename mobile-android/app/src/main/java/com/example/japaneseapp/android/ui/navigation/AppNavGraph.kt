package com.example.japaneseapp.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.japaneseapp.android.data.repository.AuthRepository
import com.example.japaneseapp.android.data.repository.LessonRepository
import com.example.japaneseapp.android.data.repository.ProgressRepository
import com.example.japaneseapp.android.data.repository.PracticeRepository
import com.example.japaneseapp.android.data.token.TokenStore
import com.example.japaneseapp.android.ui.screens.LessonDetailScreen
import com.example.japaneseapp.android.ui.screens.LessonsScreen
import com.example.japaneseapp.android.ui.screens.LoginScreen
import com.example.japaneseapp.android.ui.screens.RegisterScreen
import com.example.japaneseapp.android.ui.screens.PracticeScreen
import com.example.japaneseapp.android.viewmodel.AuthViewModel
import com.example.japaneseapp.android.viewmodel.LessonDetailViewModel
import com.example.japaneseapp.android.viewmodel.LessonsViewModel
import com.example.japaneseapp.android.viewmodel.PracticeViewModel

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    val tokenStore = TokenStore.current()
    val authRepo = remember { AuthRepository(tokenStore) }
    val lessonRepo = remember { LessonRepository(tokenStore) }
    val progressRepo = remember { ProgressRepository(tokenStore) }
    val practiceRepo = remember { PracticeRepository(tokenStore) }

    NavHost(navController = navController, startDestination = Route.Login.value) {
        composable(Route.Login.value) {
            val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.factory(authRepo))
            LoginScreen(
                viewModel = authViewModel,
                onLoggedIn = { navController.navigate(Route.Lessons.value) { popUpTo(Route.Login.value) { inclusive = true } } },
                onGoRegister = { navController.navigate(Route.Register.value) }
            )
        }
        composable(Route.Register.value) {
            val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.factory(authRepo))
            RegisterScreen(
                viewModel = authViewModel,
                onRegistered = { navController.navigate(Route.Lessons.value) { popUpTo(Route.Login.value) { inclusive = true } } },
                onGoLogin = { navController.popBackStack() }
            )
        }
        composable(route = Route.Lessons.value) {
            val lessonsViewModel: LessonsViewModel = viewModel(
                factory = LessonsViewModel.factory(lessonRepo, progressRepo, authRepo)
            )
            LessonsScreen(
                viewModel = lessonsViewModel,
                onOpenLesson = { id -> navController.navigate(Route.LessonDetail.create(id)) },
                onOpenPractice = { navController.navigate(Route.Practice.value) },
                onLogout = {
                    navController.navigate(Route.Login.value) {
                        popUpTo(Route.Lessons.value) { inclusive = true }
                    }
                },
            )
        }

        composable(Route.Practice.value) {
            val practiceViewModel: PracticeViewModel = viewModel(
                factory = PracticeViewModel.factory(practiceRepo, tokenStore)
            )
            PracticeScreen(
                viewModel = practiceViewModel,
                onBackToLessons = { navController.popBackStack() },
            )
        }

        composable(
            route = Route.LessonDetail.value,
            arguments = listOf(navArgument("lessonId") { type = NavType.LongType })
        ) { entry ->
            val lessonId = entry.arguments?.getLong("lessonId") ?: return@composable
            val detailViewModel: LessonDetailViewModel = viewModel(
                factory = LessonDetailViewModel.factory(lessonRepo, progressRepo, lessonId)
            )
            LessonDetailScreen(
                viewModel = detailViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
