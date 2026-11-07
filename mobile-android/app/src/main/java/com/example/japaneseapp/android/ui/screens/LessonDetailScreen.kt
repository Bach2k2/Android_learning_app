package com.example.japaneseapp.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.japaneseapp.android.viewmodel.LessonDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonDetailScreen(
    viewModel: LessonDetailViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val lesson = state.lesson

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(lesson?.title ?: "Lesson") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Loading indicator (fixed at top)
            if (state.loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                state.error?.let { errorMsg ->
                    item {
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                state.feedback?.let { feedbackMsg ->
                    item {
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = feedbackMsg)
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = viewModel::clearMessages) { Text("Dismiss") }
                            }
                        }
                    }
                }

                state.aiText?.let { aiResponse ->
                    item {
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = "AI", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = aiResponse)
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = viewModel::clearMessages) { Text("Dismiss") }
                            }
                        }
                    }
                }

                // Lesson content
                if (lesson != null) {
                    item {
                        Text(
                            text = "Lesson ${lesson.lessonNumber}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(text = lesson.overview)
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { viewModel.generateQuiz(5) }) { Text("AI: Generate Quiz") }
                            OutlinedButton(onClick = {
                                val grammarText = lesson.grammarPoints.joinToString("\n") {
                                    "- ${it.title}: ${it.explanation}"
                                }
                                viewModel.explainGrammar(grammarText)
                            }) { Text("AI: Explain Grammar") }
                        }
                    }

                    item { Text(text = "Vocabulary", style = MaterialTheme.typography.titleMedium) }
                    items(lesson.vocabulary) { v ->
                        val kanjiText = if (v.kanji != null) " (${v.kanji})" else ""
                        Text(text = "${v.kana}$kanjiText — ${v.meaning}")
                    }

                    item { Text(text = "Grammar", style = MaterialTheme.typography.titleMedium) }
                    items(lesson.grammarPoints) { g ->
                        Text(text = "${g.title}: ${g.explanation}")
                    }

                    item { Text(text = "Examples", style = MaterialTheme.typography.titleMedium) }
                    items(lesson.exampleSentences) { e ->
                        Text(text = "${e.japanese}  (${e.english})")
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                val vocabText = lesson.vocabulary.joinToString("\n") {
                                    "${it.kana} = ${it.meaning}"
                                }
                                viewModel.reviewVocab(vocabText)
                            }) { Text("AI: Review Vocab") }
                        }
                    }

                    item { Text(text = "Quiz", style = MaterialTheme.typography.titleMedium) }
                    items(lesson.quiz) { q ->
                        ElevatedCard {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = q.prompt)
                                q.options.forEachIndexed { idx, opt ->
                                    OutlinedButton(
                                        onClick = { viewModel.checkAnswer(q.id, idx) },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text(text = opt)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
