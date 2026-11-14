package com.example.studyresourcemanager.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * One Retrofit/OkHttp setup shared by every remote catalogue.
 *
 * Both endpoints are public and key-less, so no credential is stored in source.
 * A descriptive User-Agent is mandatory: Wikimedia answers HTTP 403 to clients that do
 * not identify themselves, and Open Library asks heavy clients to be identifiable.
 */
internal object RetrofitClient {

    private const val OPEN_LIBRARY_BASE_URL = "https://openlibrary.org/"
    private const val WIKIPEDIA_BASE_URL = "https://en.wikipedia.org/"
    private const val USER_AGENT = "StudyResourceManager/1.0 (Android student project)"

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            chain.proceed(
                chain.request()
                    .newBuilder()
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "application/json")
                    .build()
            )
        }
        // BASIC logs method, URL, status and timing only: useful for the demo/viva and
        // safe to leave enabled (no response bodies, no headers, nothing sensitive).
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    val openLibrary: OpenLibraryApi = retrofit(OPEN_LIBRARY_BASE_URL).create(OpenLibraryApi::class.java)
    val wikipedia: WikipediaApi = retrofit(WIKIPEDIA_BASE_URL).create(WikipediaApi::class.java)

    private fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private const val CONNECT_TIMEOUT_SECONDS = 15L
    private const val READ_TIMEOUT_SECONDS = 20L
    private const val CALL_TIMEOUT_SECONDS = 25L
}
