package com.nfrdev.grade12textbooks.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.AlertDialog
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
    var debugOpen by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
        StreamCard(stringResource(R.string.natural_science)) { onStreamSelected("natural_science") }
        StreamCard(stringResource(R.string.social_science)) { onStreamSelected("social_science") }
        if (BuildConfig.DEBUG) Text(stringResource(R.string.debug_menu), Modifier.combinedClickable(onClick = {}, onLongClick = { debugOpen = true }))
    }
    if (debugOpen) AlertDialog(onDismissRequest = { debugOpen = false }, title = { Text(stringResource(R.string.debug_menu)) },
        text = { Column { TextButton(onClick = { kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch { AppDatabase.getInstance(context).clearAllTables() }; debugOpen = false }) { Text(stringResource(R.string.reset_catalog_cache)) } } },
        confirmButton = { TextButton(onClick = { debugOpen = false }) { Text(stringResource(R.string.ok)) } })
}
