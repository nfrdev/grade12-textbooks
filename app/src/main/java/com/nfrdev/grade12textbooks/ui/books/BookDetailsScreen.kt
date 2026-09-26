package com.nfrdev.grade12textbooks.ui.books

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfrdev.grade12textbooks.R
import com.nfrdev.grade12textbooks.data.download.DownloadState
import androidx.navigation.NavController
import com.nfrdev.grade12textbooks.ui.components.EmptyState
import com.nfrdev.grade12textbooks.ui.components.LoadingState

@Composable
fun BookDetailsScreen(navController: NavController? = null, viewModel: BookDetailsViewModel = hiltViewModel()) {
    val book by viewModel.book.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val state by viewModel.downloadState.collectAsStateWithLifecycle()
    val currentState = state
    Box(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            Modifier.fillMaxWidth().widthIn(max = 760.dp).padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            when {
                isLoading -> LoadingState()
                book == null -> EmptyState(stringResource(R.string.book_not_found))
                else -> book?.let { selectedBook ->
                    Text(selectedBook.title, style = MaterialTheme.typography.headlineLarge)
                    Text(
                        stringResource(R.string.subject_label, selectedBook.subjectId.replace('_', ' ').replaceFirstChar { it.uppercase() }),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider()
                    Text(
                        selectedBook.description ?: stringResource(R.string.language_label, selectedBook.language),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            when (currentState) {
                                DownloadState.Completed -> {
                                    Text(stringResource(R.string.download_complete), style = MaterialTheme.typography.titleMedium)
                                    Button(onClick = { navController?.navigate("reader/${selectedBook.id}") }) {
                                        Text(stringResource(R.string.read_now))
                                    }
                                }
                                DownloadState.Paused -> {
                                    Text(stringResource(R.string.download_paused))
                                    Button(onClick = viewModel::download) { Text(stringResource(R.string.resume)) }
                                }
                                is DownloadState.InProgress -> {
                                    Text(currentState.progress?.let { stringResource(R.string.download_percent, (it * 100).toInt()) }
                                        ?: stringResource(R.string.download_in_progress))
                                    LinearProgressIndicator(progress = { currentState.progress ?: 0f }, modifier = Modifier.fillMaxWidth())
                                    Button(onClick = viewModel::cancel) { Text(stringResource(R.string.cancel)) }
                                }
                                DownloadState.Queued -> {
                                    Text(stringResource(R.string.download_waiting))
                                    Button(onClick = viewModel::cancel) { Text(stringResource(R.string.cancel)) }
                                }
                                DownloadState.Verifying -> {
                                    Text(stringResource(R.string.verifying_download))
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                }
                                is DownloadState.Failed -> {
                                    Text(stringResource(R.string.download_failed), color = MaterialTheme.colorScheme.error)
                                    Button(onClick = viewModel::download) { Text(stringResource(R.string.retry)) }
                                }
                                DownloadState.Idle -> Button(onClick = viewModel::download) {
                                    Text(stringResource(R.string.download))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
