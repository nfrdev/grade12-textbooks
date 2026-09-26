package com.nfrdev.grade12textbooks.util

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")

object PreferenceKeys {
    val wifiOnly = booleanPreferencesKey("wifiOnly")
    val themeMode = stringPreferencesKey("themeMode")
    val readerScrollMode = stringPreferencesKey("readerScrollMode")
    fun readerZoom(bookId: String) = floatPreferencesKey("readerZoom:$bookId")
    fun readerNightMode(bookId: String) = booleanPreferencesKey("readerNightMode:$bookId")
    val librarySortOrder = stringPreferencesKey("librarySortOrder")
    val disclaimerAcceptedV1 = booleanPreferencesKey("disclaimerAcceptedV1")
}

class UserPreferencesRepository(private val context: Context) {
    val isWifiOnly: Flow<Boolean> = context.userPreferencesDataStore.data
        .map { preferences -> preferences[PreferenceKeys.wifiOnly] ?: true }

    fun getReaderZoom(bookId: String): Flow<Float> = context.userPreferencesDataStore.data
        .map { preferences -> preferences[PreferenceKeys.readerZoom(bookId)] ?: 1.0f }

    fun getReaderNightMode(bookId: String): Flow<Boolean> = context.userPreferencesDataStore.data
        .map { preferences -> preferences[PreferenceKeys.readerNightMode(bookId)] ?: false }

    suspend fun setWifiOnly(enabled: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferenceKeys.wifiOnly] = enabled
        }
    }

    suspend fun setReaderZoom(bookId: String, zoom: Float) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferenceKeys.readerZoom(bookId)] = zoom
        }
    }

    suspend fun setReaderNightMode(bookId: String, nightMode: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[PreferenceKeys.readerNightMode(bookId)] = nightMode
        }
    }
}
