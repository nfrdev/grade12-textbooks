package com.nfrdev.grade12textbooks.ui.reader

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfrdev.grade12textbooks.R

@Composable
fun ReaderScreen(viewModel: ReaderViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            when (val current = state) {
                ReaderViewModel.ReaderState.Loading -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Text(text = stringResource(R.string.loading), modifier = Modifier.padding(top = 12.dp))
                        }
                    }
                }
                ReaderViewModel.ReaderState.Error -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(text = stringResource(R.string.reader_error), color = MaterialTheme.colorScheme.error)
                    }
                }
                is ReaderViewModel.ReaderState.Ready -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.page_indicator, current.page + 1, current.total),
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(onClick = { viewModel.addBookmark(null) }) {
                            Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = "Add Bookmark")
                        }
                    }

                    Image(
                        bitmap = current.bitmap.asImageBitmap(),
                        contentDescription = stringResource(R.string.pdf_page),
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = viewModel::previous,
                    enabled = state is ReaderViewModel.ReaderState.Ready && (state as ReaderViewModel.ReaderState.Ready).page > 0
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    Text(stringResource(R.string.previous), modifier = Modifier.padding(start = 4.dp))
                }
                Button(
                    onClick = viewModel::next,
                    enabled = state is ReaderViewModel.ReaderState.Ready && (state as ReaderViewModel.ReaderState.Ready).page < (state as ReaderViewModel.ReaderState.Ready).total - 1
                ) {
                    Text(stringResource(R.string.next), modifier = Modifier.padding(end = 4.dp))
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}
