package com.example.studyresourcemanager.ui

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
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyresourcemanager.data.SampleStudyRepository
import com.example.studyresourcemanager.data.StudyResource

@Composable
internal fun SearchScreen(
    resources: List<StudyResource>,
    savedIds: Set<String>,
    query: String,
    onQueryChange: (String) -> Unit,
    selectedSubject: String?,
    onSubjectChange: (String?) -> Unit,
    onOpen: (StudyResource) -> Unit,
    onToggleSaved: (String) -> Unit
) {
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
            onValueChange = onQueryChange,
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
                onClick = { onSubjectChange(null) },
                label = { Text("All") }
            )
            SampleStudyRepository.subjects.forEach { subject ->
                FilterChip(
                    selected = selectedSubject == subject,
                    onClick = { onSubjectChange(if (selectedSubject == subject) null else subject) },
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
                    onOpen = { onOpen(resource) },
                    onBookmark = { onToggleSaved(resource.id) }
                )
            }
        }
    }
}

@Composable
internal fun SavedScreen(
    resources: List<StudyResource>,
    onRemove: (String) -> Unit,
    onOpen: (StudyResource) -> Unit,
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
                    ResourceRow(resource, true, { onOpen(resource) }) { onRemove(resource.id) }
                }
            }
        }
    }
}

@Composable
internal fun ResourceRow(
    resource: StudyResource,
    isSaved: Boolean,
    onOpen: () -> Unit,
    onBookmark: () -> Unit
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier.weight(1f).clickable(onClick = onOpen).padding(vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(width = 42.dp, height = 52.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Accent.copy(alpha = .12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, tint = Accent, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f).padding(start = 12.dp, end = 4.dp)) {
                Text(resource.title, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(resource.author, color = Muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 3.dp))
                Text(resource.subject.uppercase(), color = Accent, fontSize = 9.sp, letterSpacing = .5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
            }
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
