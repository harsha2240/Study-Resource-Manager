package com.example.studyresourcemanager.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Open Library search API. No API key is required.
 *
 * `q` is a full-text query that covers title, author and subject text (Open Library's
 * documented behaviour), so the unified search box works for "clean code" (title),
 * "Kurose" (author) and "computer networks" (subject). The optional `subject` filter is
 * left available for callers that want to narrow results further.
 */
internal interface OpenLibraryApi {

    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("subject") subject: String?,
        @Query("fields") fields: String,
        @Query("limit") limit: Int
    ): OpenLibrarySearchResponse

    companion object {
        /**
         * Asked for explicitly: without it the endpoint returns every indexed field
         * (including the full ISBN list) which makes responses several times larger.
         */
        const val SEARCH_FIELDS =
            "key,title,author_name,first_publish_year,edition_count,cover_i,language,subject"
    }
}
