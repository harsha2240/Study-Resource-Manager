package com.example.studyresourcemanager.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.studyresourcemanager.R
import com.example.studyresourcemanager.data.StudyResource
import com.example.studyresourcemanager.ui.components.ResourceThumbnail

@Composable
internal fun ResourceDetailsScreen(
    resource: StudyResource?,
    isSaved: Boolean,
    onBack: () -> Unit,
    onBookmark: (String) -> Unit
) {
    val context = LocalContext.current
    var actionFailed by rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ScreenHeader("Resource details", onBack)
        if (resource == null) {
            Text("This resource is unavailable.", color = Muted)
            return@Column
        }

        // Remote catalogues supply a cover; sample data has none, so no empty block is shown.
        if (!resource.thumbnailUrl.isNullOrBlank()) {
            ResourceThumbnail(
                url = resource.thumbnailUrl,
                contentDescription = resource.title,
                modifier = Modifier.fillMaxWidth().height(190.dp)
            )
        }

        SectionLabel(resource.subject.uppercase())
        Text(resource.title, color = Ink, style = MaterialTheme.typography.headlineMedium)
        Text(resource.author, color = Ink, style = MaterialTheme.typography.titleMedium)
        Text("${resource.source} · ${resource.kind.lowercase()}", color = Muted)
        Text(resource.description, color = Ink, style = MaterialTheme.typography.bodyLarge)

        Button(onClick = {
            actionFailed = !context.launchResourceIntent(Intent(Intent.ACTION_VIEW, Uri.parse(resource.url)))
        }) { Text(stringResource(R.string.open_resource_link)) }
        OutlinedButton(onClick = {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, resource.title)
                putExtra(Intent.EXTRA_TEXT, "${resource.title}\n${resource.url}")
            }
            actionFailed = !context.launchResourceIntent(
                Intent.createChooser(shareIntent, context.getString(R.string.share_resource_link))
            )
        }) { Text(stringResource(R.string.share_resource_link)) }
        OutlinedButton(onClick = { onBookmark(resource.id) }) {
            Text(if (isSaved) "Remove bookmark" else "Bookmark")
        }
        if (actionFailed) {
            Text(stringResource(R.string.resource_action_unavailable), color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
internal fun ScreenHeader(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Ink)
        }
        Text(title, color = Ink, style = MaterialTheme.typography.titleLarge)
    }
}

private fun Context.launchResourceIntent(intent: Intent): Boolean = try {
    startActivity(intent)
    true
} catch (_: ActivityNotFoundException) {
    false
}
