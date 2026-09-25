package com.nfrdev.grade12textbooks.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nfrdev.grade12textbooks.data.local.dao.BookDao
import com.nfrdev.grade12textbooks.data.local.dao.CatalogMetadataDao
import com.nfrdev.grade12textbooks.data.local.entity.BookEntity
import com.nfrdev.grade12textbooks.data.local.entity.CatalogMetadataEntity

@Database(entities = [BookEntity::class, CatalogMetadataEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun catalogMetadataDao(): CatalogMetadataDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(context: android.content.Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: androidx.room.Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "grade12.db"
                ).build().also { INSTANCE = it }
            }
    }
}
