package com.example.studyresourcemanager.data

/** Shared UI-facing shape for a resource; replace the sample source with the team repository later. */
data class StudyResource(
    val id: String,
    val title: String,
    val author: String,
    val subject: String,
    val source: String,
    val kind: String,
    val description: String,
    val url: String,
    /** Cover/thumbnail URL supplied by the remote catalogue; null when the source has no image. */
    val thumbnailUrl: String? = null
)

data class StudyNote(
    val id: Int,
    val title: String,
    val subject: String,
    val body: String
)
