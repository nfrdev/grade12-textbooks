package com.nfrdev.grade12textbooks.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.nfrdev.grade12textbooks.BuildConfig
import com.nfrdev.grade12textbooks.data.remote.api.CatalogApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton
import com.nfrdev.grade12textbooks.data.remote.CatalogParser
import com.nfrdev.grade12textbooks.domain.repository.CatalogRepository
import com.nfrdev.grade12textbooks.data.repository.RoomCatalogRepository

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton fun json(): Json = Json { ignoreUnknownKeys = false; isLenient = false }
    @Provides @Singleton fun okHttpClient(): OkHttpClient = OkHttpClient.Builder().followRedirects(true).build()
    @Provides @Singleton fun retrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.CATALOG_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
    @Provides @Singleton fun catalogApi(retrofit: Retrofit): CatalogApi = retrofit.create(CatalogApi::class.java)
    @Provides @Singleton fun catalogParser(json: Json, logger: com.nfrdev.grade12textbooks.util.Logger): CatalogParser = CatalogParser(json, logger)
    @Provides @Singleton fun catalogRepository(repository: RoomCatalogRepository): CatalogRepository = repository
}
