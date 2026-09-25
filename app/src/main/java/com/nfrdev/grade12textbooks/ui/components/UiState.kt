package com.nfrdev.grade12textbooks.ui.components

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable fun LoadingState() { CircularProgressIndicator() }
@Composable fun EmptyState(message: String) { Text(message) }
@Composable fun ErrorState(message: String) { Text(message) }
