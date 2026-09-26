package com.nfrdev.grade12textbooks.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.nfrdev.grade12textbooks.data.local.AppDatabase
import com.nfrdev.grade12textbooks.BuildConfig
import com.nfrdev.grade12textbooks.data.local.toDomain
import com.nfrdev.grade12textbooks.data.local.dao.BookDao
import com.nfrdev.grade12textbooks.data.local.dao.BookLocalStateDao
import com.nfrdev.grade12textbooks.data.local.dao.CatalogMetadataDao
import com.nfrdev.grade12textbooks.data.remote.CatalogParseResult
import com.nfrdev.grade12textbooks.data.remote.CatalogParser
import com.nfrdev.grade12textbooks.data.remote.api.CatalogApi
import com.nfrdev.grade12textbooks.domain.model.Book
import com.nfrdev.grade12textbooks.domain.model.Stream
import com.nfrdev.grade12textbooks.domain.model.Subject
import com.nfrdev.grade12textbooks.domain.repository.CatalogRepository
import com.nfrdev.grade12textbooks.domain.repository.CatalogResult
import com.nfrdev.grade12textbooks.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomCatalogRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: CatalogApi,
    private val parser: CatalogParser,
    private val db: AppDatabase,
    private val books: BookDao,
    private val states: BookLocalStateDao,
    private val metadata: CatalogMetadataDao,
    private val logger: Logger
) : CatalogRepository {
    override suspend fun refresh(): CatalogResult<Unit> = withContext(Dispatchers.IO) {
        val remote = runCatching { api.getCatalog(BuildConfig.CATALOG_BASE_URL + "catalog.json").string() }
            .mapCatching { parser.parse(it) }.getOrNull()
        if (remote is CatalogParseResult.Success) {
            val cachedVersion = metadata.get()?.catalogVersion ?: -1L
            if (remote.version > cachedVersion) replaceAtomically(remote)
            return@withContext CatalogResult.Success(Unit)
        }
        if (books.getAll().isNotEmpty()) return@withContext CatalogResult.Success(Unit)
        val bundled = runCatching { context.assets.open("catalog.json").bufferedReader().use { it.readText() } }
            .mapCatching { parser.parse(it) }.getOrNull()
        if (bundled is CatalogParseResult.Success && books.getAll().isEmpty()) {
            replaceAtomically(bundled)
            CatalogResult.Success(Unit)
        } else CatalogResult.Failure("catalog_unavailable")
    }

    private suspend fun replaceAtomically(catalog: CatalogParseResult.Success) {
        val ids = catalog.books.map { it.id }
        val previousIds = books.getAll().map { it.id }
        db.withTransaction {
            books.upsertAll(catalog.books)
            val existingStates = states.getAll().associateBy { it.bookId }
            previousIds.filterNot { it in ids }.forEach { id ->
                states.upsert(existingStates[id]?.copy(isOrphaned = true) ?: com.nfrdev.grade12textbooks.data.local.entity.BookLocalStateEntity(id, isOrphaned = true))
            }
            if (ids.isNotEmpty()) states.clearOrphans(ids)
            metadata.upsert(com.nfrdev.grade12textbooks.data.local.entity.CatalogMetadataEntity(1, catalog.version, System.currentTimeMillis()))
        }
    }

    override suspend fun getBooks(): List<Book> = withContext(Dispatchers.IO) {
        val local = states.getAll().associateBy { it.bookId }
        books.getAll().map { it.toDomain(local[it.id]) }
    }

    override suspend fun getBooks(stream: Stream, subjectId: String): List<Book> = withContext(Dispatchers.IO) {
        val key = when (stream) {
            Stream.NATURAL_SCIENCE -> "natural_science"
            Stream.SOCIAL_SCIENCE -> "social_science"
            Stream.COMMON -> "common"
        }
        val local = states.getAll().associateBy { it.bookId }
        books.getBySubject(key, subjectId).map { it.toDomain(local[it.id]) }
    }

    override suspend fun getSubjects(stream: Stream): List<Subject> {
        val books = getBooks().filter { it.stream == stream }
        return books.groupBy { it.subjectId }.map { (id, values) ->
            Subject(id, id.replaceFirstChar { it.uppercase() }, stream, bookCount = values.size)
        }.sortedBy { it.name }
    }
}
