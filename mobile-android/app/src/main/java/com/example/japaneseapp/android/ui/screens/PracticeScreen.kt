package com.example.japaneseapp.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.japaneseapp.android.viewmodel.PracticeMessage
import com.example.japaneseapp.android.viewmodel.PracticeViewModel

@Composable
fun PracticeScreen(
    viewModel: PracticeViewModel,
    onBackToLessons: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    if (state.deleteConfirmationVisible) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteConfirmation,
            title = { Text("Delete conversation?") },
            text = {
                Text(
                    "This permanently deletes this conversation and its transcript. " +
                        "Otherwise, transcripts are retained for no more than 30 days."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteConversation(onBackToLessons) },
                    enabled = !state.deleting,
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteConfirmation) { Text("Cancel") }
            },
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(
                onClick = onBackToLessons,
                enabled = !state.deleting,
            ) { Text("‹ Lessons") }
            Text(
                text = if (state.conversationId == null) "Roleplay practice" else "Conversation",
                style = MaterialTheme.typography.titleLarge,
            )
            if (state.conversationId != null) {
                TextButton(
                    onClick = viewModel::showDeleteConfirmation,
                    enabled = !state.deleting,
                ) { Text("Delete") }
            } else {
                Spacer(Modifier)
            }
        }

        LanguageSelector(
            language = state.language,
            onSelect = viewModel::setLanguage,
        )

        state.error?.let { error ->
            Text(error, color = MaterialTheme.colorScheme.error)
            if (state.retryTarget != null) {
                TextButton(onClick = viewModel::retry) { Text("Retry") }
            }
        }

        if (state.deleting) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Text("Deleting conversation…")
        } else if (state.conversationId == null) {
            CharacterAndScenarioChooser(viewModel)
        } else {
            ConversationContent(
                viewModel,
                state.messages,
                state.draft,
                state.suggestions,
                state.language,
            )
        }
    }
}

@Composable
private fun CharacterAndScenarioChooser(viewModel: PracticeViewModel) {
    val state by viewModel.state.collectAsState()
    if (state.loadingCharacters || state.loadingScenarios) {
        LinearProgressIndicator(Modifier.fillMaxWidth())
    }
    DropdownSelector(
        label = "Character",
        selected = state.characters.firstOrNull { it.id == state.selectedCharacterId }?.displayName
            ?: "Choose a character",
        options = state.characters.map { it.id to it.displayName },
        onSelect = viewModel::selectCharacter,
    )
    state.characters.firstOrNull { it.id == state.selectedCharacterId }?.description?.let {
        Text(it, style = MaterialTheme.typography.bodySmall)
    }
    DropdownSelector(
        label = "Scenario",
        selected = state.scenarios.firstOrNull { it.id == state.selectedScenarioId }?.title
            ?: "Choose a scenario",
        options = state.scenarios.map { it.id to it.title },
        onSelect = viewModel::selectScenario,
    )
    state.scenarios.firstOrNull { it.id == state.selectedScenarioId }?.description?.let {
        Text(it, style = MaterialTheme.typography.bodySmall)
    }
    DropdownSelector(
        label = "Level",
        selected = state.level,
        options = (
            state.characters.firstOrNull { it.id == state.selectedCharacterId }
                ?.supportedLevels
                ?.takeIf { it.isNotEmpty() }
                ?: listOf("N5", "N4", "N3", "N2", "N1")
            ).map { it to it },
        onSelect = viewModel::selectLevel,
    )
    Button(
        onClick = viewModel::startConversation,
        enabled = state.selectedCharacterId != null &&
            state.selectedScenarioId != null &&
            !state.loadingScenarios &&
            !state.creatingConversation,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (state.creatingConversation) CircularProgressIndicator()
        else Text("Start conversation")
    }
    if (state.creatingConversation) Text("Starting conversation…")
}

@Composable
private fun ColumnScope.ConversationContent(
    viewModel: PracticeViewModel,
    messages: List<PracticeMessage>,
    draft: String,
    suggestions: List<String>,
    language: String,
) {
    val state by viewModel.state.collectAsState()

    if (state.loadingMessages) {
        LinearProgressIndicator(Modifier.fillMaxWidth())
    }
    LazyColumn(
        modifier = Modifier.weight(1f).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(messages, key = { it.id }) { message ->
            MessageCard(message = message, isTutor = message.role.isTutorRole(), language = language)
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = viewModel::loadSuggestions,
            enabled = !state.loadingSuggestions && !state.sending,
        ) {
            Text(if (state.loadingSuggestions) "Loading…" else "Suggestions")
        }
        if (state.loadingSuggestions) {
            TextButton(onClick = viewModel::cancelSuggestions) { Text("Cancel") }
        }
    }
    suggestions.forEach { suggestion ->
        OutlinedButton(
            onClick = { viewModel.chooseSuggestion(suggestion) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(suggestion) }
    }

    OutlinedTextField(
        value = draft,
        onValueChange = viewModel::updateDraft,
        label = { Text("Type your reply in Japanese") },
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.sending,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        if (state.sending) {
            TextButton(onClick = viewModel::cancelSend) { Text("Cancel send") }
        } else {
            Button(
                onClick = viewModel::sendMessage,
                enabled = draft.isNotBlank(),
            ) { Text("Send") }
        }
    }
}

@Composable
private fun MessageCard(message: PracticeMessage, isTutor: Boolean, language: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(4.dp),
        horizontalAlignment = if (isTutor) Alignment.Start else Alignment.End,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(if (isTutor) "Tutor" else "You", style = MaterialTheme.typography.labelMedium)
        Text(message.japanese, style = MaterialTheme.typography.bodyLarge)
        message.reading?.takeIf(String::isNotBlank)?.let {
            Text(it, style = MaterialTheme.typography.bodySmall)
        }
        message.translations[language]?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
    }
}

@Composable
private fun LanguageSelector(language: String, onSelect: (String) -> Unit) {
    DropdownSelector(
        label = "Translation",
        selected = if (language == "vi") "Vietnamese" else "English",
        options = listOf("en" to "English", "vi" to "Vietnamese"),
        onSelect = onSelect,
    )
}

@Composable
private fun DropdownSelector(
    label: String,
    selected: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        androidx.compose.foundation.layout.Box {
            OutlinedButton(
                onClick = { expanded = true },
                enabled = options.isNotEmpty(),
            ) {
                Text(selected)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (value, title) ->
                    DropdownMenuItem(
                        text = { Text(title) },
                        onClick = {
                            expanded = false
                            onSelect(value)
                        },
                    )
                }
            }
        }
    }
}

private fun String.isTutorRole(): Boolean =
    equals("assistant", ignoreCase = true) || equals("tutor", ignoreCase = true)
