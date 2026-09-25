package com.nfrdev.grade12textbooks.ui.books

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nfrdev.grade12textbooks.ui.components.*
import androidx.compose.ui.res.stringResource
import com.nfrdev.grade12textbooks.R
import com.nfrdev.grade12textbooks.ui.navigation.LocalAppSnackbarHost

@Composable
fun BookListScreen(navController: NavController, viewModel: BookListViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = LocalAppSnackbarHost.current
    val catalogError = stringResource(R.string.catalog_unavailable)
    LaunchedEffect(state) { if (state is BookListViewModel.State.Error) snackbar.showSnackbar(catalogError) }
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(stringResource(R.string.books))
        when (val current = state) {
            BookListViewModel.State.Loading -> LoadingState()
            BookListViewModel.State.Empty -> EmptyState(stringResource(R.string.no_books))
            is BookListViewModel.State.Error -> ErrorState(stringResource(R.string.generic_error))
            is BookListViewModel.State.Success -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(current.books) { book -> BookCard(book.title) { navController.navigate("book/${book.id}") } }
            }
        }
    }
}
