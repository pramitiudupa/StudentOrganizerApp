package com.pes.studentorganizer.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pes.studentorganizer.R
import com.pes.studentorganizer.ui.theme.StudentOrganizerTheme

// Header card with the student's avatar, name, ID and major.
@Composable
fun StudentProfile(
    studentName: String,
    studentId: String,
    major: String,
    avatarRes: Int,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image loaded from a drawable resource with painterResource
            Image(
                painter = painterResource(id = avatarRes),
                contentDescription = stringResource(id = R.string.student_avatar_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .border(BorderStroke(3.dp, MaterialTheme.colorScheme.primary), CircleShape)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = studentName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = studentId,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = major,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StudentProfilePreview() {
    StudentOrganizerTheme {
        StudentProfile(
            studentName = "Pramiti Udupa",
            studentId = "PES1UG24CS331",
            major = "Computer Science & Engineering",
            avatarRes = R.drawable.ic_student_avatar,
            modifier = Modifier.padding(16.dp)
        )
    }
}
