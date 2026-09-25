package com.nfrdev.grade12textbooks.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CatalogDto(val schemaVersion: Int, val version: Long, val generatedAt: String, val books: List<BookDto>)

@Serializable
data class BookDto(
    val id: String, val title: String, val author: String? = null, val stream: String,
    val subjectId: String, val description: String? = null, val coverUrl: String? = null,
    val pdfUrl: String, val checksumSha256: String, val fileSize: Long? = null,
    val language: String, val curriculumYear: String? = null, val addedAt: String
)

@Serializable
data class VersionDto(val latestVersionCode: Int, val latestVersionName: String, val downloadUrl: String, val releaseNotes: String)
