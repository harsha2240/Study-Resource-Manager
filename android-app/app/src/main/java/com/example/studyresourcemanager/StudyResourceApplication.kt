package com.example.studyresourcemanager

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.studyresourcemanager.data.SampleStudyRepository
import com.example.studyresourcemanager.data.StudyNote
import com.example.studyresourcemanager.data.StudyResource
import com.example.studyresourcemanager.ui.ThemeChoice

class StudyResourceApplication : Application() {
    internal val session = StudySession()
}

internal class StudySession {
    val resources = SampleStudyRepository.resources
    var recent by mutableStateOf(emptyList<StudyResource>())
        private set
    var notes by mutableStateOf(emptyList<StudyNote>())
        private set
    var theme by mutableStateOf(ThemeChoice.LIGHT)
    var defaultSubject by mutableStateOf<String?>(null)
    var selectedSubject by mutableStateOf<String?>(null)
    var query by mutableStateOf("")

    private var savedResources by mutableStateOf(emptyMap<String, StudyResource>())
    private var nextNoteId = 1

    val savedIds: Set<String>
        get() = savedResources.keys

    val saved: List<StudyResource>
        get() = savedResources.values.toList()

    fun recordVisit(resource: StudyResource) {
        recent = (listOf(resource) + recent.filterNot { it.id == resource.id }).take(5)
    }

    fun toggleSaved(resource: StudyResource) {
        if (resource.id in savedIds) {
            removeBookmark(resource.id)
        } else {
            savedResources = savedResources + (resource.id to resource)
        }
    }

    fun toggleSaved(id: String) {
        val resource = recent.find { it.id == id } ?: resources.find { it.id == id }
        resource?.let(::toggleSaved)
    }

    fun removeBookmark(id: String) {
        savedResources = savedResources - id
    }

    fun addNote(title: String, subject: String, body: String) {
        notes = notes + StudyNote(nextNoteId++, title, subject, body)
    }

    fun editNote(updated: StudyNote) {
        notes = notes.map { if (it.id == updated.id) updated else it }
    }

    fun deleteNote(id: Int) {
        notes = notes.filterNot { it.id == id }
    }
}
