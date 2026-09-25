package com.nfrdev.grade12textbooks.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalog_metadata")
data class CatalogMetadataEntity(@PrimaryKey val id: Int = 1, val catalogVersion: Long, val lastSyncedAt: Long)
