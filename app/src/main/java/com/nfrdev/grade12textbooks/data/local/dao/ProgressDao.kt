package com.nfrdev.grade12textbooks.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.nfrdev.grade12textbooks.data.local.entity.ProgressEntity

@Dao
interface ProgressDao {
    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId") suspend fun get(bookId: String): ProgressEntity?
    @Upsert suspend fun upsert(progress: ProgressEntity)
    @Query("DELETE FROM reading_progress WHERE bookId = :bookId") suspend fun delete(bookId: String)
}
