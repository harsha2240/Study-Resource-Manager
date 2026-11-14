package com.example.studyresourcemanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
    onToggleSaved: (StudyResource) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    showingSampleFallback: Boolean = false
) {
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
            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text("Searching online libraries…", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(start = 10.dp))
                    }
                }
            }
            if (errorMessage != null) {
                item {
                    Text(
                        if (showingSampleFallback) "$errorMessage Showing bundled sample matches instead."
                        else errorMessage,
                        color = Muted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
            if (resources.isEmpty() && !isLoading && errorMessage == null) {
                item {
                    Text(
                        "No resources match that search. Try another title or subject.",
                        color = Muted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            }
            items(resources, key = { it.id }) { resource ->
                ResourceRow(
                    resource = resource,
                    isSaved = resource.id in savedIds,
                    onOpen = { onOpen(resource) },
                    onBookmark = { onToggleSaved(resource) }
                )
            }
        }
    }
}
