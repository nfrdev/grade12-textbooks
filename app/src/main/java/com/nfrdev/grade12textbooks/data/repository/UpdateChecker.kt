package com.nfrdev.grade12textbooks.data.repository

import com.nfrdev.grade12textbooks.BuildConfig
import com.nfrdev.grade12textbooks.data.remote.api.CatalogApi
import com.nfrdev.grade12textbooks.data.remote.dto.VersionDto
import com.nfrdev.grade12textbooks.util.Logger
import kotlinx.serialization.json.Json
import java.net.URI
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class UpdateInfo(val versionCode: Int, val versionName: String, val downloadUrl: String, val releaseNotes: String)

@Singleton
class UpdateChecker @Inject constructor(private val api: CatalogApi, private val json: Json, private val logger: Logger) {
    suspend fun check(): UpdateInfo? = try {
        val dto = json.decodeFromString<VersionDto>(api.getVersion(BuildConfig.CATALOG_BASE_URL + "version.json").string())
        if (dto.latestVersionCode <= BuildConfig.VERSION_CODE || !validUrl(dto.downloadUrl)) null
        else UpdateInfo(dto.latestVersionCode, dto.latestVersionName, dto.downloadUrl, dto.releaseNotes)
    } catch (e: Exception) { logger.e("Update check failed", e); null }
    private fun validUrl(value: String): Boolean = try { URI(value).scheme?.lowercase(Locale.US) == "https" || (BuildConfig.DEBUG && URI(value).scheme == "http") } catch (_: Exception) { false }
}
