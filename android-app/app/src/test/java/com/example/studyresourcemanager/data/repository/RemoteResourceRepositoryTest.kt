package com.example.studyresourcemanager.data.repository

import com.example.studyresourcemanager.data.remote.OpenLibraryApi
import com.example.studyresourcemanager.data.remote.OpenLibraryDocument
import com.example.studyresourcemanager.data.remote.OpenLibrarySearchResponse
import com.example.studyresourcemanager.data.remote.WikipediaApi
import com.example.studyresourcemanager.data.remote.WikipediaError
import com.example.studyresourcemanager.data.remote.WikipediaPage
import com.example.studyresourcemanager.data.remote.WikipediaQuery
import com.example.studyresourcemanager.data.remote.WikipediaSearchResponse
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.UnknownHostException

/**
 * Merge behaviour of [RemoteResourceRepository] against fake providers.
 *
 * The fakes replace Retrofit entirely, so the tests never touch the network and each
 * provider failure mode (thrown exception, empty body, HTTP 200 with an error field) is
 * reproduced deterministically.
 */
class RemoteResourceRepositoryTest {

    @Test
    fun returnsBookResultsWhenWikipediaFails() {
        runBlocking {
            val books = FakeOpenLibraryApi(response = booksOf("OL1W"))
            val articles = FakeWikipediaApi(failure = UnknownHostException("en.wikipedia.org"))

            val result = repository(books, articles).search("database")

            assertTrue(result is ResourceSearchResult.Success)
            assertEquals(listOf("openlibrary:OL1W"), idsOf(result))
            assertEquals(1, articles.callCount)
        }
    }

    @Test
    fun returnsArticleResultsWhenOpenLibraryFails() {
        runBlocking {
            val books = FakeOpenLibraryApi(failure = UnknownHostException("openlibrary.org"))
            val articles = FakeWikipediaApi(response = articlesOf(42L))

            val result = repository(books, articles).search("database")

            assertTrue(result is ResourceSearchResult.Success)
            assertEquals(listOf("wikipedia:42"), idsOf(result))
            assertEquals(1, books.callCount)
        }
    }

    @Test
    fun failsWhenOneProviderFailsAndTheOtherReturnsNothing() {
        runBlocking {
            val books = FakeOpenLibraryApi(failure = UnknownHostException("openlibrary.org"))
            val articles = FakeWikipediaApi(response = WikipediaSearchResponse(query = null))

            val result = repository(books, articles).search("database")

            assertTrue(result is ResourceSearchResult.Failure)
            assertEquals(FailureReason.OFFLINE, (result as ResourceSearchResult.Failure).reason)
            assertEquals(1, articles.callCount)
        }
    }

    @Test
    fun failsWhenOneProviderFailsAndTheOtherIsSkipped() {
        runBlocking {
            val books = FakeOpenLibraryApi(failure = UnknownHostException("openlibrary.org"))
            val articles = FakeWikipediaApi(response = articlesOf(42L))

            // limit = 1 leaves no slots for the article provider, so it is never queried.
            val result = repository(books, articles).search("database", limit = 1)

            assertEquals(0, articles.callCount)
            assertTrue(result is ResourceSearchResult.Failure)
            assertEquals(FailureReason.OFFLINE, (result as ResourceSearchResult.Failure).reason)
        }
    }

    @Test
    fun returnsEmptyWhenBothProvidersSucceedWithNoResults() {
        runBlocking {
            val books = FakeOpenLibraryApi(response = OpenLibrarySearchResponse(documents = emptyList()))
            val articles = FakeWikipediaApi(response = WikipediaSearchResponse(query = WikipediaQuery()))

            val result = repository(books, articles).search("database")

            assertEquals(ResourceSearchResult.Empty, result)
        }
    }

