package com.example.studyresourcemanager.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyresourcemanager.data.StudyResource
import com.example.studyresourcemanager.ui.components.ResourceThumbnail

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
            ResourceThumbnail(
                url = resource.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier.size(width = 42.dp, height = 52.dp)
            )
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
