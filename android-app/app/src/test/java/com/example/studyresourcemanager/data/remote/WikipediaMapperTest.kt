package com.example.studyresourcemanager.data.remote

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Parsing tests for the Wikipedia action API shape (formatversion=2). */
class WikipediaMapperTest {

    private val gson = Gson()

    private fun pages(json: String) =
        gson.fromJson(json, WikipediaSearchResponse::class.java).query?.pages.orEmpty()

    @Test
    fun mapsArticleWithSummaryAndThumbnail() {
        val page = pages(
            """
            {"query":{"pages":[{
              "pageid":123456,
              "title":"Database normalization",
              "extract":"Database normalization is the process of structuring a relational database.",
              "fullurl":"https://en.wikipedia.org/wiki/Database_normalization",
              "index":1,
              "thumbnail":{"source":"https://upload.wikimedia.org/example.png","width":320,"height":200}
            }]}}
            """.trimIndent()
        ).single()

        val resource = requireNotNull(page.toStudyResource())

        assertEquals("wikipedia:123456", resource.id)
        assertEquals("Wikipedia", resource.author)
        assertEquals("Wikipedia", resource.source)
        assertEquals("ARTICLE", resource.kind)
        assertEquals("Database Systems", resource.subject)
        assertEquals("https://en.wikipedia.org/wiki/Database_normalization", resource.url)
        assertEquals("https://upload.wikimedia.org/example.png", resource.thumbnailUrl)
        assertTrue(resource.description.startsWith("Database normalization is the process"))
    }

    @Test
    fun survivesMissingThumbnailAndSummary() {
        val page = pages("""{"query":{"pages":[{"pageid":42,"title":"Graph theory"}]}}""").single()

        val resource = requireNotNull(page.toStudyResource())

        assertNull(resource.thumbnailUrl)
        assertEquals("Data Structures & Algorithms", resource.subject)
        assertEquals("Wikipedia article about Graph theory.", resource.description)
    }

    @Test
    fun buildsArticleUrlWhenFullUrlIsMissing() {
        val page = pages("""{"query":{"pages":[{"pageid":7,"title":"Operating system"}]}}""").single()

        val url = requireNotNull(page.toStudyResource()).url

        assertTrue(url.startsWith("https://en.wikipedia.org/wiki/"))
        assertTrue(url.contains("Operating_system"))
    }

    @Test
    fun returnsNullWithoutPageId() {
        val page = pages("""{"query":{"pages":[{"title":"Missing page id"}]}}""").single()

        assertNull(page.toStudyResource())
    }

    @Test
    fun returnsNullForEmptyQueryBlock() {
        val response = gson.fromJson("""{"batchcomplete":true}""", WikipediaSearchResponse::class.java)

        assertNull(response.query)
    }
}
