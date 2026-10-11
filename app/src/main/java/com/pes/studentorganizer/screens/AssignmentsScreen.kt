package com.pes.studentorganizer.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pes.studentorganizer.components.AssignmentCard
import com.pes.studentorganizer.model.Assignment
import com.pes.studentorganizer.model.sampleAssignments
import com.pes.studentorganizer.ui.theme.StudentOrganizerTheme

val filterOptions = listOf("All", "Pending", "Completed")

// Screen 2: All assignments with a status filter and completion toggles.
@Composable
fun AssignmentsScreen(
    assignments: List<Assignment>,
    onToggleComplete: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // The selected filter only matters to this screen, so the state lives here
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredAssignments = when (selectedFilter) {
        "Pending" -> assignments.filter { !it.isCompleted }
        "Completed" -> assignments.filter { it.isCompleted }
        else -> assignments
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Filter Row: horizontal row of RadioButtons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (option in filterOptions) {
                Row(
                    modifier = Modifier.clickable { selectedFilter = option },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedFilter == option,
                        onClick = { selectedFilter = option }
                    )
                    Text(option)
                }
            }
        }
        HorizontalDivider()

        if (filteredAssignments.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No ${selectedFilter.lowercase()} assignments",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            // Main content: LazyColumn of AssignmentCards.
            // key = id keeps each card's state correct when the list changes.
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Showing ${filteredAssignments.size} of ${assignments.size} assignments",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                items(filteredAssignments, key = { it.id }) { assignment ->
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AssignmentsScreenPreview() {
    StudentOrganizerTheme {
        AssignmentsScreen(assignments = sampleAssignments, onToggleComplete = {})
    }
}
