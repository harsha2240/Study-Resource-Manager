package com.example.studyresourcemanager.data.remote

import com.example.studyresourcemanager.data.StudyResource
import com.google.gson.annotations.SerializedName

/**
 * Wire models for `https://openlibrary.org/search.json`.
 *
 * Every property is nullable or defaulted because the API omits keys when it has no data
 * for a document (a book without a cover, an author-less work, and so on).
 */
internal data class OpenLibrarySearchResponse(
    @SerializedName("numFound") val totalFound: Int = 0,
    @SerializedName("docs") val documents: List<OpenLibraryDocument> = emptyList()
)

internal data class OpenLibraryDocument(
    @SerializedName("key") val workKey: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("author_name") val authorNames: List<String>? = null,
    @SerializedName("first_publish_year") val firstPublishYear: Int? = null,
    @SerializedName("edition_count") val editionCount: Int? = null,
    @SerializedName("cover_i") val coverId: Long? = null,
    @SerializedName("language") val languages: List<String>? = null,
    @SerializedName("subject") val subjects: List<String>? = null
)

/**
 * Maps one search hit onto the shared UI model.
 *
 * Returns null for records without a stable work key or a title, because such a record
 * could neither be opened nor bookmarked (see the id rule: "openlibrary:<workId>").
 *
 * @param subjectTag canonical subject to force (set when the user filtered by a chip);
 * when null the subject is inferred from the title and the API subject list.
 */
internal fun OpenLibraryDocument.toStudyResource(subjectTag: String? = null): StudyResource? {
    val workPath = workKey.cleanApiText()
    val externalId = workPath.substringAfterLast('/', missingDelimiterValue = "")
    val cleanTitle = title.cleanApiText()
    if (externalId.isEmpty() || cleanTitle.isEmpty()) return null

    val authors = authorNames.orEmpty()
        .map { it.cleanApiText() }
        .filter { it.isNotEmpty() }

    return StudyResource(
        id = "openlibrary:$externalId",
        title = cleanTitle,
        author = authors.joinToString(", ").ifEmpty { UNKNOWN_AUTHOR },
        subject = subjectTag ?: SubjectMapper.classify(cleanTitle, subjects.orEmpty().joinToString(" ")),
        source = SOURCE_NAME,
        kind = KIND_BOOK,
        description = describeHoldings(cleanTitle),
        url = "https://openlibrary.org${workPath.ensureLeadingSlash()}",
        // "M" is ~180px wide: sharp enough for the list row and the details header.
        thumbnailUrl = coverId?.takeIf { it > 0 }
            ?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg" }
    )
}

/**
 * The search endpoint has no description field, so build an honest metadata summary
 * instead of inventing prose or making one extra request per book.
 */
private fun OpenLibraryDocument.describeHoldings(cleanTitle: String): String {
    val facts = buildList {
        firstPublishYear?.takeIf { it > 0 }?.let { add("First published $it") }
        editionCount?.takeIf { it > 0 }?.let { add(if (it == 1) "1 edition" else "$it editions") }
        val languageCount = languages.orEmpty().distinct().count { it.isNotBlank() }
        if (languageCount > 1) add("$languageCount languages")
    }
    return if (facts.isEmpty()) {
        "\"$cleanTitle\" on Open Library. No further metadata is available for this record."
    } else {
        "Open Library catalogue record: ${facts.joinToString(" · ")}."
    }
}

private fun String.ensureLeadingSlash(): String = if (startsWith("/")) this else "/$this"

private const val SOURCE_NAME = "Open Library"
private const val KIND_BOOK = "BOOK"
private const val UNKNOWN_AUTHOR = "Unknown author"
