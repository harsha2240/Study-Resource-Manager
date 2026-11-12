package com.example.studyresourcemanager.data.repository

import com.example.studyresourcemanager.data.StudyResource

/**
 * Source of study resources for the UI layer.
 *
 * Implementations must never throw: every failure is reported through
 * [ResourceSearchResult.Failure] so a ViewModel can stay a plain state machine.
 * The app currently has one implementation, [RemoteResourceRepository].
 */
interface ResourceRepository {

    /**
     * @param query free-text search. When blank, [subject] is used as the search text.
     * @param subject canonical subject to filter by, or null for any subject.
     * @param limit maximum number of resources to return.
     */
    suspend fun search(
        query: String,
        subject: String? = null,
        limit: Int = DEFAULT_LIMIT
    ): ResourceSearchResult

    companion object {
        const val DEFAULT_LIMIT = 20
    }
}

/** Outcome of one search. Success, Empty and Failure are deliberately distinct states. */
sealed interface ResourceSearchResult {

    data class Success(val resources: List<StudyResource>) : ResourceSearchResult

    /** The services answered normally but matched nothing. */
    data object Empty : ResourceSearchResult

    data class Failure(val reason: FailureReason, val message: String) : ResourceSearchResult
}

/**
 * Why a search failed. The UI decides the wording; [ResourceSearchResult.Failure.message]
 * is only a safe English fallback for logs and previews.
 */
enum class FailureReason {
    /** No connectivity, DNS failure or timeout. */
    OFFLINE,

    /** The service answered with an HTTP error status. */
    SERVER,

    /** A response arrived but could not be read as the expected JSON. */
    PARSE,

    UNKNOWN
}
