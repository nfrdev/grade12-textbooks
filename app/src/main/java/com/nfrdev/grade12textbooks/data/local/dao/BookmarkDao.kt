package com.nfrdev.grade12textbooks.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Delete
import com.nfrdev.grade12textbooks.data.local.entity.BookmarkEntity

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks WHERE bookId = :bookId ORDER BY page ASC, createdAt ASC") suspend fun forBook(bookId: String): List<BookmarkEntity>
    @Insert suspend fun insert(bookmark: BookmarkEntity): Long
    @Delete suspend fun delete(bookmark: BookmarkEntity)
    @Query("DELETE FROM bookmarks WHERE bookId = :bookId") suspend fun deleteForBook(bookId: String)
}
