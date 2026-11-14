package com.example.studyresourcemanager

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.studyresourcemanager.data.StudyResource
import com.example.studyresourcemanager.ui.Accent
import com.example.studyresourcemanager.ui.Ink
import com.example.studyresourcemanager.ui.Muted
import com.example.studyresourcemanager.ui.Paper
import com.example.studyresourcemanager.ui.ThemeChoice
import com.example.studyresourcemanager.ui.theme.StudyResourceManagerTheme

internal enum class StudyTab(val label: String, val activity: Class<out StudyActivity>) {
    HOME("Home", MainActivity::class.java),
    SEARCH("Search", SearchActivity::class.java),
    SAVED("Saved", SavedActivity::class.java),
    NOTES("Notes", NotesActivity::class.java)
}

abstract class StudyActivity : ComponentActivity() {
    internal open val tab: StudyTab? = null

    internal val session: StudySession
        get() = (application as StudyResourceApplication).session

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val darkTheme = when (session.theme) {
                ThemeChoice.SYSTEM -> isSystemInDarkTheme()
                ThemeChoice.DARK -> true
                ThemeChoice.LIGHT -> false
            }
            StudyResourceManagerTheme(darkTheme = darkTheme, dynamicColor = false) {
                Scaffold(containerColor = Paper, bottomBar = {
                    tab?.let { StudyNavigationBar(it, ::openTab) }
                }) { padding ->
                    Box(Modifier.fillMaxSize().padding(padding)) { Screen() }
                }
            }
        }
    }

    @Composable
    protected abstract fun Screen()

    internal fun openTab(destination: StudyTab) {
        if (tab == destination) return
        val intent = Intent(this, destination.activity)
        if (destination == StudyTab.HOME) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
        // Keep Home under the active tab so system Back returns to Home.
        if (tab != StudyTab.HOME && destination != StudyTab.HOME) finish()
    }

    internal fun browse(subject: String?) {
        session.query = ""
        session.selectedSubject = subject
        openTab(StudyTab.SEARCH)
    }

    internal fun openResource(resource: StudyResource) {
        session.recordVisit(resource)
        startActivity(ResourceDetailsActivity.intent(this, resource))
    }
}

@Composable
private fun StudyNavigationBar(current: StudyTab, onSelect: (StudyTab) -> Unit) {
    NavigationBar(containerColor = Paper, tonalElevation = 0.dp) {
        StudyTab.entries.forEach { item ->
            val selected = current == item
            val icon = when (item) {
                StudyTab.HOME -> Icons.Filled.Home
                StudyTab.SEARCH -> Icons.Filled.Search
                StudyTab.SAVED -> Icons.Filled.Bookmark
                StudyTab.NOTES -> Icons.Outlined.EditNote
            }
            NavigationBarItem(
                selected = selected,
                onClick = { onSelect(item) },
                icon = { Icon(icon, contentDescription = item.label) },
                label = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(item.label)
                        Spacer(Modifier.height(5.dp))
                        Box(
                            Modifier.width(34.dp).height(3.dp)
                                .background(if (selected) Accent else Color.Transparent)
                        )
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Accent,
                    selectedTextColor = Ink,
                    unselectedIconColor = Ink,
                    unselectedTextColor = Muted,
                    indicatorColor = Paper
                )
            )
        }
    }
}
