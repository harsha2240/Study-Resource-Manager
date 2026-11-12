package com.example.studyresourcemanager.data.remote

import com.example.studyresourcemanager.data.SampleStudyRepository

/**
 * Bridges the free-text subjects returned by public APIs and the six fixed subjects the
 * Compose UI filters by.
 *
 * SearchScreen filters with an exact string match (`resource.subject == selectedSubject`),
 * so a live result tagged "Computer networks" would never match the canonical
 * "Computer Networks". Everything produced by this package is therefore tagged with a
 * canonical subject (or [GENERAL]).
 */
internal object SubjectMapper {

    /** Same list the UI filters by, taken from the catalogue so the two cannot drift apart. */
    val canonicalSubjects: List<String>
        get() = SampleStudyRepository.subjects

    const val GENERAL = "General"

    /**
     * Ordered rules: the first canonical subject whose keyword appears in the text wins.
     * Machine Learning is checked before Computer Networks so "neural network" does not
     * end up classified as networking.
     */
    private val keywordRules: List<Pair<String, List<String>>> = listOf(
        "Data Structures & Algorithms" to listOf(
            "algorithm", "data structure", "sorting", "graph theory", "dynamic programming", "complexity"
        ),
        "Database Systems" to listOf(
            "database", "sql", "normalization", "normalisation", "relational", "dbms", "transaction"
        ),
        "Machine Learning" to listOf(
            "machine learning", "deep learning", "neural network", "artificial intelligence", "data mining"
        ),
        "Computer Networks" to listOf(
            "network", "networking", "protocol", "tcp", "wireless", "internet"
        ),
        "Software Engineering" to listOf(
            "software engineering", "design pattern", "clean code", "agile", "unit testing", "sdlc"
        ),
        "Operating Systems" to listOf(
            "operating system", "kernel", "scheduler", "virtual memory", "process management", "linux"
        )
    )

    /** Maps arbitrary API text (title, summary, raw subjects) onto a canonical subject. */
    fun classify(vararg texts: String?): String {
        val haystack = texts.filterNotNull().joinToString(" ").lowercase()
        if (haystack.isBlank()) return GENERAL
        return keywordRules
            .firstOrNull { (_, keywords) -> keywords.any { haystack.contains(it) } }
            ?.first
            ?: GENERAL
    }

    /** Search text sent to the APIs when the user only picked a subject chip. */
    fun searchTerm(subject: String): String = when (subject) {
        "Data Structures & Algorithms" -> "algorithms data structures"
        "Database Systems" -> "database systems sql"
        "Machine Learning" -> "machine learning"
        "Computer Networks" -> "computer networks"
        "Software Engineering" -> "software engineering"
        "Operating Systems" -> "operating system"
        else -> subject
    }
}
