package com.nfrdev.grade12textbooks.data.download

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Transaction

@Dao
interface DownloadSlotDao {
    @Query("SELECT COUNT(*) FROM download_slots") suspend fun activeCount(): Int
    @Insert(onConflict = androidx.room.OnConflictStrategy.IGNORE) suspend fun insert(slot: DownloadSlotEntity): Long
    @Transaction
    suspend fun tryClaim(bookId: String, claimedAt: Long): Long {
        if (activeCount() >= 2) return -1L
        return insert(DownloadSlotEntity(bookId, claimedAt))
    }
    @Query("SELECT COUNT(*) FROM download_slots WHERE bookId = :bookId") suspend fun isClaimed(bookId: String): Int
    @Query("DELETE FROM download_slots WHERE bookId = :bookId") suspend fun release(bookId: String)
    @Query("DELETE FROM download_slots") suspend fun releaseAll()
}
