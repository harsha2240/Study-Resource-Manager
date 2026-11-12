package com.example.studyresourcemanager.data.remote

/**
 * Cleaning helpers for text coming from public catalogues.
 *
 * Live API data is messy: Open Library records mix HTML entities, repeated whitespace
 * and invisible bidirectional marks (some ISBN values even contain U+200E). Every
 * DTO -> model mapper runs its text through these helpers so the Compose screens never
 * have to deal with that noise.
 */
internal fun String?.cleanApiText(): String {
    if (this == null) return ""
    val withoutInvisibleMarks = filterNot { character ->
        character.code in 0x200B..0x200F || character.code == 0xFEFF || character.code == 0x00AD
    }
    return withoutInvisibleMarks
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&nbsp;", " ")
        .replace(REPEATED_WHITESPACE, " ")
        .trim()
}

/** Shortens long API text on a word boundary so cards and details stay readable. */
internal fun String.truncateWords(maxLength: Int): String {
    if (length <= maxLength) return this
    val cut = take(maxLength).substringBeforeLast(' ')
    val base = if (cut.isBlank()) take(maxLength) else cut
    return base.trimEnd().trimEnd(',', ';', '-', '.') + "…"
}

private val REPEATED_WHITESPACE = Regex("\\s+")
