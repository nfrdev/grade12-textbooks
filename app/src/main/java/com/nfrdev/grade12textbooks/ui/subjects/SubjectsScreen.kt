package com.nfrdev.grade12textbooks.ui.subjects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nfrdev.grade12textbooks.ui.components.*
import androidx.compose.ui.res.stringResource
import com.nfrdev.grade12textbooks.R

@Composable
fun SubjectsScreen(navController: NavController, viewModel: SubjectsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 24.dp)) {
        Text(stringResource(R.string.subjects), style = MaterialTheme.typography.headlineMedium)
        when (val current = state) {
            SubjectsViewModel.State.Loading -> LoadingState()
            SubjectsViewModel.State.Empty -> EmptyState(stringResource(R.string.no_subjects))
            is SubjectsViewModel.State.Error -> ErrorState(
                message = stringResource(R.string.catalog_unavailable),
                onRetry = viewModel::retry
            )
            is SubjectsViewModel.State.Success -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 280.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(current.subjects, key = { "${it.stream}-${it.id}" }) { subject ->
                    SubjectCard(subject.name, subject.bookCount) {
                        navController.navigate("books/${subject.stream.routeValue()}/${subject.id}")
                    }
                }
            }
        }
    }
}

private fun com.nfrdev.grade12textbooks.domain.model.Stream.routeValue() = when (this) {
    com.nfrdev.grade12textbooks.domain.model.Stream.NATURAL_SCIENCE -> "natural_science"
    com.nfrdev.grade12textbooks.domain.model.Stream.SOCIAL_SCIENCE -> "social_science"
    com.nfrdev.grade12textbooks.domain.model.Stream.COMMON -> "common"
}
