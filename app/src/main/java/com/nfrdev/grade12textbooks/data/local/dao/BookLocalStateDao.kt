package com.nfrdev.grade12textbooks.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.nfrdev.grade12textbooks.data.local.entity.BookLocalStateEntity

@Dao
interface BookLocalStateDao {
    @Query("SELECT * FROM book_local_state") suspend fun getAll(): List<BookLocalStateEntity>
    @Query("SELECT * FROM book_local_state WHERE bookId = :bookId") suspend fun get(bookId: String): BookLocalStateEntity?
    @Upsert suspend fun upsert(state: BookLocalStateEntity)
    @Query("UPDATE book_local_state SET isOrphaned = 1 WHERE bookId NOT IN (:activeIds)") suspend fun markOrphans(activeIds: List<String>)
    @Query("UPDATE book_local_state SET isOrphaned = 0 WHERE bookId IN (:activeIds)") suspend fun clearOrphans(activeIds: List<String>)
    @Query("DELETE FROM book_local_state") suspend fun deleteAll()
}
