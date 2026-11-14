package com.example.studyresourcemanager

import androidx.compose.runtime.Composable
import com.example.studyresourcemanager.data.SampleStudyRepository
import com.example.studyresourcemanager.ui.SettingsScreen

class SettingsActivity : StudyActivity() {
    @Composable
    override fun Screen() {
        SettingsScreen(
            theme = session.theme,
            onThemeChange = { session.theme = it },
            subjects = SampleStudyRepository.subjects,
            defaultSubject = session.defaultSubject,
            onSubjectChange = {
                session.defaultSubject = it
                session.selectedSubject = it
            },
            onBack = ::finish
        )
    }
}
