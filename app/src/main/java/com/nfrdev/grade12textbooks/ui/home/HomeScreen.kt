package com.nfrdev.grade12textbooks.ui.home

import android.content.Context
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nfrdev.grade12textbooks.BuildConfig
import com.nfrdev.grade12textbooks.R
import com.nfrdev.grade12textbooks.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var debugOpen by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
        StreamCard(R.string.natural_science, Modifier) { }
        StreamCard(R.string.social_science, Modifier) { }
        if (BuildConfig.DEBUG) {
            Text(
                text = stringResource(R.string.debug_menu),
                modifier = Modifier.combinedClickable(onClick = {}, onLongClick = { debugOpen = true })
            )
        }
    }
    if (debugOpen) {
        AlertDialog(
            onDismissRequest = { debugOpen = false },
            title = { Text(stringResource(R.string.debug_menu)) },
            text = {
                Column {
                    TextButton(onClick = {
                        scope.launch(Dispatchers.IO) {
                            val db = AppDatabase.getInstance(context)
                            db.clearAllTables()
                            message = true
                        }
                        debugOpen = false
                    }) { Text(stringResource(R.string.reset_catalog_cache)) }
                    TextButton(onClick = {
                        context.getExternalFilesDir(null)?.resolve("books")?.deleteRecursively()
                        scope.launch(Dispatchers.IO) { AppDatabase.getInstance(context).clearAllTables(); message = true }
                        debugOpen = false
                    }) { Text(stringResource(R.string.wipe_downloads)) }
                }
            },
            confirmButton = { TextButton(onClick = { debugOpen = false }) { Text(stringResource(R.string.ok)) } }
        )
    }
    if (message) { LaunchedEffect(Unit) { kotlinx.coroutines.delay(1200); message = false } }
}

@Composable
private fun StreamCard(labelRes: Int, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.fillMaxWidth()) { Text(stringResource(labelRes), Modifier.padding(28.dp), style = MaterialTheme.typography.titleLarge) }
}
