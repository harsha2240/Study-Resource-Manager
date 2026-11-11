package com.example.studyresourcemanager.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyresourcemanager.data.StudyNote

@Composable
internal fun NotesScreen(
    notes: List<StudyNote>,
    onAdd: (String, String, String) -> Unit,
    onEdit: (StudyNote) -> Unit,
    onDelete: (Int) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var subject by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }
    var editingId by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedSubject by rememberSaveable { mutableStateOf<String?>(null) }
    val subjects = notes.map { it.subject }.distinct().sorted()
    val activeSubject = selectedSubject?.takeIf { it in subjects }
    val visibleNotes = notes.asReversed().filter { activeSubject == null || it.subject == activeSubject }
    val editing = notes.firstOrNull { it.id == editingId }
    if (editing != null) {
        EditNoteDialog(editing, onDismiss = { editingId = null }) {
            onEdit(it)
            editingId = null
        }
    }
    val canSave = title.isNotBlank() && body.isNotBlank()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Paper),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp)
    ) {
        item {
            SectionLabel("YOUR STUDY SPACE")
            Spacer(Modifier.height(8.dp))
            Text("Study notes", color = Ink, fontSize = 29.sp, fontWeight = FontWeight.Medium)
            Text("Capture the ideas you want to remember.", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 5.dp))
            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = Rule)
            Spacer(Modifier.height(16.dp))
            SectionLabel("NEW NOTE")
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(3.dp),
                label = { Text("Note title") }
            )
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(3.dp),
                label = { Text("Subject (optional)") }
            )
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(3.dp),
                label = { Text("Your note") }
            )
            Button(
                onClick = {
                    onAdd(title.trim(), subject.trim().ifBlank { "General" }, body.trim())
                    selectedSubject = null
                    title = ""
                    subject = ""
                    body = ""
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent)
            ) { Text("SAVE NOTE") }
            Spacer(Modifier.height(22.dp))
            SectionLabel("YOUR NOTES · ${visibleNotes.size}")
            if (subjects.size > 1) {
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (listOf(null) + subjects).forEach { filter ->
                        FilterChip(
                            selected = activeSubject == filter,
                            onClick = { selectedSubject = filter },
                            label = { Text(filter ?: "All subjects") }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        if (notes.isEmpty()) {
            item {
                Text("Your saved notes will appear here.", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(vertical = 14.dp))
            }
        } else {
            items(visibleNotes, key = { it.id }) { note ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    shape = RoundedCornerShape(3.dp),
                    colors = CardDefaults.cardColors(containerColor = Paper),
                    border = BorderStroke(1.dp, Rule)
                ) {
                    Column(Modifier.fillMaxWidth().padding(start = 14.dp, end = 8.dp, top = 11.dp, bottom = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(note.title, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text(note.subject.uppercase(), color = Accent, fontSize = 9.sp, letterSpacing = .6.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 3.dp))
                            }
                            TextButton(onClick = { editingId = note.id }) { Text("EDIT", fontSize = 10.sp) }
                            TextButton(onClick = { onDelete(note.id) }) { Text("DELETE", color = Muted, fontSize = 10.sp) }
                        }
                        Text(note.body, color = Ink, fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 6.dp, bottom = 5.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EditNoteDialog(note: StudyNote, onDismiss: () -> Unit, onSave: (StudyNote) -> Unit) {
    var title by rememberSaveable(note.id) { mutableStateOf(note.title) }
    var subject by rememberSaveable(note.id) { mutableStateOf(note.subject) }
    var body by rememberSaveable(note.id) { mutableStateOf(note.body) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit note") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Note title") }, singleLine = true)
                OutlinedTextField(subject, { subject = it }, label = { Text("Subject (optional)") }, singleLine = true)
                OutlinedTextField(body, { body = it }, label = { Text("Your note") }, minLines = 3, maxLines = 5)
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && body.isNotBlank(),
                onClick = { onSave(note.copy(title = title.trim(), subject = subject.trim().ifBlank { "General" }, body = body.trim())) }
            ) { Text("Save changes") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
