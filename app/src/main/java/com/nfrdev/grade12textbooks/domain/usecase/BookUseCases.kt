package com.nfrdev.grade12textbooks.domain.usecase

import com.nfrdev.grade12textbooks.domain.model.Book
import com.nfrdev.grade12textbooks.domain.repository.CatalogRepository
import javax.inject.Inject

class GetBookUseCase @Inject constructor(private val repository: CatalogRepository) {
    suspend operator fun invoke(bookId: String): Book? = repository.getBooks().firstOrNull { it.id == bookId }
}

class GetDownloadedBooksUseCase @Inject constructor(private val repository: CatalogRepository) {
    suspend operator fun invoke(): List<Book> = repository.getBooks().filter { it.isDownloaded }
}
