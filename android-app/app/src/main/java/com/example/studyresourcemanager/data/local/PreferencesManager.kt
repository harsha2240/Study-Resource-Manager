package com.example.studyresourcemanager.data.local

import android.content.Context
import com.example.studyresourcemanager.ui.ThemeChoice

internal class PreferencesManager(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getDefaultSubject(): String? = prefs.getString(KEY_DEFAULT_SUBJECT, null)

    fun setDefaultSubject(subject: String?) {
        prefs.edit().apply {
            if (subject.isNullOrBlank()) remove(KEY_DEFAULT_SUBJECT)
            else putString(KEY_DEFAULT_SUBJECT, subject)
        }.apply()
    }

    fun getThemeChoice(): ThemeChoice {
        val savedName = prefs.getString(KEY_THEME_CHOICE, ThemeChoice.LIGHT.name)
        return ThemeChoice.entries.firstOrNull { it.name == savedName } ?: ThemeChoice.LIGHT
    }

    fun setThemeChoice(theme: ThemeChoice) {
        prefs.edit().putString(KEY_THEME_CHOICE, theme.name).apply()
    }

    private companion object {
        const val PREFS_NAME = "study_resource_prefs"
        const val KEY_DEFAULT_SUBJECT = "default_subject"
        const val KEY_THEME_CHOICE = "theme_choice"
    }
}
