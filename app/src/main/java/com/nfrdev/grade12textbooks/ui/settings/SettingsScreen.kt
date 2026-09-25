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

@Composable fun SettingsScreen() {
    var wifiOnly by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { wifiOnly = context.userPreferencesDataStore.data.first()[PreferenceKeys.wifiOnly] ?: true }
    Column(Modifier.padding(20.dp)) {
        Text(stringResource(R.string.settings))
        Switch(checked = wifiOnly, onCheckedChange = { wifiOnly = it; scope.launch { context.userPreferencesDataStore.edit { prefs -> prefs[PreferenceKeys.wifiOnly] = it } } })
        Text(stringResource(R.string.wifi_only))
    }
}
