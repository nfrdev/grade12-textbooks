package com.nfrdev.grade12textbooks.util

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

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
