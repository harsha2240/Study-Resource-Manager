package com.example.studyresourcemanager.data.remote

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Parsing tests for the captured Open Library payload shape (search.json).
 * They run on the JVM, so "JSON is parsed correctly" and "missing fields do not crash"
 * can be proved without a device.
 */
class OpenLibraryMapperTest {

    private val gson = Gson()

    private fun documents(json: String) =
        gson.fromJson(json, OpenLibrarySearchResponse::class.java).documents

    @Test
    fun mapsCompleteDocument() {
        val document = documents(
            """
            {"numFound":1,"docs":[{
              "key":"/works/OL1970691W",
              "title":"Computer Networks",
              "author_name":["Andrew S. Tanenbaum","David J. Wetherall"],
              "first_publish_year":1981,
              "edition_count":42,
              "cover_i":9378629,
              "language":["eng","ger","fre"],
              "subject":["Computer networks","Networking"]
            }]}
            """.trimIndent()
        ).single()

        val resource = requireNotNull(document.toStudyResource())

        assertEquals("openlibrary:OL1970691W", resource.id)
        assertEquals("Computer Networks", resource.title)
        assertEquals("Andrew S. Tanenbaum, David J. Wetherall", resource.author)
        assertEquals("Computer Networks", resource.subject)
        assertEquals("Open Library", resource.source)
        assertEquals("BOOK", resource.kind)
        assertEquals("https://openlibrary.org/works/OL1970691W", resource.url)
        assertEquals("https://covers.openlibrary.org/b/id/9378629-M.jpg", resource.thumbnailUrl)
        assertTrue(resource.description.contains("First published 1981"))
        assertTrue(resource.description.contains("42 editions"))
    }

    @Test
    fun survivesMissingOptionalFields() {
        val document = documents("""{"docs":[{"key":"/works/OL56272W","title":"Computer networking"}]}""").single()

        val resource = requireNotNull(document.toStudyResource())

        assertEquals("Unknown author", resource.author)
        assertNull(resource.thumbnailUrl)
        assertEquals("Computer Networks", resource.subject)
    }

    @Test
    fun dropsDocumentsWithoutStableIdOrTitle() {
        val resources = documents(
            """
            {"docs":[
              {"title":"No work key"},
              {"key":"/works/OL1W"},
              {"key":"/works/OL2W","title":"   "}
            ]}
            """.trimIndent()
        ).mapNotNull { it.toStudyResource() }

        assertTrue(resources.isEmpty())
    }

    @Test
    fun honoursExplicitSubjectTag() {
        val document = documents("""{"docs":[{"key":"/works/OL9W","title":"Clean Code"}]}""").single()

        assertEquals("Database Systems", document.toStudyResource("Database Systems")?.subject)
    }

    @Test
    fun stripsInvisibleMarksFromTitle() {
        // \u200E is the left-to-right mark seen inside some live Open Library values.
        val document = documents("""{"docs":[{"key":"/works/OL3W","title":"\u200EClean Code\u200E"}]}""").single()

        assertEquals("Clean Code", document.toStudyResource()?.title)
    }
}
