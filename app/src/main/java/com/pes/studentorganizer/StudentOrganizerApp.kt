package com.pes.studentorganizer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pes.studentorganizer.components.AppBar
import com.pes.studentorganizer.components.BottomBar
import com.pes.studentorganizer.model.sampleAssignments
import com.pes.studentorganizer.model.sampleCourses
import com.pes.studentorganizer.model.sampleStudent
import com.pes.studentorganizer.navigation.Screen
import com.pes.studentorganizer.screens.AddAssignmentScreen
import com.pes.studentorganizer.screens.AssignmentsScreen
import com.pes.studentorganizer.screens.DashboardScreen
import com.pes.studentorganizer.screens.ProfileScreen
import com.pes.studentorganizer.screens.SettingsScreen

// Task 6: central navigation + Scaffold.
// The app-wide state lives here and is passed DOWN to the screens;
// the screens send events back UP through callbacks (unidirectional data flow).
@Composable
fun StudentOrganizerApp(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    // Step 1: NavHostController - manages the back stack.
    // rememberNavController keeps it across recompositions.
    val navController = rememberNavController()

    // The route of the screen currently on top of the back stack.
    // Used to change the title, highlight the bottom tab and hide the BottomBar / FAB.
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // App state (sample data, no database)
    var student by remember { mutableStateOf(sampleStudent) }
    var assignments by remember { mutableStateOf(sampleAssignments) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var defaultStartScreen by remember { mutableStateOf("Dashboard") }
    val courses = sampleCourses

    // Flip isCompleted of one assignment.
    // A new list is created, so Compose sees the change and recomposes.
    val toggleComplete: (Int) -> Unit = { id ->
        assignments = assignments.map { assignment ->
            if (assignment.id == id) assignment.copy(isCompleted = !assignment.isCompleted) else assignment
        }
    }

    // Leave the Add Assignment screen.
    // popBackStack(route, inclusive = true) returns false if the screen is already gone,
    // so a double tap on Save / Cancel cannot pop the Dashboard as well.
    val closeAddScreen: () -> Boolean = {
        navController.popBackStack(Screen.AddAssignment.route, inclusive = true)
    }

    val isAddScreen = currentRoute == Screen.AddAssignment.route

    val title = when (currentRoute) {
        Screen.Dashboard.route -> "Welcome back, ${student.name.substringBefore(" ")}!"
        Screen.Assignments.route -> "My Assignments"
        Screen.Profile.route -> "Student Profile"
        Screen.Settings.route -> "Settings"
        Screen.AddAssignment.route -> "Add New Assignment"
        else -> "Student Organizer"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppBar(
                title = title,
                canNavigateBack = isAddScreen,
                onBackClick = { closeAddScreen() }
            )
        },
        bottomBar = {
            // BottomBar is hidden on the add_assignment screen
            if (!isAddScreen) {
                BottomBar(navController = navController, currentRoute = currentRoute)
            }
        },
        floatingActionButton = {
            // '+' FAB only on the Dashboard and Assignments screens
            if (currentRoute == Screen.Dashboard.route || currentRoute == Screen.Assignments.route) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddAssignment.route) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Assignment")
                }
            }
        }
    ) { innerPadding ->
        // Step 2: NavHost - the container where the current screen is displayed.
        // innerPadding keeps the content clear of the top bar and bottom bar.
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Navigation graph: one composable() destination per route
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    courses = courses,
                    assignments = assignments,
                    onToggleComplete = toggleComplete
                )
            }
            composable(Screen.Assignments.route) {
                AssignmentsScreen(
                    assignments = assignments,
                    onToggleComplete = toggleComplete
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    student = student,
                    onProfileUpdate = { name, major ->
                        student = student.copy(name = name, major = major)
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    isDarkMode = isDarkMode,
                    onDarkModeChange = onDarkModeChange,
                    notificationsEnabled = notificationsEnabled,
                    onNotificationsChange = { notificationsEnabled = it },
                    defaultStartScreen = defaultStartScreen,
                    onDefaultStartScreenChange = { defaultStartScreen = it }
                )
            }
            composable(Screen.AddAssignment.route) {
                AddAssignmentScreen(
                    courseOptions = courses,
                    onSave = { newAssignment ->
                        if (closeAddScreen()) {
                            // assignments are never deleted, so size + 1 is always a new id
                            val nextId = assignments.size + 1
                            assignments = assignments + newAssignment.copy(id = nextId)
                        }
                    },
                    onCancel = { closeAddScreen() }
                )
            }
        }
    }
}
