package com.example.studyresourcemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.studyresourcemanager.data.SampleStudyRepository
import com.example.studyresourcemanager.data.StudyNote
import com.example.studyresourcemanager.data.StudyResource
import com.example.studyresourcemanager.ui.theme.StudyResourceManagerTheme

private val Ink = Color(0xFF111111)
private val Muted = Color(0xFF777777)
private val Rule = Color(0xFFD9D9D9)
private val Accent = Color(0xFFF04432)
private val Paper = Color.White

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

    StudyResourceManagerTheme(darkTheme = false, dynamicColor = false) {
        Scaffold(
            containerColor = Paper,
            bottomBar = {
                val backStack by navController.currentBackStackEntryAsState()
                val currentRoute = backStack?.destination?.hierarchy?.firstOrNull()?.route

                NavigationBar(
                    containerColor = Paper,
                    tonalElevation = 0.dp
                ) {
                    appTabs.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
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
                        onSearch = { navController.navigate("search") },
                        onNotes = { navController.navigate("notes") },
                        featuredSaved = "algorithms" in savedIds,
                        onToggleFeaturedSaved = { toggleSaved("algorithms") }
                    )
                }
                composable("search") {
                    SearchScreen(
                        resources = resources,
                        savedIds = savedIds,
                        onToggleSaved = ::toggleSaved
                    )
                }
                composable("bookmarks") {
                    SavedScreen(
                        resources = resources.filter { it.id in savedIds },
                        onRemove = ::toggleSaved,
                        onDiscover = { navController.navigate("search") }
                    )
                }
                composable("notes") {
                    NotesScreen(
                        notes = notes,
                        onAdd = { title, subject, body ->
                            notes = notes + StudyNote(nextNoteId++, title, subject, body)
                        },
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

@Composable
private fun HomeScreen(
    onSearch: () -> Unit,
    onNotes: () -> Unit,
    featuredSaved: Boolean,
    onToggleFeaturedSaved: () -> Unit
) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 20.dp)
    ) {
        item { HomeHeader(onSearch) }

        item {
            Column(Modifier.padding(top = 30.dp, bottom = 24.dp)) {
                Text("Good afternoon,", color = Ink, fontSize = 34.sp, lineHeight = 38.sp, fontWeight = FontWeight.Medium)
                Text("ready to learn?", color = Ink, fontSize = 34.sp, lineHeight = 38.sp, fontWeight = FontWeight.Medium)
                Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.Top) {
                    Text("“", color = Accent, fontSize = 26.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp)
                    Spacer(Modifier.width(5.dp))
                    Text("Small steps make a strong study habit.", color = Ink, fontSize = 13.sp, lineHeight = 18.sp)
                }
            }
        }

        item { HorizontalDivider(color = Rule) }

        item {
            Column(Modifier.padding(top = 17.dp, bottom = 15.dp)) {
                SectionLabel("YOUR SUBJECTS")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("DSA", "DBMS", "ML", "Networks").forEachIndexed { index, subject ->
                        if (index > 0) {
                            Box(Modifier.padding(horizontal = 12.dp).width(1.dp).height(22.dp).background(Rule))
                        }
                        Text(subject, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        item { HorizontalDivider(color = Rule) }

        item {
            Column(Modifier.padding(top = 19.dp, bottom = 13.dp)) {
                SectionLabel("FEATURED RESOURCE")
                Spacer(Modifier.height(12.dp))
                FeaturedArtwork(isSaved = featuredSaved, onToggleSaved = onToggleFeaturedSaved)
                Spacer(Modifier.height(12.dp))
                Text("Introduction to Algorithms", color = Ink, fontSize = 21.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold)
                Text("Thomas H. Cormen  ·  Open Library book", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 3.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("DATA STRUCTURES", color = Muted, fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (featuredSaved) "SAVED" else "SAVE", color = Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = onToggleFeaturedSaved, modifier = Modifier.size(32.dp)) {
                            Icon(
                                if (featuredSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = if (featuredSaved) "Remove featured resource from saved" else "Save featured resource",
                                tint = Accent
                            )
                        }
                    }
                }
            }
        }

        item { HorizontalDivider(color = Rule) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallFeaturePanel(
                    title = "NEXT TO READ",
                    body = "Computer Networks",
                    detail = "Open Library",
                    icon = Icons.AutoMirrored.Outlined.MenuBook,
                    modifier = Modifier.weight(1f),
                    onClick = onSearch
                )
                SmallFeaturePanel(
                    title = "STUDY NOTES",
                    body = "Keep your ideas",
                    detail = "Open notes",
                    icon = Icons.Outlined.EditNote,
                    modifier = Modifier.weight(1f),
                    onClick = onNotes
                )
            }
        }
    }
}

@Composable
private fun HomeHeader(onSearch: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = "Study Resource Manager", tint = Ink, modifier = Modifier.size(24.dp))
        Box(Modifier.padding(horizontal = 12.dp).width(1.dp).height(30.dp).background(Rule))
        Surface(
            onClick = onSearch,
            color = Paper,
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, Rule),
            modifier = Modifier.weight(1f)
        ) {
            Row(Modifier.padding(horizontal = 10.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = Muted, modifier = Modifier.size(18.dp))
                Text("Search books or topics", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Box(Modifier.padding(horizontal = 10.dp).width(1.dp).height(30.dp).background(Rule))
        Text("MY LIBRARY", color = Muted, fontSize = 9.sp, letterSpacing = .5.sp, maxLines = 1)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = Ink, fontSize = 12.sp, letterSpacing = .8.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun FeaturedArtwork(isSaved: Boolean, onToggleSaved: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(178.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF0B5A86), Color(0xFF092842))))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawRect(Color(0xFF0C4774), size = size)
            val structure = Path().apply {
                moveTo(w * .24f, h)
                lineTo(w * .65f, h * .12f)
                lineTo(w, h * .48f)
                lineTo(w, h)
                close()
            }
            drawPath(structure, Color(0xFF123959))
            drawRect(Color(0xFFE94332), topLeft = androidx.compose.ui.geometry.Offset(w * .77f, h * .34f), size = androidx.compose.ui.geometry.Size(w * .09f, h * .66f))
            drawLine(Color.White.copy(alpha = .38f), androidx.compose.ui.geometry.Offset(w * .10f, h * .76f), androidx.compose.ui.geometry.Offset(w * .63f, h * .18f), strokeWidth = 2.dp.toPx())
            drawLine(Color.White.copy(alpha = .24f), androidx.compose.ui.geometry.Offset(w * .08f, h * .88f), androidx.compose.ui.geometry.Offset(w * .52f, h * .40f), strokeWidth = 1.dp.toPx())
        }
        Row(
            modifier = Modifier.align(Alignment.BottomEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(color = Paper, shape = RoundedCornerShape(topStart = 2.dp), modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = "Open resource", tint = Ink) }
            }
            Surface(color = Accent, shape = RoundedCornerShape(topStart = 2.dp), modifier = Modifier.size(48.dp)) {
                IconButton(onClick = onToggleSaved, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (isSaved) "Remove featured resource from saved" else "Save featured resource",
                        tint = Paper
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallFeaturePanel(
    title: String,
    body: String,
    detail: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(2.dp),
        colors = CardDefaults.cardColors(containerColor = Paper),
        border = BorderStroke(1.dp, Rule)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(title, color = Ink, fontSize = 10.sp, letterSpacing = .4.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Spacer(Modifier.height(14.dp))
            Icon(icon, contentDescription = null, tint = Accent, modifier = Modifier.size(23.dp))
            Spacer(Modifier.height(10.dp))
            Text(body, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(detail, color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SearchScreen(
    resources: List<StudyResource>,
    savedIds: Set<String>,
    onToggleSaved: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    val filteredResources = resources.filter { resource ->
        val matchesQuery = query.isBlank() || listOf(resource.title, resource.author, resource.subject, resource.source)
            .any { it.contains(query.trim(), ignoreCase = true) }
        val matchesSubject = selectedSubject == null || resource.subject == selectedSubject
        matchesQuery && matchesSubject
    }

    Column(Modifier.fillMaxSize().background(Paper).padding(horizontal = 20.dp, vertical = 24.dp)) {
        SectionLabel("DISCOVER")
        Spacer(Modifier.height(8.dp))
        Text("Search resources", color = Ink, fontSize = 29.sp, fontWeight = FontWeight.Medium)
        Text("Books and articles for your next study session.", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 5.dp))
        Spacer(Modifier.height(17.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(3.dp),
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            placeholder = { Text("Search books or topics") }
        )
        Spacer(Modifier.height(18.dp))
        SectionLabel(if (selectedSubject == null && query.isBlank()) "POPULAR SUBJECTS" else "FILTER BY SUBJECT")
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 9.dp, bottom = 13.dp).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedSubject == null,
                onClick = { selectedSubject = null },
                label = { Text("All") }
            )
            SampleStudyRepository.subjects.forEach { subject ->
                FilterChip(
                    selected = selectedSubject == subject,
                    onClick = { selectedSubject = if (selectedSubject == subject) null else subject },
                    label = { Text(subject, maxLines = 1) }
                )
            }
        }
        SectionLabel(if (query.isBlank() && selectedSubject == null) "SAMPLE LIBRARY" else "SEARCH RESULTS")
        Spacer(Modifier.height(4.dp))
        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(bottom = 12.dp)) {
            if (filteredResources.isEmpty()) {
                item {
                    Text(
                        "No resources match that search. Try another title or subject.",
                        color = Muted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            }
            items(filteredResources, key = { it.id }) { resource ->
                ResourceRow(
                    resource = resource,
                    isSaved = resource.id in savedIds,
                    onBookmark = { onToggleSaved(resource.id) }
                )
            }
        }
    }
}

@Composable
private fun SavedScreen(
    resources: List<StudyResource>,
    onRemove: (String) -> Unit,
    onDiscover: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(Paper).padding(horizontal = 20.dp, vertical = 24.dp)) {
        SectionLabel("YOUR LIBRARY")
        Spacer(Modifier.height(8.dp))
        Text("Saved resources", color = Ink, fontSize = 29.sp, fontWeight = FontWeight.Medium)
        Text("A small collection for your next study session.", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 5.dp))
        Spacer(Modifier.height(18.dp))
        HorizontalDivider(color = Rule)

        if (resources.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(bottom = 45.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Outlined.BookmarkBorder, contentDescription = null, tint = Accent, modifier = Modifier.size(30.dp))
                Spacer(Modifier.height(12.dp))
                Text("Nothing saved yet", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text("Browse the sample library and bookmark a resource.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 5.dp))
                Button(
                    onClick = onDiscover,
                    modifier = Modifier.padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) { Text("BROWSE RESOURCES") }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 12.dp)) {
                items(resources, key = { it.id }) { resource ->
                    ResourceRow(resource, isSaved = true, onBookmark = { onRemove(resource.id) })
                }
            }
        }
    }
}

@Composable
private fun ResourceRow(resource: StudyResource, isSaved: Boolean, onBookmark: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(width = 42.dp, height = 52.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFFFF0ED)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, tint = Accent, modifier = Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f).padding(start = 12.dp, end = 4.dp)) {
            Text(resource.title, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(resource.author, color = Muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 3.dp))
            Text(resource.subject.uppercase(), color = Accent, fontSize = 9.sp, letterSpacing = .5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
        }
        IconButton(onClick = onBookmark, modifier = Modifier.size(40.dp)) {
            Icon(
                if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = if (isSaved) "Remove ${resource.title} from saved" else "Save ${resource.title}",
                tint = Accent
            )
        }
    }
    HorizontalDivider(color = Rule)
}

@Composable
private fun NotesScreen(
    notes: List<StudyNote>,
    onAdd: (String, String, String) -> Unit,
    onDelete: (Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    val canSave = title.isNotBlank() && body.isNotBlank()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Paper),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp)
    ) {
        item {
            SectionLabel("YOUR STUDY SPACE")
            Spacer(Modifier.height(8.dp))
            Text("Study notes", color = Ink, fontSize = 29.sp, fontWeight = FontWeight.Medium)
            Text("Capture the ideas you want to remember.", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 5.dp))
            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = Rule)
            Spacer(Modifier.height(16.dp))
            SectionLabel("NEW NOTE")
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(3.dp),
                label = { Text("Note title") }
            )
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(3.dp),
                label = { Text("Subject (optional)") }
            )
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(3.dp),
                label = { Text("Your note") }
            )
            Button(
                onClick = {
                    onAdd(title.trim(), subject.trim().ifBlank { "General" }, body.trim())
                    title = ""
                    subject = ""
                    body = ""
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent)
            ) { Text("SAVE NOTE") }
            Spacer(Modifier.height(22.dp))
            SectionLabel("YOUR NOTES  ·  ${notes.size}")
            Spacer(Modifier.height(8.dp))
        }

        if (notes.isEmpty()) {
            item {
                Text("Your saved notes will appear here.", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(vertical = 14.dp))
            }
        } else {
            items(notes.reversed(), key = { it.id }) { note ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    shape = RoundedCornerShape(3.dp),
                    colors = CardDefaults.cardColors(containerColor = Paper),
                    border = BorderStroke(1.dp, Rule)
                ) {
                    Column(Modifier.fillMaxWidth().padding(start = 14.dp, end = 8.dp, top = 11.dp, bottom = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(note.title, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text(note.subject.uppercase(), color = Accent, fontSize = 9.sp, letterSpacing = .6.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 3.dp))
                            }
                            TextButton(onClick = { onDelete(note.id) }) { Text("DELETE", color = Muted, fontSize = 10.sp) }
                        }
                        Text(note.body, color = Ink, fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 6.dp, bottom = 5.dp))
                    }
                }
            }
        }
    }
}
