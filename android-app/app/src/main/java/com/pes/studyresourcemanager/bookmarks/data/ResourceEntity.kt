package com.pes.studyresourcemanager.bookmarks.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pes.studyresourcemanager.model.Resource

@Entity(tableName = "bookmarked_resources")
data class ResourceEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String?,
    val source: String,
    val url: String,
    val subject: String = "General",
    val savedAt: Long = System.currentTimeMillis()
)

fun ResourceEntity.toResource(): Resource = Resource(
    id = id,
    title = title,
    description = description,
    thumbnailUrl = thumbnailUrl,
    source = source,
    url = url
)

fun Resource.toEntity(subject: String = "General"): ResourceEntity = ResourceEntity(
    id = id.ifBlank { "${source}_$url" },
    title = title,
    description = description,
    thumbnailUrl = thumbnailUrl,
    source = source,
    url = url,
    subject = subject,
    savedAt = System.currentTimeMillis()
)