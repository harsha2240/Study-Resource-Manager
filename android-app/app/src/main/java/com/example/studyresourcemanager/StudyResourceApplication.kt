package com.example.studyresourcemanager

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.studyresourcemanager.data.SampleStudyRepository
import com.example.studyresourcemanager.data.StudyNote
import com.example.studyresourcemanager.data.StudyResource
import com.example.studyresourcemanager.data.local.PreferencesManager
import com.example.studyresourcemanager.data.local.StudyDatabase
import com.example.studyresourcemanager.data.local.StudyNoteEntity
import com.example.studyresourcemanager.data.local.toEntity
import com.example.studyresourcemanager.ui.ThemeChoice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StudyResourceApplication : Application() {
    internal val session: StudySession by lazy { StudySession(this) }
}

internal class StudySession(application: Application) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val database = StudyDatabase.getInstance(application)
    private val bookmarkDao = database.bookmarkDao()
    private val noteDao = database.noteDao()
    private val prefs = PreferencesManager(application)

    val resources = SampleStudyRepository.resources
    var recent by mutableStateOf(emptyList<StudyResource>())
        private set
    var notes by mutableStateOf(emptyList<StudyNote>())
        private set

    private var _theme by mutableStateOf(prefs.getThemeChoice())
    var theme: ThemeChoice
        get() = _theme
        set(value) {
            _theme = value
            prefs.setThemeChoice(value)
        }

    private var _defaultSubject by mutableStateOf(prefs.getDefaultSubject())
    var defaultSubject: String?
        get() = _defaultSubject
        set(value) {
            _defaultSubject = value
            prefs.setDefaultSubject(value)
            selectedSubject = value
        }

    var selectedSubject by mutableStateOf<String?>(prefs.getDefaultSubject())
    var query by mutableStateOf("")

    private var savedResources by mutableStateOf(emptyMap<String, StudyResource>())

    val savedIds: Set<String>
        get() = savedResources.keys

    val saved: List<StudyResource>
        get() = savedResources.values.toList()

    init {
        scope.launch {
            bookmarkDao.observeAllBookmarks().collectLatest { entities ->
                savedResources = entities.associate { it.id to it.toStudyResource() }
            }
        }
        scope.launch {
            noteDao.observeAllNotes().collectLatest { entities ->
                notes = entities.map { it.toStudyNote() }
            }
        }
    }

    fun recordVisit(resource: StudyResource) {
        recent = (listOf(resource) + recent.filterNot { it.id == resource.id }).take(5)
    }

    fun findResource(id: String?): StudyResource? = id?.let { resourceId ->
        savedResources[resourceId]
            ?: recent.find { it.id == resourceId }
            ?: resources.find { it.id == resourceId }
    }

    fun toggleSaved(resource: StudyResource) {
        if (resource.id in savedIds) {
            removeBookmark(resource.id)
        } else {
            val resourceToSave = if (resource.subject.isBlank()) {
                resource.copy(subject = defaultSubject ?: "General")
            } else {
                resource
            }
            savedResources = savedResources + (resourceToSave.id to resourceToSave)
            scope.launch {
                bookmarkDao.insertBookmark(resourceToSave.toEntity())
            }
        }
    }

    fun toggleSaved(id: String) {
        val resource = savedResources[id]
            ?: recent.find { it.id == id }
            ?: resources.find { it.id == id }
        resource?.let(::toggleSaved)
    }

    fun removeBookmark(id: String) {
        savedResources = savedResources - id
        scope.launch {
            bookmarkDao.deleteBookmarkById(id)
        }
    }

    fun addNote(title: String, subject: String, body: String) {
        val resolvedSubject = subject.trim().ifBlank { defaultSubject ?: "General" }
        scope.launch {
            noteDao.insertNote(
                StudyNoteEntity(
                    title = title.trim(),
                    subject = resolvedSubject,
                    body = body.trim()
                )
            )
        }
    }

    fun editNote(updated: StudyNote) {
        val resolvedSubject = updated.subject.trim().ifBlank { defaultSubject ?: "General" }
        scope.launch {
            noteDao.updateNote(
                StudyNoteEntity(
                    id = updated.id,
                    title = updated.title.trim(),
                    subject = resolvedSubject,
                    body = updated.body.trim()
                )
            )
        }
    }

    fun deleteNote(id: Int) {
        scope.launch {
            noteDao.deleteNoteById(id)
        }
    }
}
