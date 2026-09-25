package com.nfrdev.grade12textbooks.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Url
import okhttp3.ResponseBody

interface CatalogApi {
    @GET suspend fun getCatalog(@Url url: String): ResponseBody
    @GET suspend fun getVersion(@Url url: String): ResponseBody
}