    @Test
    fun failsWhenWikipediaReturnsHttp200WithAnErrorField() {
        runBlocking {
            val books = FakeOpenLibraryApi(response = OpenLibrarySearchResponse(documents = emptyList()))
            val articles = FakeWikipediaApi(
                response = WikipediaSearchResponse(
                    error = WikipediaError(code = "badvalue", info = "Unrecognized value for parameter gsrlimit")
                )
            )

            val result = repository(books, articles).search("database")

            assertTrue(result is ResourceSearchResult.Failure)
            assertEquals(FailureReason.SERVER, (result as ResourceSearchResult.Failure).reason)
        }
    }

    @Test
    fun wikipediaErrorFieldDoesNotDiscardBookResults() {
        runBlocking {
            val books = FakeOpenLibraryApi(response = booksOf("OL2W"))
            val articles = FakeWikipediaApi(
                response = WikipediaSearchResponse(error = WikipediaError(code = "badvalue", info = null))
            )

            val result = repository(books, articles).search("database")

            assertTrue(result is ResourceSearchResult.Success)
            assertEquals(listOf("openlibrary:OL2W"), idsOf(result))
        }
    }

    @Test
    fun failsOfflineWhenBothProvidersFail() {
        runBlocking {
            val books = FakeOpenLibraryApi(failure = UnknownHostException("openlibrary.org"))
            val articles = FakeWikipediaApi(failure = UnknownHostException("en.wikipedia.org"))

            val result = repository(books, articles).search("database")

            assertTrue(result is ResourceSearchResult.Failure)
            assertEquals(FailureReason.OFFLINE, (result as ResourceSearchResult.Failure).reason)
        }
    }

    /** Guards the DTO mapping itself: the real body shape must populate [WikipediaSearchResponse.error]. */
    @Test
    fun gsonMapsTheWikipediaErrorBody() {
        val json = """{"batchcomplete":true,"error":{"code":"badvalue","info":"Unrecognized value for parameter gsrlimit"}}"""

        val response = Gson().fromJson(json, WikipediaSearchResponse::class.java)

        assertNotNull(response.error)
        assertEquals("badvalue", response.error?.code)
        assertNull(response.query)
    }

    // region helpers

    private fun repository(books: FakeOpenLibraryApi, articles: FakeWikipediaApi) =
        RemoteResourceRepository(openLibraryApi = books, wikipediaApi = articles)

    private fun idsOf(result: ResourceSearchResult): List<String> =
        (result as ResourceSearchResult.Success).resources.map { it.id }

    private fun booksOf(vararg workIds: String) = OpenLibrarySearchResponse(
        documents = workIds.map { workId ->
            OpenLibraryDocument(workKey = "/works/$workId", title = "Book $workId")
        }
    )

    private fun articlesOf(vararg pageIds: Long) = WikipediaSearchResponse(
        query = WikipediaQuery(
            pages = pageIds.map { pageId ->
                WikipediaPage(pageId = pageId, title = "Article $pageId", extract = "Summary $pageId")
            }
        )
    )

    /** Succeeds with [response], or throws [failure] to mimic a network/service error. */
    private class FakeOpenLibraryApi(
        private val response: OpenLibrarySearchResponse = OpenLibrarySearchResponse(),
        private val failure: Throwable? = null
    ) : OpenLibraryApi {
        var callCount = 0
            private set

        override suspend fun searchBooks(
            query: String,
            subject: String?,
            fields: String,
            limit: Int
        ): OpenLibrarySearchResponse {
            callCount++
            failure?.let { throw it }
            return response
        }
    }

    /** Succeeds with [response], or throws [failure] to mimic a network/service error. */
    private class FakeWikipediaApi(
        private val response: WikipediaSearchResponse = WikipediaSearchResponse(),
        private val failure: Throwable? = null
    ) : WikipediaApi {
        var callCount = 0
            private set

        override suspend fun searchArticles(query: String, limit: Int): WikipediaSearchResponse {
            callCount++
            failure?.let { throw it }
            return response
        }
    }

    // endregion
}
