package com.pes.studentorganizer.navigation

// Task 1: all 5 routes defined in one sealed class.
// A sealed class has a fixed set of subclasses, so every route is known
// at compile time and we never mistype a route string.
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Assignments : Screen("assignments")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object AddAssignment : Screen("add_assignment")
}
