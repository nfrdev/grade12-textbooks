package com.nfrdev.grade12textbooks.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.nfrdev.grade12textbooks.BuildConfig
import com.nfrdev.grade12textbooks.R
import com.nfrdev.grade12textbooks.util.PreferenceKeys
import com.nfrdev.grade12textbooks.util.userPreferencesDataStore
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.Button
import android.content.Intent
import android.net.Uri

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    var wifiOnly by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val update by viewModel.update.collectAsStateWithLifecycle()
    var disclaimerAccepted by remember { mutableStateOf(false) }
    var preferencesLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val preferences = context.userPreferencesDataStore.data.first()
        wifiOnly = preferences[PreferenceKeys.wifiOnly] ?: true
        disclaimerAccepted = preferences[PreferenceKeys.disclaimerAcceptedV1] ?: false
        preferencesLoaded = true
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 340.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineMedium)
        }
        item {
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.download_preferences), style = MaterialTheme.typography.titleLarge)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.wifi_only), modifier = Modifier.weight(1f))
                        Switch(
                            checked = wifiOnly,
                            enabled = preferencesLoaded,
                            onCheckedChange = { checked ->
                                wifiOnly = checked
                                scope.launch {
                                    context.userPreferencesDataStore.edit { preferences -> preferences[PreferenceKeys.wifiOnly] = checked }
                                }
                            },
                            modifier = Modifier.semantics {
                                contentDescription = context.getString(R.string.wifi_only)
                            }
                        )
                    }
                }
            }
        }
        item {
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.storage_and_app), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.storage_usage), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    HorizontalDivider()
                    Text(stringResource(R.string.app_version, BuildConfig.VERSION_NAME), style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = viewModel::checkForUpdates) { Text(stringResource(R.string.check_updates)) }
                    update?.let { info ->
                        Text(stringResource(R.string.update_available, info.versionName))
                        Button(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(info.downloadUrl))) }) {
                            Text(stringResource(R.string.download))
                        }
                    }
                }
            }
        }
    }

    if (preferencesLoaded && !disclaimerAccepted) androidx.compose.material3.AlertDialog(
        onDismissRequest = {}, title = { Text(stringResource(R.string.disclaimer_title)) },
        text = { Text(stringResource(R.string.disclaimer_text)) },
        confirmButton = {
            Button(onClick = {
                disclaimerAccepted = true
                scope.launch { context.userPreferencesDataStore.edit { it[PreferenceKeys.disclaimerAcceptedV1] = true } }
            }) { Text(stringResource(R.string.accept)) }
        }
    )
}
