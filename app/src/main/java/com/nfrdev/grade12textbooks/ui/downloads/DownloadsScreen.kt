package com.nfrdev.grade12textbooks.ui.downloads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo
import com.nfrdev.grade12textbooks.R

@Composable
fun DownloadsScreen(
    onBrowseSubjects: () -> Unit = {},
    onOpenBook: (String) -> Unit = {},
    viewModel: DownloadsViewModel = hiltViewModel()
) {
    val downloads by viewModel.items.collectAsStateWithLifecycle()
    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(stringResource(R.string.downloads), style = MaterialTheme.typography.headlineMedium)
        if (downloads.isEmpty()) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.DownloadForOffline, null, tint = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.downloads_empty_title), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.downloads_empty_description), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = onBrowseSubjects) { Text(stringResource(R.string.browse_subjects)) }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 320.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(downloads, key = { it.book.id }) { item ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(item.book.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            val status = when (item.state) {
                                WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> stringResource(R.string.download_waiting)
                                WorkInfo.State.RUNNING -> item.progress?.let { stringResource(R.string.download_percent, (it * 100).toInt()) } ?: stringResource(R.string.download_in_progress)
                                WorkInfo.State.FAILED -> stringResource(R.string.download_failed)
                                WorkInfo.State.CANCELLED -> stringResource(R.string.download_cancelled)
                                WorkInfo.State.SUCCEEDED -> stringResource(R.string.download_complete)
                            }
                            Text(status, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (item.state == WorkInfo.State.RUNNING) LinearProgressIndicator(progress = { item.progress ?: 0f }, modifier = Modifier.fillMaxWidth())
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                if (item.state == WorkInfo.State.FAILED) TextButton(onClick = { onOpenBook(item.book.id) }) { Text(stringResource(R.string.retry)) }
                                if (item.state == WorkInfo.State.RUNNING || item.state == WorkInfo.State.ENQUEUED || item.state == WorkInfo.State.BLOCKED) {
                                    TextButton(onClick = { viewModel.cancel(item.book.id) }) { Text(stringResource(R.string.cancel)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
