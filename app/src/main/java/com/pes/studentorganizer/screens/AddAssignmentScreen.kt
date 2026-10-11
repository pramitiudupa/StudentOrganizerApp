package com.pes.studentorganizer.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pes.studentorganizer.components.AssignmentForm
import com.pes.studentorganizer.model.Assignment
import com.pes.studentorganizer.model.Course
import com.pes.studentorganizer.model.sampleCourses
import com.pes.studentorganizer.ui.theme.StudentOrganizerTheme

// Screen 5: Form to create a new assignment.
// This screen is the STATEFUL parent: it owns the form state and passes
// it down to the stateless AssignmentForm (state hoisting).
@Composable
fun AddAssignmentScreen(
    courseOptions: List<Course>,
    onSave: (Assignment) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Task 5: form state with remember { mutableStateOf() }
    var title by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var description by remember { mutableStateOf("") }
    var reminderEnabled by remember { mutableStateOf(false) }

    // Validation: Save is enabled only when all required fields are filled
    val isFormValid = title.isNotBlank() && course.isNotBlank() && dueDate.isNotBlank()

    AssignmentForm(
        title = title,
        course = course,
        dueDate = dueDate,
        priority = priority,
        description = description,
        reminderEnabled = reminderEnabled,
        onTitleChange = { title = it },
        onCourseChange = { course = it },
        onDueDateChange = { dueDate = it },
        onPriorityChange = { priority = it },
        onDescriptionChange = { description = it },
        onReminderChange = { reminderEnabled = it },
        courseOptions = courseOptions,
        modifier = modifier,
        // Slot content: the action buttons shown at the bottom of the form
        actions = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        enabled = isFormValid,
                        onClick = {
                            val newAssignment = Assignment(
                                id = 0, // the real id is given by StudentOrganizerApp
                                title = title.trim(),
                                course = course.trim(),
                                dueDate = dueDate.trim(),
                                priority = priority,
                                description = description.trim(),
                                reminderEnabled = reminderEnabled
                            )
                            Toast.makeText(context, "Assignment \"${newAssignment.title}\" saved", Toast.LENGTH_SHORT).show()
                            onSave(newAssignment)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Assignment")
                    }
                }
                if (!isFormValid) {
                    Text(
                        text = "* Title, Course and Due Date are required",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AddAssignmentScreenPreview() {
    StudentOrganizerTheme {
        AddAssignmentScreen(courseOptions = sampleCourses, onSave = {}, onCancel = {})
    }
}
