package com.nfrdev.grade12textbooks.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nfrdev.grade12textbooks.data.local.dao.BookDao
import com.nfrdev.grade12textbooks.data.local.dao.CatalogMetadataDao
import com.nfrdev.grade12textbooks.data.local.entity.BookEntity
import com.nfrdev.grade12textbooks.data.local.entity.CatalogMetadataEntity
import com.nfrdev.grade12textbooks.data.local.entity.BookLocalStateEntity
import com.nfrdev.grade12textbooks.data.local.entity.BookmarkEntity
import com.nfrdev.grade12textbooks.data.local.entity.ProgressEntity
import com.nfrdev.grade12textbooks.data.local.dao.BookLocalStateDao
import com.nfrdev.grade12textbooks.data.local.dao.BookmarkDao
import com.nfrdev.grade12textbooks.data.local.dao.ProgressDao
import com.nfrdev.grade12textbooks.data.download.DownloadSlotEntity
import com.nfrdev.grade12textbooks.data.download.DownloadSlotDao

@Database(
    entities = [BookEntity::class, BookLocalStateEntity::class, CatalogMetadataEntity::class, BookmarkEntity::class, ProgressEntity::class, DownloadSlotEntity::class],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun bookLocalStateDao(): BookLocalStateDao
    abstract fun catalogMetadataDao(): CatalogMetadataDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun progressDao(): ProgressDao
    abstract fun downloadSlotDao(): DownloadSlotDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "grade12.db"
            ).build().also { instance = it }
        }
    }
}
