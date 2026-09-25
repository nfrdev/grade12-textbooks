package com.nfrdev.grade12textbooks.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.nfrdev.grade12textbooks.data.local.entity.CatalogMetadataEntity

@Dao
interface CatalogMetadataDao {
    @Query("SELECT * FROM catalog_metadata WHERE id = 1") suspend fun get(): CatalogMetadataEntity?
    @Upsert suspend fun upsert(metadata: CatalogMetadataEntity)
    @Query("DELETE FROM catalog_metadata") suspend fun deleteAll()
}
