package com.nfrdev.grade12textbooks.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nfrdev.grade12textbooks.R
import com.nfrdev.grade12textbooks.ui.components.StreamCard
import com.nfrdev.grade12textbooks.BuildConfig
import com.nfrdev.grade12textbooks.data.local.AppDatabase
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(onStreamSelected: (String) -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var debugOpen by remember { mutableStateOf(false) }
    val streams = listOf(
        stringResource(R.string.natural_science) to "natural_science",
        stringResource(R.string.social_science) to "social_science",
        stringResource(R.string.common_subjects) to "common"
    )

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 280.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        stringResource(R.string.home_eyebrow),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        stringResource(R.string.home_headline),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        stringResource(R.string.home_description),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        stringResource(R.string.home_stream_heading),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        items(streams, key = { it.second }) { (label, route) ->
            StreamCard(label = label, onClick = { onStreamSelected(route) })
        }
    }

    if (debugOpen) AlertDialog(onDismissRequest = { debugOpen = false }, title = { Text(stringResource(R.string.debug_menu)) },
        text = { Column { TextButton(onClick = { scope.launch(kotlinx.coroutines.Dispatchers.IO) { AppDatabase.getInstance(context).clearAllTables() }; debugOpen = false }) { Text(stringResource(R.string.reset_catalog_cache)) } } },
        confirmButton = { TextButton(onClick = { debugOpen = false }) { Text(stringResource(R.string.ok)) } })
}
