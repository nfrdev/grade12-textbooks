package com.nfrdev.grade12textbooks.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppSnackbarHost = staticCompositionLocalOf<SnackbarHostState> { error("SnackbarHostState missing") }
