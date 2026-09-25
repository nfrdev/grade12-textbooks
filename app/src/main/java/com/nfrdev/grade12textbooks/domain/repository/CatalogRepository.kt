package com.nfrdev.grade12textbooks.domain.repository

import com.nfrdev.grade12textbooks.domain.model.Book
import com.nfrdev.grade12textbooks.domain.model.Stream
import com.nfrdev.grade12textbooks.domain.model.Subject

sealed interface CatalogResult<out T> {
    data class Success<T>(val value: T) : CatalogResult<T>
    data class Failure(val code: String) : CatalogResult<Nothing>
}

interface CatalogRepository {
    suspend fun refresh(): CatalogResult<Unit>
    suspend fun getBooks(): List<Book>
    suspend fun getBooks(stream: Stream, subjectId: String): List<Book>
    suspend fun getSubjects(stream: Stream): List<Subject>
}
