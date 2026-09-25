package com.nfrdev.grade12textbooks.ui.subjects

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
import androidx.compose.runtime.remember
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
fun SubjectsScreen(navController: NavController, viewModel: SubjectsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = LocalAppSnackbarHost.current
    val catalogError = stringResource(R.string.catalog_unavailable)
    LaunchedEffect(state) { if (state is SubjectsViewModel.State.Error) snackbar.showSnackbar(catalogError) }
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(stringResource(R.string.subjects))
        when (val current = state) {
            SubjectsViewModel.State.Loading -> LoadingState()
            SubjectsViewModel.State.Empty -> EmptyState(stringResource(R.string.no_subjects))
            is SubjectsViewModel.State.Error -> ErrorState(stringResource(R.string.generic_error))
            is SubjectsViewModel.State.Success -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(current.subjects) { subject -> SubjectCard(subject.name, subject.bookCount) { navController.navigate("books/${subject.stream.routeValue()}/${subject.id}") } }
            }
        }
    }
}

private fun com.nfrdev.grade12textbooks.domain.model.Stream.routeValue() = if (this == com.nfrdev.grade12textbooks.domain.model.Stream.SOCIAL_SCIENCE) "social_science" else "natural_science"
