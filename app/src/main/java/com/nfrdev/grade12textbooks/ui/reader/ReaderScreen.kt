package com.nfrdev.grade12textbooks.ui.reader

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfrdev.grade12textbooks.R

@Composable fun ReaderScreen(viewModel: ReaderViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
        when (val current = state) {
            ReaderViewModel.ReaderState.Loading -> Text(stringResource(R.string.loading))
            ReaderViewModel.ReaderState.Error -> Text(stringResource(R.string.reader_error))
            is ReaderViewModel.ReaderState.Ready -> {
                Image(current.bitmap.asImageBitmap(), contentDescription = stringResource(R.string.pdf_page), modifier = Modifier.weight(1f).fillMaxWidth())
                Text(stringResource(R.string.page_indicator, current.page + 1, current.total))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = viewModel::previous) { Text(stringResource(R.string.previous)) }
            Button(onClick = viewModel::next) { Text(stringResource(R.string.next)) }
        }
    }
}
