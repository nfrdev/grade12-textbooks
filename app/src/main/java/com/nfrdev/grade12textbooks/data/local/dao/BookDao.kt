package com.nfrdev.grade12textbooks.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.nfrdev.grade12textbooks.data.local.entity.BookEntity

@Dao
interface BookDao {
    @Query("SELECT * FROM books") suspend fun getAll(): List<BookEntity>
    // subjectId is scoped to stream; never query it alone.
    @Query("SELECT * FROM books WHERE stream = :stream AND subjectId = :subjectId")
    suspend fun getBySubject(stream: String, subjectId: String): List<BookEntity>
    @Query("DELETE FROM books") suspend fun deleteAll()
}
