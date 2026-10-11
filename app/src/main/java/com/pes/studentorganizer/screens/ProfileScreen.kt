package com.pes.studentorganizer.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pes.studentorganizer.components.StudentProfile
import com.pes.studentorganizer.model.Student
import com.pes.studentorganizer.model.sampleStudent
import com.pes.studentorganizer.ui.theme.StudentOrganizerTheme

// Screen 3: Student information and academic statistics.
@Composable
fun ProfileScreen(
    student: Student,
    onProfileUpdate: (name: String, major: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header: the reusable StudentProfile composable
        item {
            StudentProfile(
                studentName = student.name,
                studentId = student.studentId,
                major = student.major,
                avatarRes = student.avatarRes
            )
        }

        // Academic Stats Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Academic Stats",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(value = "${student.gpa}", label = "GPA")
                        StatItem(value = "${student.completedCredits}", label = "Credits")
                        StatItem(value = student.currentSemester, label = "Semester")
                    }
                }
            }
        }

        // Quick Actions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile")
                }
                OutlinedButton(
                    onClick = {
                        // Implicit intent: Android shows every app that can share text
                        val summary = "Academic Summary\n" +
                                "Name: ${student.name}\n" +
                                "Student ID: ${student.studentId}\n" +
                                "Major: ${student.major}\n" +
                                "GPA: ${student.gpa}\n" +
                                "Completed Credits: ${student.completedCredits}\n" +
                                "Current Semester: ${student.currentSemester}"
                        val sendIntent = Intent(Intent.ACTION_SEND)
                        sendIntent.type = "text/plain"
                        sendIntent.putExtra(Intent.EXTRA_TEXT, summary)
                        context.startActivity(Intent.createChooser(sendIntent, "Export Academic Summary"))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Academic Summary")
                }
            }
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            currentName = student.name,
            currentMajor = student.major,
            onDismiss = { showEditDialog = false },
            onSave = { name, major ->
                onProfileUpdate(name, major)
                showEditDialog = false
            }
        )
    }
}

// One value + label inside the Academic Stats card
@Composable
fun StatItem(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

// Dialog to edit the student's name and major
@Composable
fun EditProfileDialog(
    currentName: String,
    currentMajor: String,
    onDismiss: () -> Unit,
    onSave: (name: String, major: String) -> Unit
) {
    // Draft values: only copied to the real profile when Save is pressed
    var name by remember { mutableStateOf(currentName) }
    var major by remember { mutableStateOf(currentMajor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                OutlinedTextField(
                    value = major,
                    onValueChange = { major = it },
                    label = { Text("Major") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && major.isNotBlank(),
                onClick = { onSave(name.trim(), major.trim()) }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfileScreenPreview() {
    StudentOrganizerTheme {
        ProfileScreen(student = sampleStudent, onProfileUpdate = { _, _ -> })
    }
}
