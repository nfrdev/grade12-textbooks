package com.nfrdev.grade12textbooks.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "reading_progress",
    primaryKeys = ["bookId"],
    foreignKeys = [ForeignKey(entity = BookEntity::class, parentColumns = ["id"], childColumns = ["bookId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("bookId")]
)
data class ProgressEntity(val bookId: String, val currentPage: Int, val totalPages: Int, val updatedAt: Long)
