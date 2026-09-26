package com.nfrdev.grade12textbooks.domain

import com.nfrdev.grade12textbooks.FakeCatalogRepository
import com.nfrdev.grade12textbooks.domain.model.Book
import com.nfrdev.grade12textbooks.domain.model.Stream
import com.nfrdev.grade12textbooks.domain.repository.CatalogResult
import com.nfrdev.grade12textbooks.domain.usecase.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class UseCaseTest {
    @Test fun streamsIncludeCommonSubjects() { assertEquals(3, GetStreamsUseCase()().size) }
    @Test fun booksUseCompositeSubjectKey() = runTest {
        val repo = FakeCatalogRepository().apply {
            books += Book("ns", "NS Math", null, Stream.NATURAL_SCIENCE, "mathematics", null, null, "https://kehulum.com/bfile_asset/books_99/collection/grade-12-mathematics-new-curriculum--student-textbook-kehulumcom17599122086bb1.pdf", "a".repeat(64), null, "en", null, 1)
            books += Book("ss", "SS Math", null, Stream.SOCIAL_SCIENCE, "mathematics", null, null, "https://kehulum.com/bfile_asset/books_99/collection/grade-12-mathematics-new-curriculum--student-textbook-kehulumcom17599122086bb1.pdf", "b".repeat(64), null, "en", null, 1)
        }
        val result = GetBooksUseCase(repo)(Stream.NATURAL_SCIENCE, "mathematics") as CatalogResult.Success
        assertEquals(listOf("ns"), result.value.map { it.id })
    }
    @Test fun failuresBecomeResult() = runTest {
        val repo = FakeCatalogRepository().apply { shouldFail = true }
        assertTrue(RefreshCatalogUseCase(repo)() is CatalogResult.Failure)
    }
}
