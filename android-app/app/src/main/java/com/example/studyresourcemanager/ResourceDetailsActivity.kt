package com.example.studyresourcemanager

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import com.example.studyresourcemanager.data.StudyResource
import com.example.studyresourcemanager.ui.ResourceDetailsScreen

class ResourceDetailsActivity : StudyActivity() {
    private val resource by lazy {
        session.findResource(intent.getStringExtra(EXTRA_RESOURCE_ID))
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
        private const val EXTRA_RESOURCE_ID = "com.example.studyresourcemanager.RESOURCE_ID"

        fun intent(context: Context, resource: StudyResource): Intent =
            Intent(context, ResourceDetailsActivity::class.java)
                .putExtra(EXTRA_RESOURCE_ID, resource.id)
    }
}
