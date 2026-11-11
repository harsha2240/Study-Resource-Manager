package com.example.studyresourcemanager.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.studyresourcemanager.data.StudyResource

@Composable
internal fun HomeScreen(
    onSearch: () -> Unit,
    onNotes: () -> Unit,
    onSettings: () -> Unit,
    subjects: List<String>,
    onSubject: (String) -> Unit,
    featured: StudyResource,
    nextToRead: StudyResource,
    recent: List<StudyResource>,
    savedIds: Set<String>,
    onOpen: (StudyResource) -> Unit,
    onBookmark: (String) -> Unit,
) {
    val featuredSaved = featured.id in savedIds
    val onToggleFeaturedSaved = { onBookmark(featured.id) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 20.dp)
    ) {
        item { HomeHeader(onSearch, onSettings) }

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
                    subjects.forEachIndexed { index, subject ->
                        if (index > 0) {
                            Box(Modifier.padding(horizontal = 12.dp).width(1.dp).height(22.dp).background(Rule))
                        }
                        val label = when (subject) {
                            "Data Structures & Algorithms" -> "DSA"
                            "Database Systems" -> "DBMS"
                            "Machine Learning" -> "ML"
                            "Computer Networks" -> "Networks"
                            else -> subject
                        }
                        TextButton(onClick = { onSubject(subject) }) {
                            Text(label, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        item { HorizontalDivider(color = Rule) }

        item {
            Column(Modifier.padding(top = 19.dp, bottom = 13.dp)) {
                SectionLabel("FEATURED RESOURCE")
                Spacer(Modifier.height(12.dp))
                FeaturedArtwork(featuredSaved, onToggleFeaturedSaved, onOpen = { onOpen(featured) })
                Spacer(Modifier.height(12.dp))
                Text(featured.title, modifier = Modifier.clickable { onOpen(featured) }, color = Ink, fontSize = 21.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold)
                Text("${featured.author} · ${featured.source} book", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 3.dp))
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
                    body = nextToRead.title,
                    detail = nextToRead.source,
                    icon = Icons.AutoMirrored.Outlined.MenuBook,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpen(nextToRead) }
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
        if (recent.isNotEmpty()) {
            item {
                HorizontalDivider(color = Rule)
                Spacer(Modifier.height(16.dp))
                SectionLabel("RECENTLY VIEWED")
            }
            items(recent, key = { it.id }) { resource ->
                ResourceRow(resource, resource.id in savedIds, { onOpen(resource) }) {
                    onBookmark(resource.id)
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(onSearch: () -> Unit, onSettings: () -> Unit) {
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
        IconButton(onClick = onSettings) {
            Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Ink)
        }
    }
}

@Composable
internal fun SectionLabel(text: String) {
    Text(text, color = Ink, fontSize = 12.sp, letterSpacing = .8.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun FeaturedArtwork(isSaved: Boolean, onToggleSaved: () -> Unit, onOpen: () -> Unit) {
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
            Surface(onClick = onOpen, color = Paper, shape = RoundedCornerShape(topStart = 2.dp), modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = "View featured resource", tint = Ink) }
            }
            Surface(color = Accent, shape = RoundedCornerShape(topStart = 2.dp), modifier = Modifier.size(48.dp)) {
                IconButton(onClick = onToggleSaved, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (isSaved) "Remove featured resource from saved" else "Save featured resource",
                        tint = Color(0xFF111111)
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
