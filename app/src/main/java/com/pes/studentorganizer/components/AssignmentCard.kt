package com.pes.studentorganizer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pes.studentorganizer.ui.theme.PriorityHigh
import com.pes.studentorganizer.ui.theme.PriorityLow
import com.pes.studentorganizer.ui.theme.PriorityMedium
import com.pes.studentorganizer.ui.theme.StudentOrganizerTheme

// Interactive card for one assignment.
// Stateless: isCompleted comes from the parent and a tap on the checkbox
// is sent back up through onToggleComplete (state down, events up).
@Composable
fun AssignmentCard(
    title: String,
    course: String,
    dueDate: String,
    priority: String,
    isCompleted: Boolean,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            // completed assignments are faded out
            .alpha(if (isCompleted) 0.6f else 1f),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isCompleted,
                onCheckedChange = { onToggleComplete() }
            )
            // weight(1f) lets the text column take all the space left after the checkbox and badge
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = course,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCompleted) "Completed" else "Due: $dueDate",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            PriorityBadge(priority = priority)
        }
    }
}

// Small coloured label showing the priority (High / Medium / Low)
@Composable
fun PriorityBadge(
    priority: String,
    modifier: Modifier = Modifier
) {
    val badgeColor = when (priority) {
        "High" -> PriorityHigh
        "Medium" -> PriorityMedium
        else -> PriorityLow
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(badgeColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = priority,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AssignmentCardPreview() {
    StudentOrganizerTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AssignmentCard(
                title = "Linked List Implementation",
                course = "CS101",
                dueDate = "2026-10-12",
                priority = "High",
                isCompleted = false,
                onToggleComplete = {}
            )
            Spacer(modifier = Modifier.height(8.dp))
            AssignmentCard(
                title = "Stack and Queue Lab",
                course = "CS101",
                dueDate = "2026-10-05",
                priority = "Low",
                isCompleted = true,
                onToggleComplete = {}
            )
        }
    }
}
