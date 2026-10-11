package com.pes.studentorganizer.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pes.studentorganizer.components.AssignmentCard
import com.pes.studentorganizer.components.CourseInfo
import com.pes.studentorganizer.model.Assignment
import com.pes.studentorganizer.model.Course
import com.pes.studentorganizer.model.sampleAssignments
import com.pes.studentorganizer.model.sampleCourses
import com.pes.studentorganizer.ui.theme.StudentOrganizerTheme

// Screen 1: Dashboard - main hub.
// The greeting top bar and the '+' FAB are provided by the Scaffold in StudentOrganizerApp.
@Composable
fun DashboardScreen(
    courses: List<Course>,
    assignments: List<Assignment>,
    onToggleComplete: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val pendingCount = assignments.count { !it.isCompleted }

    // Top 2 upcoming deadlines: pending assignments, earliest due date first
    val urgentAssignments = assignments
        .filter { !it.isCompleted }
        .sortedBy { it.dueDate }
        .take(2)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        // extra bottom padding so the FAB never covers the last card
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Stats Row: two elevated cards sharing the width equally (weight)
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = "Active Courses",
                    value = courses.size,
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Pending Tasks",
                    value = pendingCount,
                    icon = Icons.Default.PendingActions,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Active Courses Section: horizontal scrolling LazyRow of CourseInfo cards
        item {
            SectionTitle(text = "Active Courses")
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(courses, key = { it.courseCode }) { course ->
                    CourseInfo(
                        courseName = course.courseName,
                        courseCode = course.courseCode,
                        instructor = course.instructor,
                        progress = course.progress,
                        modifier = Modifier.width(250.dp)
                    )
                }
            }
        }

        // Urgent Deadlines Section: a plain Column with the top 2 AssignmentCards
        item {
            SectionTitle(text = "Urgent Deadlines")
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (urgentAssignments.isEmpty()) {
                    Text("All caught up! No pending assignments.")
                }
                for (assignment in urgentAssignments) {
                    AssignmentCard(
                        title = assignment.title,
                        course = assignment.course,
                        dueDate = assignment.dueDate,
                        priority = assignment.priority,
                        isCompleted = assignment.isCompleted,
                        onToggleComplete = { onToggleComplete(assignment.id) }
                    )
                }
            }
        }
    }
}

// Small elevated card showing one number, e.g. "Active Courses 4"
@Composable
fun StatCard(
    label: String,
    value: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$value",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DashboardScreenPreview() {
    StudentOrganizerTheme {
        DashboardScreen(
            courses = sampleCourses,
            assignments = sampleAssignments,
            onToggleComplete = {}
        )
    }
}
