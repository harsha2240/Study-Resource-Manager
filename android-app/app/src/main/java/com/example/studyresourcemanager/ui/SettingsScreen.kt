package com.example.studyresourcemanager.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

internal enum class ThemeChoice(val label: String) {
    LIGHT("Light"), DARK("Dark"), SYSTEM("Follow device")
}

@Composable
internal fun SettingsScreen(
    theme: ThemeChoice,
    onThemeChange: (ThemeChoice) -> Unit,
    subjects: List<String>,
    defaultSubject: String?,
    onSubjectChange: (String?) -> Unit,
    onBack: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader("Settings", onBack)
        SectionLabel("APPEARANCE")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeChoice.entries.forEach { option ->
                FilterChip(
                    selected = theme == option,
                    onClick = { onThemeChange(option) },
                    label = { Text(option.label) }
                )
            }
        }

        HorizontalDivider(color = Rule)
        SectionLabel("DEFAULT SUBJECT")
        Text("Choose the filter Search starts with.", color = Muted)
        Column(Modifier.selectableGroup()) {
            (listOf(null) + subjects).forEach { subject ->
                val selected = defaultSubject == subject
                Row(
                    Modifier.fillMaxWidth()
                        .selectable(selected, role = Role.RadioButton, onClick = { onSubjectChange(subject) })
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RadioButton(selected = selected, onClick = null)
                    Text(subject ?: "All subjects", color = Ink)
                }
            }
        }
    }
}
