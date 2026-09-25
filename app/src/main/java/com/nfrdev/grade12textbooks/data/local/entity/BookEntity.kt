package com.nfrdev.grade12textbooks.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "books",
    primaryKeys = ["id"],
    indices = [Index("stream"), Index("subjectId"), Index(value = ["stream", "subjectId"]), Index("addedAt")]
)
data class BookEntity(
    val id: String,
    val title: String,
    val author: String?,
    val stream: String,
    val subjectId: String,
    val description: String?,
    val coverUrl: String?,
    val pdfUrl: String,
    val checksumSha256: String,
    val fileSize: Long?,
    val language: String,
    val curriculumYear: String?,
    val addedAt: Long
)
