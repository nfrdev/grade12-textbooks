package com.nfrdev.grade12textbooks.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book_local_state")
data class BookLocalStateEntity(
    @PrimaryKey val bookId: String,
    val localPath: String? = null,
    val isDownloaded: Boolean = false,
    val downloadedAt: Long? = null,
    val lastOpenedAt: Long? = null,
    val isFavorite: Boolean = false,
    val isOrphaned: Boolean = false
)
