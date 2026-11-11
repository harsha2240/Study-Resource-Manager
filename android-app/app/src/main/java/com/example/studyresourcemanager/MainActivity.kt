package com.example.studyresourcemanager

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.studyresourcemanager.data.SampleStudyRepository
import com.example.studyresourcemanager.data.StudyNote
import com.example.studyresourcemanager.data.StudyResource
import com.example.studyresourcemanager.ui.Accent
import com.example.studyresourcemanager.ui.HomeScreen
import com.example.studyresourcemanager.ui.Ink
import com.example.studyresourcemanager.ui.Muted
import com.example.studyresourcemanager.ui.NotesScreen
import com.example.studyresourcemanager.ui.Paper
import com.example.studyresourcemanager.ui.ResourceDetailsScreen
import com.example.studyresourcemanager.ui.SavedScreen
import com.example.studyresourcemanager.ui.SearchScreen
import com.example.studyresourcemanager.ui.SettingsScreen
import com.example.studyresourcemanager.ui.ThemeChoice
import com.example.studyresourcemanager.ui.theme.StudyResourceManagerTheme

private data class AppTab(val route: String, val label: String)

private val appTabs = listOf(
    AppTab("home", "Home"),
    AppTab("search", "Search"),
    AppTab("bookmarks", "Saved"),
    AppTab("notes", "Notes")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { StudyResourceApp() }
    }
}

@Composable
private fun StudyResourceApp() {
    val navController = rememberNavController()
    val resources = SampleStudyRepository.resources
    var savedIds by remember { mutableStateOf(setOf<String>()) }
    var notes by remember { mutableStateOf(emptyList<StudyNote>()) }
    var nextNoteId by remember { mutableIntStateOf(1) }

    fun toggleSaved(resourceId: String) {
        savedIds = if (resourceId in savedIds) savedIds - resourceId else savedIds + resourceId
    }

    var recentIds by rememberSaveable { mutableStateOf(arrayListOf<String>()) }
    var theme by rememberSaveable { mutableStateOf(ThemeChoice.LIGHT) }
    var defaultSubject by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedSubject by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    fun navigateTab(route: String) {
        if (currentRoute == route) return
        if (route == "home") {
            navController.popBackStack("home", inclusive = false, saveState = true)
        } else {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    fun browse(subject: String?) {
        query = ""
        selectedSubject = subject
        navigateTab("search")
    }

    fun openResource(resource: StudyResource) {
        recentIds = ArrayList((listOf(resource.id) + recentIds.filterNot { it == resource.id }).take(5))
        navController.navigate("resource/${Uri.encode(resource.id)}") { launchSingleTop = true }
    }

    val darkTheme = when (theme) {
        ThemeChoice.SYSTEM -> isSystemInDarkTheme()
        ThemeChoice.DARK -> true
        ThemeChoice.LIGHT -> false
    }
    StudyResourceManagerTheme(darkTheme = darkTheme, dynamicColor = false) {
        Scaffold(
            containerColor = Paper,
            bottomBar = bottomBar@{
                if (appTabs.none { it.route == currentRoute }) return@bottomBar

                NavigationBar(
                    containerColor = Paper,
                    tonalElevation = 0.dp
                ) {
                    appTabs.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navigateTab(tab.route) },
                            icon = {
                                Icon(
                                    imageVector = tabIcon(tab.route),
                                    contentDescription = tab.label
                                )
                            },
                            label = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(tab.label)
                                    Spacer(Modifier.height(5.dp))
                                    Box(
                                        Modifier
                                            .width(34.dp)
                                            .height(3.dp)
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
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(padding)
            ) {
                composable("home") {
                    HomeScreen(
                        onSearch = { browse(defaultSubject) },
                        onNotes = { navigateTab("notes") },
                        onSettings = { navController.navigate("settings") },
                        subjects = SampleStudyRepository.subjects,
                        onSubject = ::browse,
                        featured = resources.first { it.id == "algorithms" },
                        nextToRead = resources.first { it.id == "computer-networks" },
                        recent = recentIds.mapNotNull { id -> resources.find { it.id == id } },
                        savedIds = savedIds,
                        onOpen = ::openResource,
                        onBookmark = ::toggleSaved
                    )
                }
                composable("search") {
                    SearchScreen(
                        resources = resources,
                        savedIds = savedIds,
                        query = query,
                        onQueryChange = { query = it },
                        selectedSubject = selectedSubject,
                        onSubjectChange = { selectedSubject = it },
                        onOpen = ::openResource,
                        onToggleSaved = ::toggleSaved
                    )
                }
                composable("bookmarks") {
                    SavedScreen(
                        resources = resources.filter { it.id in savedIds },
                        onRemove = ::toggleSaved,
                        onOpen = ::openResource,
                        onDiscover = { browse(defaultSubject) }
                    )
                }
                composable("resource/{resourceId}") { entry ->
                    val resource = resources.find { it.id == entry.arguments?.getString("resourceId") }
                    ResourceDetailsScreen(
                        resource = resource,
                        isSaved = resource != null && resource.id in savedIds,
                        onBack = { navController.popBackStack() },
                        onBookmark = ::toggleSaved
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        theme = theme,
                        onThemeChange = { theme = it },
                        subjects = SampleStudyRepository.subjects,
                        defaultSubject = defaultSubject,
                        onSubjectChange = {
                            defaultSubject = it
                            selectedSubject = it
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("notes") {
                    NotesScreen(
                        notes = notes,
                        onAdd = { title, subject, body ->
                            notes = notes + StudyNote(nextNoteId++, title, subject, body)
                        },
                        onEdit = { updated -> notes = notes.map { if (it.id == updated.id) updated else it } },
                        onDelete = { noteId -> notes = notes.filterNot { it.id == noteId } }
                    )
                }
            }
        }
    }
}

@Composable
private fun tabIcon(route: String) = when (route) {
    "home" -> Icons.Filled.Home
    "search" -> Icons.Filled.Search
    "bookmarks" -> Icons.Filled.Bookmark
    else -> Icons.Outlined.EditNote
}
