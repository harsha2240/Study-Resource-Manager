package com.example.studyresourcemanager.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarked_resources ORDER BY savedAt DESC")
    fun observeAllBookmarks(): Flow<List<BookmarkedResourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(resource: BookmarkedResourceEntity)

    @Query("DELETE FROM bookmarked_resources WHERE id = :id")
    suspend fun deleteBookmarkById(id: String)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM study_notes ORDER BY id ASC")
    fun observeAllNotes(): Flow<List<StudyNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: StudyNoteEntity)

    @Update
    suspend fun updateNote(note: StudyNoteEntity)

    @Query("DELETE FROM study_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)
}
