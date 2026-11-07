package com.example.japaneseapp.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.japaneseapp.android.viewmodel.LessonsViewModel

@Composable
fun LessonsScreen(
    viewModel: LessonsViewModel,
    onOpenLesson: (Long) -> Unit,
    onOpenPractice: () -> Unit,
    onLogout: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Lessons", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = viewModel::refresh, enabled = !state.loading) { Text("Refresh") }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onOpenPractice) { Text("Practice chat") }
            TextButton(
                onClick = { viewModel.logout(onLogout) },
                enabled = !state.loggingOut,
            ) {
                Text(if (state.loggingOut) "Signing out…" else "Log out")
            }
        }

        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        if (state.loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.lessons) { item ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenLesson(item.id) }
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "Lesson ${item.lessonNumber}: ${item.title}", style = MaterialTheme.typography.titleMedium)
                        Text(text = item.overview, style = MaterialTheme.typography.bodyMedium)
                        item.status?.let { Text(text = "Status: $it", style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }
    }
}
