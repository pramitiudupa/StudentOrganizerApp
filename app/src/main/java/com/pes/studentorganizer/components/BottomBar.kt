package com.pes.studentorganizer.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.pes.studentorganizer.navigation.Screen
import com.pes.studentorganizer.ui.theme.StudentOrganizerTheme

// One tab of the bottom bar
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen
)

val bottomNavItems = listOf(
    BottomNavItem("Dashboard", Icons.Default.Dashboard, Screen.Dashboard),
    BottomNavItem("Assignments", Icons.AutoMirrored.Filled.Assignment, Screen.Assignments),
    BottomNavItem("Profile", Icons.Default.Person, Screen.Profile),
    BottomNavItem("Settings", Icons.Default.Settings, Screen.Settings)
)

// Bottom navigation bar with 4 tabs.
// currentRoute is used to highlight the tab of the screen being shown.
@Composable
fun BottomBar(
    navController: NavHostController,
    currentRoute: String?,
    modifier: Modifier = Modifier
) {
    // NavigationBar is the Material 3 version of BottomNavigation
    NavigationBar(modifier = modifier) {
        for (item in bottomNavItems) {
            NavigationBarItem(
                selected = currentRoute == item.screen.route,
                onClick = {
                    if (currentRoute != item.screen.route) {
                        if (item.screen == Screen.Dashboard) {
                            // Dashboard is always at the bottom of the back stack,
                            // so going "home" just pops every screen above it.
                            navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                        } else {
                            // popUpTo keeps the back stack short:
                            // Dashboard -> (one tab). Pressing Back from a tab returns to Dashboard.
                            navController.navigate(item.screen.route) {
                                popUpTo(Screen.Dashboard.route)
                            }
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomBarPreview() {
    StudentOrganizerTheme {
        BottomBar(navController = rememberNavController(), currentRoute = Screen.Dashboard.route)
    }
}
