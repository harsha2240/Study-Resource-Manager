package com.example.studyresourcemanager

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.core.content.IntentCompat
import com.example.studyresourcemanager.data.StudyResource
import com.example.studyresourcemanager.ui.ResourceDetailsScreen

class ResourceDetailsActivity : StudyActivity() {
    private val resource by lazy {
        IntentCompat.getSerializableExtra(intent, EXTRA_RESOURCE, StudyResource::class.java)
    }

    @Composable
    override fun Screen() {
        val item = resource
        ResourceDetailsScreen(
            resource = item,
            isSaved = item != null && item.id in session.savedIds,
            onBack = ::finish,
            onBookmark = { item?.let(session::toggleSaved) }
        )
    }

    internal companion object {
        private const val EXTRA_RESOURCE = "com.example.studyresourcemanager.RESOURCE"

        fun intent(context: Context, resource: StudyResource): Intent =
            Intent(context, ResourceDetailsActivity::class.java).putExtra(EXTRA_RESOURCE, resource)
    }
}
