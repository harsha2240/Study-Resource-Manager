package com.pes.studyresourcemanager.bookmarks.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ResourceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(resource: ResourceEntity)

    @Query("DELETE FROM bookmarked_resources WHERE id = :resourceId")
    suspend fun deleteBookmarkById(resourceId: String)

    @Query("SELECT * FROM bookmarked_resources ORDER BY savedAt DESC")
    fun getAllBookmarks(): Flow<List<ResourceEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarked_resources WHERE id = :resourceId)")
    fun isBookmarked(resourceId: String): Flow<Boolean>
}

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("SELECT * FROM study_notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>
}

@Dao
interface SubjectDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT COUNT(*) FROM subjects")
    suspend fun getSubjectCount(): Int
}