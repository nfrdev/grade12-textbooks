package com.nfrdev.grade12textbooks.data.local.entity

import androidx.room.Entity

@Entity(tableName = "catalog_metadata")
data class CatalogMetadataEntity(val id: Int = 1, val catalogVersion: Long, val lastSyncedAt: Long)
