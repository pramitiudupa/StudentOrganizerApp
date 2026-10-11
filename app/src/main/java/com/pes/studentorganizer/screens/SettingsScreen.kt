package com.pes.studentorganizer.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pes.studentorganizer.ui.theme.StudentOrganizerTheme

val startScreenOptions = listOf("Dashboard", "Assignments")

// Screen 4: App preferences.
// Stateless: every value is hoisted to StudentOrganizerApp / MainActivity,
// so the choices are kept when the user moves to another tab and comes back.
@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    notificationsEnabled: Boolean,
    onNotificationsChange: (Boolean) -> Unit,
    defaultStartScreen: String,
    onDefaultStartScreenChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Preferences List: a Column of toggleable items
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Dark Mode: Row with a text label and a Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingLabel(
                icon = Icons.Default.DarkMode,
                title = "Dark Mode",
                subtitle = "Use a dark colour theme",
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = isDarkMode,
                onCheckedChange = onDarkModeChange
            )
        }
        HorizontalDivider()

        // Push Notifications: Row with a text label and a Checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingLabel(
                icon = Icons.Default.Notifications,
                title = "Push Notifications",
                subtitle = "Get alerts for upcoming deadlines",
                modifier = Modifier.weight(1f)
            )
            Checkbox(
                checked = notificationsEnabled,
                onCheckedChange = onNotificationsChange
            )
        }
        HorizontalDivider()

        // Default Start Screen: radio button group
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            SettingLabel(
                icon = Icons.Default.Home,
                title = "Default Start Screen",
                subtitle = "Screen shown when the app opens"
            )
            for (option in startScreenOptions) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDefaultStartScreenChange(option) }
                        .padding(start = 28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = defaultStartScreen == option,
                        onClick = { onDefaultStartScreenChange(option) }
                    )
                    Text(option)
                }
            }
        }
    }
}

// Icon + title + subtitle used by every preference row
@Composable
fun SettingLabel(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SettingsScreenPreview() {
    StudentOrganizerTheme {
        SettingsScreen(
            isDarkMode = false,
            onDarkModeChange = {},
            notificationsEnabled = true,
            onNotificationsChange = {},
            defaultStartScreen = "Dashboard",
            onDefaultStartScreenChange = {}
        )
    }
}
