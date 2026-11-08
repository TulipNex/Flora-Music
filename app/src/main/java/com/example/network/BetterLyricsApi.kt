package com.example.network

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response

interface BetterLyricsApi {
    @GET("getLyrics")
    suspend fun searchLyrics(
        @Query("s") title: String,
        @Query("a") artist: String,
        @Query("al") album: String? = null,
        @Query("d") durationSec: Int? = null
    ): Response<BetterLyricsResponse>
}

@JsonClass(generateAdapter = true)
data class BetterLyricsResponse(
    val ttml: String?
)
