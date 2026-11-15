package com.pes.studyresourcemanager.bookmarks.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pes.studyresourcemanager.bookmarks.data.*
import com.pes.studyresourcemanager.model.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BookmarksViewModel(application: Application) : AndroidViewModel(application) {
    private val database = StudyDatabase.getDatabase(application)
    private val repository = StudyRepository(
        database.resourceDao(),
        database.noteDao(),
        database.subjectDao()
    )
    private val prefs = PreferencesManager(application)

    private val _selectedSubject = MutableStateFlow("All")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    private val _defaultSubject = MutableStateFlow(prefs.defaultSubject)
    val defaultSubject: StateFlow<String> = _defaultSubject.asStateFlow()

    val subjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredBookmarks: StateFlow<List<ResourceEntity>> = combine(
        repository.allBookmarks,
        _selectedSubject
    ) { bookmarks, subject ->
        if (subject == "All") bookmarks else bookmarks.filter { it.subject == subject }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredNotes: StateFlow<List<NoteEntity>> = combine(
        repository.allNotes,
        _selectedSubject
    ) { notes, subject ->
        if (subject == "All") notes else notes.filter { it.subject == subject }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.ensureDefaultSubjects()
        }
    }

    fun selectSubjectFilter(subject: String) {
        _selectedSubject.value = subject
    }

    fun updateDefaultSubject(subject: String) {
        prefs.defaultSubject = subject
        _defaultSubject.value = subject
    }

    fun saveBookmark(resource: Resource, subject: String = prefs.defaultSubject) {
        viewModelScope.launch { repository.saveBookmark(resource, subject) }
    }

    fun removeBookmark(resourceId: String) {
        viewModelScope.launch { repository.removeBookmark(resourceId) }
    }

    fun addNote(title: String, content: String, subject: String = prefs.defaultSubject) {
        if (title.isBlank() && content.isBlank()) return
        viewModelScope.launch { repository.addNote(title.trim(), content.trim(), subject) }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch { repository.updateNote(note) }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch { repository.deleteNote(note) }
    }
}