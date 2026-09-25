package com.nfrdev.grade12textbooks.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.nfrdev.grade12textbooks.R

@Composable fun StreamCard(label: String, onClick: () -> Unit) = Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) { Text(label, Modifier.padding(28.dp)) }
@Composable fun SubjectCard(name: String, count: Int, onClick: () -> Unit) = Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) { Text(stringResource(R.string.subject_count, name, count), Modifier.padding(16.dp)) }
@Composable fun BookCard(title: String, onClick: () -> Unit) = Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) { Text(title, Modifier.padding(16.dp)) }
