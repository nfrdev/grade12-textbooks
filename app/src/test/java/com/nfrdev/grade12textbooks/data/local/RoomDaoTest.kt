package com.nfrdev.grade12textbooks.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nfrdev.grade12textbooks.data.local.entity.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlinx.coroutines.test.runTest

@RunWith(RobolectricTestRunner::class)
class RoomDaoTest {
    private lateinit var db: AppDatabase
    @Before fun setUp() { db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), AppDatabase::class.java).allowMainThreadQueries().build() }
    @After fun tearDown() { db.close() }
    private fun book(id: String, stream: String) = BookEntity(id, id, null, stream, "mathematics", null, null, "https://kehulum.com/bfile_asset/books_99/collection/grade-12-mathematics-new-curriculum--student-textbook-kehulumcom17599122086bb1.pdf", "a".repeat(64), null, "en", null, 1)

    @Test fun compositeSubjectLookupSeparatesStreams() = runTest {
        db.bookDao().upsertAll(listOf(book("ns", "natural_science"), book("ss", "social_science")))
        assertEquals(listOf("ns"), db.bookDao().getBySubject("natural_science", "mathematics").map { it.id })
        assertEquals(listOf("ss"), db.bookDao().getBySubject("social_science", "mathematics").map { it.id })
    }

    @Test fun bookDeleteCascadesBookmarksAndProgress() = runTest {
        db.bookDao().upsertAll(listOf(book("ns", "natural_science")))
        db.bookmarkDao().insert(BookmarkEntity(bookId = "ns", page = 2, note = null, createdAt = 1))
        db.progressDao().upsert(ProgressEntity("ns", 2, 10, 1))
        db.bookDao().deleteAll()
        assertEquals(emptyList<BookmarkEntity>(), db.bookmarkDao().forBook("ns"))
        assertEquals(null, db.progressDao().get("ns"))
    }
}
