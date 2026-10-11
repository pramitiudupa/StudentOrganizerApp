package com.pes.studentorganizer.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pes.studentorganizer.model.Course
import com.pes.studentorganizer.model.sampleCourses
import com.pes.studentorganizer.ui.theme.StudentOrganizerTheme

val priorityOptions = listOf("Low", "Medium", "High")

// State-hoisted (stateless) form for creating an assignment.
// Every field value comes IN as a parameter, and every change goes OUT
// through an onXChange callback - the parent screen owns the state.
//
// actions is a slot (a composable lambda parameter): the caller decides
// which buttons appear at the bottom of the form.
@Composable
fun AssignmentForm(
    title: String,
    course: String,
    dueDate: String,
    priority: String,
    description: String,
    reminderEnabled: Boolean,
    onTitleChange: (String) -> Unit,
    onCourseChange: (String) -> Unit,
    onDueDateChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onReminderChange: (Boolean) -> Unit,
    courseOptions: List<Course>,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {}
) {
    // Is the course dropdown open? Only the form needs to know this.
    var courseMenuExpanded by remember { mutableStateOf(false) }

    // Form container: LazyColumn, so the form can scroll.
    // imePadding() shrinks the list by the keyboard's height so the
    // field being typed in is never hidden behind the keyboard.
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Assignment Title (required, single line)
        item {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Assignment Title *") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 2. Course Name/Code - type it, or pick one from the dropdown
        item {
            Box {
                OutlinedTextField(
                    value = course,
                    onValueChange = onCourseChange,
                    label = { Text("Course Name/Code *") },
                    placeholder = { Text("e.g. CS101") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Next
                    ),
                    trailingIcon = {
                        IconButton(onClick = { courseMenuExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Choose course")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(
                    expanded = courseMenuExpanded,
                    onDismissRequest = { courseMenuExpanded = false }
                ) {
                    for (option in courseOptions) {
                        DropdownMenuItem(
                            text = { Text("${option.courseCode} - ${option.courseName}") },
                            onClick = {
                                onCourseChange(option.courseCode)
                                courseMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // 3. Due Date - text field with a trailing calendar icon
        item {
            OutlinedTextField(
                value = dueDate,
                onValueChange = onDueDateChange,
                label = { Text("Due Date *") },
                placeholder = { Text("YYYY-MM-DD") },
                singleLine = true,
                trailingIcon = {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Due date")
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 4. Priority Level - label + Row of RadioButtons
        item {
            Column {
                Text(
                    text = "Priority Level",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (option in priorityOptions) {
                        Row(
                            modifier = Modifier
                                .clickable { onPriorityChange(option) }
                                .padding(end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = priority == option,
                                onClick = { onPriorityChange(option) }
                            )
                            Text(option)
                        }
                    }
                }
            }
        }

        // 5. Description / Notes - multi-line
        item {
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Description / Notes") },
                minLines = 3,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 6. Reminder Setting - Row with a Checkbox
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onReminderChange(!reminderEnabled) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = reminderEnabled,
                    onCheckedChange = onReminderChange
                )
                Text("Enable Reminder Notification")
            }
        }

        // Slot: whatever buttons the caller passed in
        item {
            actions()
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun AssignmentFormPreview() {
    StudentOrganizerTheme {
        AssignmentForm(
            title = "Compose UI Lab",
            course = "CS343",
            dueDate = "2026-10-13",
            priority = "High",
            description = "",
            reminderEnabled = true,
            onTitleChange = {},
            onCourseChange = {},
            onDueDateChange = {},
            onPriorityChange = {},
            onDescriptionChange = {},
            onReminderChange = {},
            courseOptions = sampleCourses,
            actions = {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(onClick = {}, modifier = Modifier.weight(1f)) { Text("Save Assignment") }
                }
            }
        )
    }
}
