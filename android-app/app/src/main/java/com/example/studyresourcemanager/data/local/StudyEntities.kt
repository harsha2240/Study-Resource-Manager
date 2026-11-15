package com.example.studyresourcemanager.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.studyresourcemanager.data.StudyNote
import com.example.studyresourcemanager.data.StudyResource

@Entity(tableName = "bookmarked_resources")
data class BookmarkedResourceEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val subject: String,
    val source: String,
    val kind: String,
    val description: String,
    val url: String,
    val thumbnailUrl: String? = null,
    val savedAt: Long = System.currentTimeMillis()
) {
    fun toStudyResource(): StudyResource = StudyResource(
        id = id,
        title = title,
        author = author,
        subject = subject,
        source = source,
        kind = kind,
        description = description,
        url = url,
        thumbnailUrl = thumbnailUrl
    )
}

fun StudyResource.toEntity(): BookmarkedResourceEntity = BookmarkedResourceEntity(
    id = id,
    title = title,
    author = author,
    subject = subject,
    source = source,
    kind = kind,
    description = description,
    url = url,
    thumbnailUrl = thumbnailUrl
)

@Entity(tableName = "study_notes")
data class StudyNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val subject: String,
    val body: String,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toStudyNote(): StudyNote = StudyNote(
        id = id,
        title = title,
        subject = subject,
        body = body
    )
}
