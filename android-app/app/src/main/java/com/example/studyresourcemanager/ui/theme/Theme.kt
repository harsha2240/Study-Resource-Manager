package com.example.studyresourcemanager.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF8A79),
    surface = Color(0xFF181818),
    onSurface = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFFBFBFBF),
    outlineVariant = Color(0xFF484848)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFF04432),
    onPrimary = Color(0xFF111111),
    surface = Color.White,
    onSurface = Color(0xFF111111),
    onSurfaceVariant = Color(0xFF666666),
    outlineVariant = Color(0xFFD9D9D9)
)

@Composable
fun StudyResourceManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
