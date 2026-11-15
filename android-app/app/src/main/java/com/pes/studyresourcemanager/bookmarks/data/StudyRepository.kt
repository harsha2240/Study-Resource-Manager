package com.pes.studyresourcemanager.bookmarks.data

import android.content.Context
import android.content.SharedPreferences
import com.pes.studyresourcemanager.model.Resource
import kotlinx.coroutines.flow.Flow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("study_resource_prefs", Context.MODE_PRIVATE)

    var defaultSubject: String
        get() = prefs.getString("default_subject", "General") ?: "General"
        set(value) = prefs.edit().putString("default_subject", value).apply()
}

class StudyRepository(
    private val resourceDao: ResourceDao,
    private val noteDao: NoteDao,
    private val subjectDao: SubjectDao
) {
    val allBookmarks: Flow<List<ResourceEntity>> = resourceDao.getAllBookmarks()
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()
    val allSubjects: Flow<List<SubjectEntity>> = subjectDao.getAllSubjects()

    suspend fun saveBookmark(resource: Resource, subject: String = "General") {
        resourceDao.insertBookmark(resource.toEntity(subject))
    }

    suspend fun removeBookmark(resourceId: String) {
        resourceDao.deleteBookmarkById(resourceId)
    }

    suspend fun addNote(title: String, content: String, subject: String) {
        val now = System.currentTimeMillis()
        noteDao.insertNote(
            NoteEntity(
                title = title,
                content = content,
                subject = subject,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun updateNote(note: NoteEntity) {
        noteDao.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteNote(note: NoteEntity) {
        noteDao.deleteNote(note)
    }

    suspend fun ensureDefaultSubjects() {
        if (subjectDao.getSubjectCount() == 0) {
            listOf(
                SubjectEntity(name = "General", description = "General study topics"),
                SubjectEntity(name = "DSA", description = "Data Structures and Algorithms"),
                SubjectEntity(name = "DBMS", description = "Database Management Systems"),
                SubjectEntity(name = "Machine Learning", description = "AI and ML concepts"),
                SubjectEntity(name = "Computer Networks", description = "Networking protocols and layers")
            ).forEach { subjectDao.insertSubject(it) }
        }
    }
}