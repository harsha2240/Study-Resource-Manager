package com.example.studyresourcemanager.data.repository

import com.example.studyresourcemanager.data.SampleStudyRepository
import com.example.studyresourcemanager.data.StudyResource
import com.example.studyresourcemanager.data.remote.OpenLibraryApi
import com.example.studyresourcemanager.data.remote.RetrofitClient
import com.example.studyresourcemanager.data.remote.SubjectMapper
import com.example.studyresourcemanager.data.remote.WikipediaApi
import com.example.studyresourcemanager.data.remote.WikipediaServiceException
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
 * Merge rules (a skipped provider is not the same as one that answered with nothing):
 *  - usable results from either provider are always returned, even when the other one failed;
 *  - when nothing usable came back, any provider failure wins over the other provider's empty
 *    or skipped response, so a dead service is never reported as "no results";
 *  - Empty is only returned when every provider that actually ran answered with no matches;
 *  - the returned list is de-duplicated by [StudyResource.id] and capped at the requested limit;
 *  - cancellation (screen left, ViewModel cleared) always propagates instead of becoming a Failure.
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

            val (booksOutcome, articlesOutcome) = coroutineScope {
                val books = async { fetchBooks(searchText, subject, bookLimit) }
                val articles = async { fetchArticles(searchText, subject, articleLimit) }
                books.await() to articles.await()
            }

            val outcomes = listOf(booksOutcome, articlesOutcome)

            // Rules 1, 2 and 5: whatever a provider returned is kept, even if the other failed.
            val usable = outcomes
                .filterIsInstance<ProviderOutcome.Success>()
                .flatMap { it.resources }
                .distinctBy { it.id }
                .take(safeLimit)
            if (usable.isNotEmpty()) return@withContext ResourceSearchResult.Success(usable)

            // Rules 3 and 4: with nothing usable, a provider failure decides the outcome
            // instead of being masked by the other provider's empty or skipped response.
            val failures = outcomes.filterIsInstance<ProviderOutcome.Failure>().map { it.error }
            if (failures.isNotEmpty()) {
                val reason = failureReason(failures)
                return@withContext ResourceSearchResult.Failure(reason, failureMessage(reason))
            }

            // Rule 6: every provider that ran answered normally with nothing to show.
            ResourceSearchResult.Empty
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
    ): ProviderOutcome {
        if (limit <= 0) return ProviderOutcome.Skipped
        return queryProvider {
            openLibraryApi.searchBooks(
                query = query,
                // The canonical subjects ("Computer Networks") are not Open Library subjects,
                // so filtering is done by tagging results instead of by sending this parameter.
                subject = null,
                fields = OpenLibraryApi.SEARCH_FIELDS,
                limit = limit
            ).documents.mapNotNull { document -> document.toStudyResource(subjectTag) }
        }
    }

    private suspend fun fetchArticles(
        query: String,
        subjectTag: String?,
        limit: Int
    ): ProviderOutcome {
        if (limit <= 0) return ProviderOutcome.Skipped
        return queryProvider {
            val response = wikipediaApi.searchArticles(query, limit)
            // MediaWiki can answer HTTP 200 with an "error" object; that is a provider
            // failure, not a search that legitimately matched nothing.
            response.error?.let { error -> throw WikipediaServiceException(error.code, error.info) }
            response.query?.pages.orEmpty()
                .sortedBy { page -> page.searchIndex ?: Int.MAX_VALUE }
                .mapNotNull { page -> page.toStudyResource(subjectTag) }
        }
    }

    private suspend fun queryProvider(block: suspend () -> List<StudyResource>): ProviderOutcome = try {
        ProviderOutcome.Success(block())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: Exception) {
        ProviderOutcome.Failure(error)
    }

    private fun failureReason(errors: List<Throwable>): FailureReason = when {
        errors.any { it.isOffline() } -> FailureReason.OFFLINE
        errors.any { it is HttpException || it is WikipediaServiceException } -> FailureReason.SERVER
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

    /**
     * What one provider contributed to a search. [Skipped] must stay distinct from a
     * success that returned nothing, otherwise a failure of the other provider would be
     * reported as an empty result.
     */
    private sealed interface ProviderOutcome {
        data class Success(val resources: List<StudyResource>) : ProviderOutcome
        data class Failure(val error: Throwable) : ProviderOutcome
        data object Skipped : ProviderOutcome
    }

    private companion object {
        /** Sanity cap so a screen cannot ask the public APIs for hundreds of rows. */
        const val MAX_RESULTS = 40
    }
}
