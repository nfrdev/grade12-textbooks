package com.nfrdev.grade12textbooks.data.local.entity

import androidx.room.Entity

@Entity(tableName = "books", primaryKeys = ["id"])
data class BookEntity(
    val id: String, val title: String, val stream: String, val subjectId: String,
    val pdfUrl: String, val checksumSha256: String, val addedAt: Long = 0L
)
