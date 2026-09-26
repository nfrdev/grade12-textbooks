package com.nfrdev.grade12textbooks.ui.bookmarks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.Button
import com.nfrdev.grade12textbooks.R
import com.nfrdev.grade12textbooks.data.local.entity.BookmarkEntity
import com.nfrdev.grade12textbooks.ui.components.EmptyState
import com.nfrdev.grade12textbooks.ui.components.ErrorState
import com.nfrdev.grade12textbooks.ui.components.LoadingState

@Composable
fun BookmarksScreen(
    onOpenPage: (Int?) -> Unit = {},
    viewModel: BookmarksViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(
        Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.bookmarks), style = MaterialTheme.typography.headlineMedium)
        when (val current = state) {
            BookmarksViewModel.State.Loading -> LoadingState()
            BookmarksViewModel.State.Error -> ErrorState(
                message = stringResource(R.string.generic_error),
                onRetry = viewModel::retry
            )
            is BookmarksViewModel.State.Empty -> {
                EmptyState(stringResource(R.string.no_bookmarks))
                Button(onClick = { onOpenPage(null) }) { Text(stringResource(R.string.read_now)) }
            }
            is BookmarksViewModel.State.Success -> {
                Text(current.bookTitle, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(current.bookmarks, key = BookmarkEntity::id) { bookmark ->
                        BookmarkCard(
                            bookmark = bookmark,
                            onOpenPage = { onOpenPage(bookmark.page) },
                            onDelete = { viewModel.delete(bookmark) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarkCard(bookmark: BookmarkEntity, onOpenPage: () -> Unit, onDelete: () -> Unit) {
    Card(
        onClick = onOpenPage,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 18.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(
                Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(stringResource(R.string.bookmark_page_label, bookmark.page + 1), style = MaterialTheme.typography.titleMedium)
                Text(bookmark.note ?: stringResource(R.string.bookmark_no_note), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = stringResource(R.string.bookmark_remove_accessibility, bookmark.page + 1)
                )
            }
        }
    }
}
