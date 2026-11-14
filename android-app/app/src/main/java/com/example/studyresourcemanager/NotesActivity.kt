package com.example.studyresourcemanager

import androidx.compose.runtime.Composable
import com.example.studyresourcemanager.ui.NotesScreen

class NotesActivity : StudyActivity() {
    internal override val tab = StudyTab.NOTES

    @Composable
    override fun Screen() {
        NotesScreen(
            notes = session.notes,
            onAdd = session::addNote,
            onEdit = session::editNote,
            onDelete = session::deleteNote
        )
    }
}
