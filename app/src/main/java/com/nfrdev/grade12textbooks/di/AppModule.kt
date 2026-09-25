package com.nfrdev.grade12textbooks.di

import android.content.Context
import androidx.room.Room
import com.nfrdev.grade12textbooks.data.local.AppDatabase
import com.nfrdev.grade12textbooks.data.local.dao.BookDao
import com.nfrdev.grade12textbooks.data.local.dao.CatalogMetadataDao
import com.nfrdev.grade12textbooks.data.local.dao.BookLocalStateDao
import com.nfrdev.grade12textbooks.data.local.dao.BookmarkDao
import com.nfrdev.grade12textbooks.data.local.dao.ProgressDao
import com.nfrdev.grade12textbooks.util.CrashReporter
import com.nfrdev.grade12textbooks.util.Logger
import com.nfrdev.grade12textbooks.util.NoOpCrashReporter
import com.nfrdev.grade12textbooks.util.TimberLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton fun database(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "grade12.db").build()
    @Provides fun bookDao(db: AppDatabase): BookDao = db.bookDao()
    @Provides fun bookLocalStateDao(db: AppDatabase): BookLocalStateDao = db.bookLocalStateDao()
    @Provides fun catalogMetadataDao(db: AppDatabase): CatalogMetadataDao = db.catalogMetadataDao()
    @Provides fun bookmarkDao(db: AppDatabase): BookmarkDao = db.bookmarkDao()
    @Provides fun progressDao(db: AppDatabase): ProgressDao = db.progressDao()
    @Provides @Singleton fun logger(): Logger = TimberLogger()
    @Provides @Singleton fun crashReporter(): CrashReporter = NoOpCrashReporter()
}
