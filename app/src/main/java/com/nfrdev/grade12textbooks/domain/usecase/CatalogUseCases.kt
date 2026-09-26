package com.nfrdev.grade12textbooks.domain.usecase

import com.nfrdev.grade12textbooks.domain.model.Book
import com.nfrdev.grade12textbooks.domain.model.Stream
import com.nfrdev.grade12textbooks.domain.model.Subject
import com.nfrdev.grade12textbooks.domain.repository.CatalogRepository
import com.nfrdev.grade12textbooks.domain.repository.CatalogResult
import javax.inject.Inject

class GetStreamsUseCase @Inject constructor() { operator fun invoke(): List<Stream> = Stream.entries }
class GetSubjectsUseCase @Inject constructor(private val repository: CatalogRepository) {
    suspend operator fun invoke(stream: Stream): CatalogResult<List<Subject>> = try { CatalogResult.Success(repository.getSubjects(stream)) } catch (_: Exception) { CatalogResult.Failure("catalog_read_failed") }
}
class GetBooksUseCase @Inject constructor(private val repository: CatalogRepository) {
    suspend operator fun invoke(stream: Stream, subjectId: String): CatalogResult<List<Book>> = try { CatalogResult.Success(repository.getBooks(stream, subjectId)) } catch (_: Exception) { CatalogResult.Failure("catalog_read_failed") }
}
class RefreshCatalogUseCase @Inject constructor(private val repository: CatalogRepository) {
    suspend operator fun invoke(): CatalogResult<Unit> = repository.refresh()
}
