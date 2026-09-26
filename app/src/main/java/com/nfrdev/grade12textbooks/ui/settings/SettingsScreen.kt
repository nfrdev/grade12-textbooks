package com.nfrdev.grade12textbooks.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
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

@Composable fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    var wifiOnly by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val update by viewModel.update.collectAsStateWithLifecycle()
    var disclaimerAccepted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { wifiOnly = context.userPreferencesDataStore.data.first()[PreferenceKeys.wifiOnly] ?: true }
    Column(Modifier.padding(20.dp)) {
        Text(stringResource(R.string.settings))
        Switch(checked = wifiOnly, onCheckedChange = { wifiOnly = it; scope.launch { context.userPreferencesDataStore.edit { prefs -> prefs[PreferenceKeys.wifiOnly] = it } } })
        Text(stringResource(R.string.wifi_only))
        Button(onClick = viewModel::checkForUpdates) { Text(stringResource(R.string.check_updates)) }
        update?.let { info ->
            Text(stringResource(R.string.update_available, info.versionName))
            Button(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(info.downloadUrl))) }) { Text(stringResource(R.string.download)) }
        }
    }
    LaunchedEffect(Unit) { disclaimerAccepted = context.userPreferencesDataStore.data.first()[com.nfrdev.grade12textbooks.util.PreferenceKeys.disclaimerAcceptedV1] ?: false }
    if (!disclaimerAccepted) androidx.compose.material3.AlertDialog(
        onDismissRequest = {}, title = { Text(stringResource(R.string.disclaimer_title)) },
        text = { Text(stringResource(R.string.disclaimer_text)) },
        confirmButton = { Button(onClick = { disclaimerAccepted = true; scope.launch { context.userPreferencesDataStore.edit { it[com.nfrdev.grade12textbooks.util.PreferenceKeys.disclaimerAcceptedV1] = true } } }) { Text(stringResource(R.string.accept)) } }
    )
}
