package com.nfrdev.grade12textbooks.data.download

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "download_slots")
data class DownloadSlotEntity(@PrimaryKey val bookId: String, val claimedAt: Long)
