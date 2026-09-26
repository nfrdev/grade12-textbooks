package com.nfrdev.grade12textbooks.ui.books

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nfrdev.grade12textbooks.ui.components.*
import androidx.compose.ui.res.stringResource
import com.nfrdev.grade12textbooks.R

@Composable
fun BookListScreen(navController: NavController, viewModel: BookListViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 24.dp)) {
        Text(stringResource(R.string.books), style = MaterialTheme.typography.headlineMedium)
        when (val current = state) {
            BookListViewModel.State.Loading -> LoadingState()
            BookListViewModel.State.Empty -> EmptyState(stringResource(R.string.no_books))
            is BookListViewModel.State.Error -> ErrorState(
                message = stringResource(R.string.catalog_unavailable),
                onRetry = viewModel::retry
            )
            is BookListViewModel.State.Success -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(current.books, key = { it.id }) { book ->
                    BookCard(book.title, subtitle = book.author ?: book.language) {
                        navController.navigate("book/${book.id}")
                    }
                }
            }
        }
    }
}
