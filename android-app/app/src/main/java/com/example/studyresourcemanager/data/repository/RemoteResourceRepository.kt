package com.example.studyresourcemanager.data.repository

import com.example.studyresourcemanager.data.SampleStudyRepository
import com.example.studyresourcemanager.data.StudyResource
import com.example.studyresourcemanager.data.remote.OpenLibraryApi
import com.example.studyresourcemanager.data.remote.RetrofitClient
import com.example.studyresourcemanager.data.remote.SubjectMapper
import com.example.studyresourcemanager.data.remote.WikipediaApi
import com.example.studyresourcemanager.data.remote.toStudyResource
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Searches both public catalogues in parallel and merges the results.
 *
 * Behaviour that matters for the UI:
 *  - one service failing does not fail the whole search (books still show if Wikipedia is down);
 *  - the returned list is de-duplicated by [StudyResource.id] and capped at the requested limit;
 *  - cancellation (screen left, ViewModel cleared) always propagates instead of turning
 *    into a Failure.
 *
 * No API key is used and nothing is persisted here; bookmarks belong to the Room layer.
 */
class RemoteResourceRepository internal constructor(
    private val openLibraryApi: OpenLibraryApi = RetrofitClient.openLibrary,
    private val wikipediaApi: WikipediaApi = RetrofitClient.wikipedia
) : ResourceRepository {

    override suspend fun search(query: String, subject: String?, limit: Int): ResourceSearchResult =
        withContext(Dispatchers.IO) {
            val safeLimit = limit.coerceIn(1, MAX_RESULTS)
            // A subject chip tapped without typing anything becomes the search text.
            val searchText = query.trim().ifBlank { subject?.let(SubjectMapper::searchTerm).orEmpty() }
            if (searchText.isBlank()) return@withContext ResourceSearchResult.Empty

            val bookLimit = (safeLimit + 1) / 2
            val articleLimit = safeLimit - bookLimit

            val (booksResult, articlesResult) = coroutineScope {
                val books = async { fetchBooks(searchText, subject, bookLimit) }
                val articles = async { fetchArticles(searchText, subject, articleLimit) }
                books.await() to articles.await()
            }

            val merged = (booksResult.getOrNull().orEmpty() + articlesResult.getOrNull().orEmpty())
                .distinctBy { it.id }
                .take(safeLimit)

            if (merged.isNotEmpty()) return@withContext ResourceSearchResult.Success(merged)

            val errors = listOfNotNull(booksResult.exceptionOrNull(), articlesResult.exceptionOrNull())
            val bothFailed = booksResult.isFailure && articlesResult.isFailure
            if (bothFailed && errors.isNotEmpty()) {
                val reason = failureReason(errors)
                ResourceSearchResult.Failure(reason, failureMessage(reason))
            } else {
                ResourceSearchResult.Empty
            }
        }

    /**
     * Offline/browse-only fallback built from the bundled sample catalogue.
     * The UI can offer this instead of an empty screen; it is the same data the app
     * shipped with, so nothing is invented.
     */
    fun sampleFallback(
        query: String = "",
        subject: String? = null,
        limit: Int = ResourceRepository.DEFAULT_LIMIT
    ): List<StudyResource> {
        val text = query.trim()
        return SampleStudyRepository.resources
            .filter { resource ->
                val matchesText = text.isBlank() ||
                    listOf(resource.title, resource.author, resource.subject, resource.source)
                        .any { it.contains(text, ignoreCase = true) }
                val matchesSubject = subject == null || resource.subject == subject
                matchesText && matchesSubject
            }
            .take(limit.coerceAtLeast(1))
    }

    private suspend fun fetchBooks(
        query: String,
        subjectTag: String?,
        limit: Int
    ): Result<List<StudyResource>> = attempt {
        openLibraryApi.searchBooks(
            query = query,
            // The canonical subjects ("Computer Networks") are not Open Library subjects,
            // so filtering is done by tagging results instead of by sending this parameter.
            subject = null,
            fields = OpenLibraryApi.SEARCH_FIELDS,
            limit = limit
        ).documents.mapNotNull { document -> document.toStudyResource(subjectTag) }
    }

    private suspend fun fetchArticles(
        query: String,
        subjectTag: String?,
        limit: Int
    ): Result<List<StudyResource>> {
        if (limit <= 0) return Result.success(emptyList())
        return attempt {
            wikipediaApi.searchArticles(query, limit)
                .query?.pages.orEmpty()
                .sortedBy { page -> page.searchIndex ?: Int.MAX_VALUE }
                .mapNotNull { page -> page.toStudyResource(subjectTag) }
        }
    }

    private suspend fun <T> attempt(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: Exception) {
        Result.failure(error)
    }

    private fun failureReason(errors: List<Throwable>): FailureReason = when {
        errors.any { it.isOffline() } -> FailureReason.OFFLINE
        errors.any { it is HttpException } -> FailureReason.SERVER
        errors.any { it is JsonParseException || it is MalformedJsonException } -> FailureReason.PARSE
        else -> FailureReason.UNKNOWN
    }

    private fun Throwable.isOffline(): Boolean {
        var current: Throwable? = this
        while (current != null) {
            if (current is UnknownHostException ||
                current is ConnectException ||
                current is SocketTimeoutException
            ) {
                return true
            }
            current = current.cause
        }
        return false
    }

    private fun failureMessage(reason: FailureReason): String = when (reason) {
        FailureReason.OFFLINE -> "No internet connection. Check your network and try again."
        FailureReason.SERVER -> "The resource services are not responding right now."
        FailureReason.PARSE -> "The resource services returned data we could not read."
        FailureReason.UNKNOWN -> "Something went wrong while loading resources."
    }

    private companion object {
        /** Sanity cap so a screen cannot ask the public APIs for hundreds of rows. */
        const val MAX_RESULTS = 40
    }
}
