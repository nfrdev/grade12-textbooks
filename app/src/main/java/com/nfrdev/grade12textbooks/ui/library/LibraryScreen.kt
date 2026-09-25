package com.nfrdev.grade12textbooks.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfrdev.grade12textbooks.R
import com.nfrdev.grade12textbooks.ui.components.BookCard

@Composable fun LibraryScreen(viewModel: LibraryViewModel = hiltViewModel()) {
    val books by viewModel.books.collectAsStateWithLifecycle()
    Column(Modifier.padding(20.dp)) {
        Text(stringResource(R.string.library))
        if (books.isEmpty()) Text(stringResource(R.string.no_downloaded_books)) else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(books) { BookCard(it.title) {} } }
    }
}
