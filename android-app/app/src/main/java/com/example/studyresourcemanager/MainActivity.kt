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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
                        onSaved = { navController.navigate("bookmarks") },
                        onNotes = { navController.navigate("notes") }
                    )
                }
                composable("search") { SearchScreen() }
                composable("bookmarks") {
                    CollectionScreen(
                        title = "Saved resources",
                        subtitle = "Your bookmarked books and articles will live here.",
                        emptyMessage = "Nothing saved yet",
                        iconRoute = "bookmarks"
                    )
                }
                composable("notes") {
                    CollectionScreen(
                        title = "Study notes",
                        subtitle = "Keep your notes organized by subject.",
                        emptyMessage = "No notes yet",
                        iconRoute = "notes"
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
    onSaved: () -> Unit,
    onNotes: () -> Unit
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
                FeaturedArtwork()
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
                        Text("SAVE", color = Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = onSaved, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Save featured resource", tint = Accent)
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
private fun FeaturedArtwork() {
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
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Bookmark resource", tint = Paper) }
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
private fun SearchScreen() {
    var query by remember { mutableStateOf("") }
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
        Spacer(Modifier.height(22.dp))
        SectionLabel(if (query.isBlank()) "POPULAR SUBJECTS" else "SEARCH RESULTS")
        Spacer(Modifier.height(10.dp))
        if (query.isBlank()) {
            listOf("Data Structures & Algorithms", "Database Systems", "Machine Learning", "Computer Networks").forEachIndexed { index, subject ->
                Row(Modifier.fillMaxWidth().padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("0${index + 1}", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(subject, color = Ink, fontSize = 15.sp, modifier = Modifier.padding(start = 16.dp).weight(1f))
                    Text("›", color = Muted, fontSize = 22.sp)
                }
                HorizontalDivider(color = Rule)
            }
        } else {
            Text("Search providers will appear here when the shared API interface is connected.", color = Muted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun CollectionScreen(title: String, subtitle: String, emptyMessage: String, iconRoute: String) {
    Column(Modifier.fillMaxSize().background(Paper).padding(horizontal = 20.dp, vertical = 24.dp)) {
        SectionLabel(if (iconRoute == "notes") "YOUR STUDY SPACE" else "YOUR LIBRARY")
        Spacer(Modifier.height(8.dp))
        Text(title, color = Ink, fontSize = 29.sp, fontWeight = FontWeight.Medium)
        Text(subtitle, color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 5.dp))
        Spacer(Modifier.height(23.dp))
        HorizontalDivider(color = Rule)
        Column(Modifier.fillMaxWidth().padding(top = 38.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = if (iconRoute == "notes") Icons.Outlined.EditNote else Icons.Outlined.BookmarkBorder,
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(30.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(emptyMessage, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text("Add a resource and it will show up here.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 5.dp))
        }
    }
}
