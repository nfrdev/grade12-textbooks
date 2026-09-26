package com.nfrdev.grade12textbooks.data.local

import com.nfrdev.grade12textbooks.data.local.entity.BookEntity
import com.nfrdev.grade12textbooks.data.local.entity.BookLocalStateEntity
import com.nfrdev.grade12textbooks.domain.model.Book
import com.nfrdev.grade12textbooks.domain.model.Stream

private fun String.toDomainStream(): Stream = when (this) {
    "natural_science" -> Stream.NATURAL_SCIENCE
    "social_science" -> Stream.SOCIAL_SCIENCE
    "common" -> Stream.COMMON
    else -> error("Unknown stream")
}

fun BookEntity.toDomain(local: BookLocalStateEntity?): Book = Book(
    id = id, title = title, author = author, stream = stream.toDomainStream(), subjectId = subjectId,
    description = description, coverUrl = coverUrl, pdfUrl = pdfUrl, checksumSha256 = checksumSha256,
    fileSize = fileSize, language = language, curriculumYear = curriculumYear, addedAt = addedAt,
    localPath = local?.localPath, isDownloaded = local?.isDownloaded ?: false,
    downloadedAt = local?.downloadedAt, lastOpenedAt = local?.lastOpenedAt,
    isFavorite = local?.isFavorite ?: false, isOrphaned = local?.isOrphaned ?: false
)
