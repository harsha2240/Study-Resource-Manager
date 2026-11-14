package com.example.studyresourcemanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyresourcemanager.data.StudyResource

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
