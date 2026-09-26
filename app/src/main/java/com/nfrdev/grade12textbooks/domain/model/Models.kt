package com.nfrdev.grade12textbooks.domain.model

enum class Stream { NATURAL_SCIENCE, SOCIAL_SCIENCE, COMMON }

data class Subject(val id: String, val name: String, val stream: Stream, val iconRes: Int? = null, val bookCount: Int)

data class Book(
    val id: String,
    val title: String,
    val author: String?,
    val stream: Stream,
    val subjectId: String,
    val description: String?,
    val coverUrl: String?,
    val pdfUrl: String,
    val checksumSha256: String,
    val fileSize: Long?,
    val language: String,
    val curriculumYear: String?,
    val addedAt: Long,
    val localPath: String? = null,
    val isDownloaded: Boolean = false,
    val downloadedAt: Long? = null,
    val lastOpenedAt: Long? = null,
    val isFavorite: Boolean = false,
    val isOrphaned: Boolean = false
)

data class Bookmark(val id: Long, val bookId: String, val page: Int, val note: String?, val createdAt: Long)
data class ReadingProgress(val bookId: String, val currentPage: Int, val totalPages: Int, val updatedAt: Long)
