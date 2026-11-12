package com.example.studyresourcemanager.ui.components

import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.studyresourcemanager.R
import com.example.studyresourcemanager.ui.Accent

/**
 * Cover/thumbnail for a resource, loaded with Glide.
 *
 * Three situations are handled without redesigning the caller's layout:
 *  - no URL (most sample data): the book icon used by the screens before Glide existed;
 *  - URL present: the image is downloaded and cropped to fill the box;
 *  - URL broken/slow: [R.drawable.ic_book_placeholder] is shown until (or instead of)
 *    the real image, so a list row never jumps or crashes.
 *
 * The ImageView is created through [AndroidView] because Glide's Compose integration is
 * still a beta artifact; this keeps the dependency to plain `com.github.bumptech.glide:glide`.
 */
@Composable
internal fun ResourceThumbnail(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(2.dp)
) {
    val context = LocalContext.current
    Box(
        modifier = modifier.clip(shape).background(Accent.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        if (url.isNullOrBlank()) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                contentDescription = contentDescription,
                tint = Accent,
                modifier = Modifier.size(22.dp)
            )
        } else {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { viewContext ->
                    ImageView(viewContext).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        this.contentDescription = contentDescription
                    }
                },
                update = { imageView ->
                    Glide.with(context)
                        .load(url)
                        .placeholder(R.drawable.ic_book_placeholder)
                        .error(R.drawable.ic_book_placeholder)
                        .centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imageView)
                },
                onRelease = { imageView -> Glide.with(context).clear(imageView) }
            )
        }
    }
}
