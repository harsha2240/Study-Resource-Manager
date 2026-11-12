package com.example.studyresourcemanager.data.remote

import com.example.studyresourcemanager.data.StudyResource
import com.google.gson.annotations.SerializedName
import java.net.URLEncoder

/**
 * Wire models for the Wikipedia search response (`formatversion=2`).
 * `query` is absent when the search matched nothing, so it is nullable.
 */
internal data class WikipediaSearchResponse(
    @SerializedName("query") val query: WikipediaQuery? = null
)

internal data class WikipediaQuery(
    @SerializedName("pages") val pages: List<WikipediaPage> = emptyList()
)

internal data class WikipediaPage(
    @SerializedName("pageid") val pageId: Long? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("extract") val extract: String? = null,
    @SerializedName("fullurl") val fullUrl: String? = null,
    @SerializedName("index") val searchIndex: Int? = null,
    @SerializedName("thumbnail") val thumbnail: WikipediaThumbnail? = null
)

internal data class WikipediaThumbnail(
    @SerializedName("source") val source: String? = null,
    @SerializedName("width") val width: Int? = null,
    @SerializedName("height") val height: Int? = null
)

/**
 * Maps one article onto the shared UI model, or null when the page has no usable id/title.
 *
 * @param subjectTag canonical subject to force (set when the user filtered by a chip);
 * when null the subject is inferred from the title and the intro text.
 */
internal fun WikipediaPage.toStudyResource(subjectTag: String? = null): StudyResource? {
    val pageId = pageId?.takeIf { it > 0 } ?: return null
    val cleanTitle = title.cleanApiText()
    if (cleanTitle.isEmpty()) return null

    val summary = extract.cleanApiText()
    // fullurl is requested through inprop=url; the fallback keeps the resource usable
    // even if the API stops returning it.
    val link = fullUrl.cleanApiText().ifEmpty {
        "https://en.wikipedia.org/wiki/" + encodeWikiTitle(cleanTitle)
    }

    return StudyResource(
        id = "wikipedia:$pageId",
        title = cleanTitle,
        author = SOURCE_NAME,
        subject = subjectTag ?: SubjectMapper.classify(cleanTitle, summary),
        source = SOURCE_NAME,
        kind = KIND_ARTICLE,
        description = if (summary.isEmpty()) {
            "Wikipedia article about $cleanTitle."
        } else {
            summary.truncateWords(MAX_SUMMARY_LENGTH)
        },
        url = link,
        thumbnailUrl = thumbnail?.source.cleanApiText()?.takeIf { it.isNotEmpty() }
    )
}

private fun encodeWikiTitle(title: String): String =
    URLEncoder.encode(title.replace(' ', '_'), "UTF-8")

private const val SOURCE_NAME = "Wikipedia"
private const val KIND_ARTICLE = "ARTICLE"
private const val MAX_SUMMARY_LENGTH = 320
