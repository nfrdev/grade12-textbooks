package com.nfrdev.grade12textbooks.ui.books

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfrdev.grade12textbooks.R
import com.nfrdev.grade12textbooks.data.download.DownloadState
import androidx.navigation.NavController

@Composable
fun BookDetailsScreen(navController: NavController? = null, viewModel: BookDetailsViewModel = hiltViewModel()) {
    val book by viewModel.book.collectAsStateWithLifecycle()
    val state by viewModel.downloadState.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (book == null) Text(stringResource(R.string.book_not_found)) else {
            Text(book!!.title)
            Text(book!!.language)
            when (state) {
                DownloadState.Completed -> { Text(stringResource(R.string.download_complete)); Button(onClick = { navController?.navigate("reader/${book!!.id}") }) { Text(stringResource(R.string.read_now)) } }
                DownloadState.Paused -> Button(onClick = viewModel::download) { Text(stringResource(R.string.resume)) }
                is DownloadState.InProgress, DownloadState.Verifying, DownloadState.Queued -> Button(onClick = viewModel::cancel) { Text(stringResource(R.string.cancel)) }
                else -> Button(onClick = viewModel::download) { Text(stringResource(R.string.download)) }
            }
        }
    }
}
