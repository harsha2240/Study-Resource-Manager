package com.example.studyresourcemanager.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Wikipedia (MediaWiki) search API. No API key is required.
 *
 * Only the search term and the result count vary between calls, so the fixed MediaWiki
 * parameters live in the URL:
 *  - `generator=search` turns the search into a list of page objects
 *  - `prop=extracts|pageimages|info` asks for the intro text, a thumbnail and the URL
 *  - `exintro=1&explaintext=1` returns the plain-text lead section (no HTML)
 *  - `formatversion=2` returns `query.pages` as a JSON array instead of a keyed object
 *
 * Wikimedia rejects requests without a descriptive User-Agent; see [RetrofitClient].
 */
internal interface WikipediaApi {

    @GET("w/api.php?action=query&generator=search&prop=extracts%7Cpageimages%7Cinfo&exintro=1&explaintext=1&inprop=url&pithumbsize=320&format=json&formatversion=2")
    suspend fun searchArticles(
        @Query("gsrsearch") query: String,
        @Query("gsrlimit") limit: Int
    ): WikipediaSearchResponse
}
