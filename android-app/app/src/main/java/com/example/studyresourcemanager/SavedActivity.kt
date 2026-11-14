package com.example.studyresourcemanager

import androidx.compose.runtime.Composable
import com.example.studyresourcemanager.ui.SavedScreen

class SavedActivity : StudyActivity() {
    internal override val tab = StudyTab.SAVED

    @Composable
    override fun Screen() {
        SavedScreen(
            resources = session.saved,
            onRemove = session::removeBookmark,
            onOpen = ::openResource,
            onDiscover = { browse(session.defaultSubject) }
        )
    }
}
