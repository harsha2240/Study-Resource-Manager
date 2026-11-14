package com.example.studyresourcemanager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.studyresourcemanager.data.StudyResource
import com.example.studyresourcemanager.data.repository.RemoteResourceRepository
import com.example.studyresourcemanager.data.repository.ResourceSearchResult
import com.example.studyresourcemanager.ui.SearchScreen
import kotlinx.coroutines.delay

class SearchActivity : StudyActivity() {
    internal override val tab = StudyTab.SEARCH
    private val repository by lazy { RemoteResourceRepository() }

    @Composable
    override fun Screen() {
        val query = session.query
        val subject = session.selectedSubject
        val isBrowsing = query.isBlank() && subject == null
        var results by remember(query, subject) {
            mutableStateOf<List<StudyResource>>(if (isBrowsing) session.resources else emptyList())
        }
        var loading by remember(query, subject) { mutableStateOf(!isBrowsing) }
        var error by remember(query, subject) { mutableStateOf<String?>(null) }

        LaunchedEffect(query, subject) {
            if (isBrowsing) return@LaunchedEffect
            if (query.isNotBlank()) delay(350)
            when (val result = repository.search(query, subject)) {
                is ResourceSearchResult.Success -> results = result.resources
                ResourceSearchResult.Empty -> results = emptyList()
                is ResourceSearchResult.Failure -> {
                    results = repository.sampleFallback(query, subject)
                    error = result.message
                }
            }
            loading = false
        }

        SearchScreen(
            resources = results,
            savedIds = session.savedIds,
            query = query,
            onQueryChange = { session.query = it },
            selectedSubject = subject,
            onSubjectChange = { session.selectedSubject = it },
            onOpen = ::openResource,
            onToggleSaved = session::toggleSaved,
            isLoading = loading,
            errorMessage = error,
            showingSampleFallback = error != null && results.isNotEmpty()
        )
    }
}
