package com.nfrdev.grade12textbooks.domain.usecase

import android.content.Context
import androidx.room.withTransaction
import com.nfrdev.grade12textbooks.data.local.AppDatabase
import com.nfrdev.grade12textbooks.data.local.dao.BookLocalStateDao
import com.nfrdev.grade12textbooks.data.local.dao.BookmarkDao
import com.nfrdev.grade12textbooks.data.local.dao.ProgressDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class DeleteBookUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val localStateDao: BookLocalStateDao,
    private val bookmarkDao: BookmarkDao,
    private val progressDao: ProgressDao
) {
    suspend operator fun invoke(bookId: String) = withContext(Dispatchers.IO) {
        require(bookId.matches(Regex("[A-Za-z0-9._-]+")))
        val booksDir = File(context.getExternalFilesDir(null), "books")
        File(booksDir, "$bookId.pdf").delete()
        File(booksDir, "$bookId.pdf.part").delete()
        database.withTransaction {
            localStateDao.delete(bookId)
            bookmarkDao.deleteForBook(bookId)
            progressDao.delete(bookId)
        }
    }
}
