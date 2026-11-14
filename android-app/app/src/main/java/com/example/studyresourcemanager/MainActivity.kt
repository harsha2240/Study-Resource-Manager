package com.example.studyresourcemanager

import android.content.Intent
import androidx.compose.runtime.Composable
import com.example.studyresourcemanager.data.SampleStudyRepository
import com.example.studyresourcemanager.ui.HomeScreen

class MainActivity : StudyActivity() {
    internal override val tab = StudyTab.HOME

    @Composable
    override fun Screen() {
        HomeScreen(
            onSearch = { browse(session.defaultSubject) },
            onNotes = { openTab(StudyTab.NOTES) },
            onSettings = { startActivity(Intent(this, SettingsActivity::class.java)) },
            subjects = SampleStudyRepository.subjects,
            onSubject = ::browse,
            featured = session.resources.first { it.id == "algorithms" },
            nextToRead = session.resources.first { it.id == "computer-networks" },
            recent = session.recent,
            savedIds = session.savedIds,
            onOpen = ::openResource,
            onBookmark = session::toggleSaved
        )
    }
}
