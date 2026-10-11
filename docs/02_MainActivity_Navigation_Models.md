# Part 2 – MainActivity, Routes, Data Models & Central Navigation

This part covers the app's "skeleton": how it starts, how screens are connected, and where the data lives.

**App flow in one picture:**
```
MainActivity.onCreate()
   └─ setContent {
         isDarkMode state
         StudentOrganizerTheme(darkTheme = isDarkMode) {
            StudentOrganizerApp(...)                 ← app-wide state lives here
               └─ Scaffold(topBar = AppBar, bottomBar = BottomBar, FAB)
                     └─ NavHost(startDestination = "dashboard")
                          ├─ "dashboard"       → DashboardScreen
                          ├─ "assignments"     → AssignmentsScreen
                          ├─ "profile"         → ProfileScreen
                          ├─ "settings"        → SettingsScreen
                          └─ "add_assignment"  → AddAssignmentScreen → AssignmentForm
      }
```

---

## 2.1 `MainActivity.kt`

```kotlin
class MainActivity : ComponentActivity() {
```
- With Compose, **an Activity is still the entry point** of the app (L16 slide "Compose in Android App").
- `ComponentActivity` is the base Activity class that supports `setContent { }` for Compose.

```kotlin
    private val tag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate called")
        enableEdgeToEdge()
```
- `onCreate()` is called **when the activity is first created**. It is always overridden to set up the UI.
- `super.onCreate(...)` must be called. The rule from the slides: "override these callbacks… and should execute the super version".
- `Log.d(tag, ...)` writes a debug message to **Logcat** (a development tool from Unit 2).
- `enableEdgeToEdge()` lets the app draw behind the status bar and navigation bar. On Android 15+ this is the default anyway.

```kotlin
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }

            StudentOrganizerTheme(darkTheme = isDarkMode) {
                StudentOrganizerApp(
                    isDarkMode = isDarkMode,
                    onDarkModeChange = { isDarkMode = it }
                )
            }
        }
    }
```
- `setContent { }` **sets composables as the UI** of this Activity, instead of an XML layout.
- `isDarkMode` is **state**: `mutableStateOf(false)` creates observable state, `remember` keeps it across recompositions, and `by` (property delegation) lets us write `isDarkMode` instead of `isDarkMode.value`.
- **Why is it here and not in SettingsScreen?** The *theme* needs this value, and the theme wraps the whole app. State must live in the **lowest common parent** of everyone who reads it, so it is hoisted all the way up to MainActivity.
- `onDarkModeChange = { isDarkMode = it }` is the **event callback**. `it` is the new Boolean from the Switch. Changing the state triggers **recomposition**, and the theme redraws with dark colours.

```kotlin
    override fun onStart()   { super.onStart();   Log.d(tag, "onStart called") }
    override fun onResume()  { super.onResume();  Log.d(tag, "onResume called") }
    override fun onPause()   { super.onPause();   Log.d(tag, "onPause called") }
    override fun onStop()    { super.onStop();    Log.d(tag, "onStop called") }
    override fun onDestroy() { super.onDestroy(); Log.d(tag, "onDestroy called") }
```
- The **Activity lifecycle callbacks**, called automatically by the ActivityManager:

| Callback | When |
|---|---|
| `onCreate` | Activity first created → set up UI |
| `onStart` | Just before it becomes **visible** |
| `onResume` | Just before the user can **interact** (Running state) |
| `onPause` | Another activity is coming to the front (Paused: visible but not focused) |
| `onStop` | No longer visible (Stopped) |
| `onDestroy` | Before the activity is destroyed |

**Demo tip:** open Logcat, filter `MainActivity`, press the Home button, and you'll see `onPause` → `onStop`. Reopen the app and you'll see `onStart` → `onResume`. (`onRestart` is not overridden here, so it doesn't appear.)

---

## 2.2 `navigation/Screen.kt` (Task 1)

```kotlin
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Assignments : Screen("assignments")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object AddAssignment : Screen("add_assignment")
}
```
- A **route** is the unique String that identifies a destination (L27: "Route – a unique String identifying the destination").
- A **sealed class** can only have the subclasses declared in this file, so the full list of screens is fixed and known at compile time.
- Each screen is an **`object`** (a singleton) because there is only ever one "Dashboard route".
- **Benefit:** everywhere else we write `Screen.Dashboard.route` instead of typing `"dashboard"`. A typo like `Screen.Dashbord` becomes a **compile error** instead of a crash at runtime.

---

## 2.3 `model/Models.kt`: data classes & sample data

```kotlin
data class Student(
    val name: String,
    val studentId: String,
    val major: String,
    val avatarRes: Int,
    val gpa: Double,
    val completedCredits: Int,
    val currentSemester: String
)
```
- A **data class** automatically gets `equals()`, `toString()` and **`copy()`**. We use `copy()` to make a changed version (for example, `student.copy(name = "New")`).
- `avatarRes: Int` holds a resource ID like `R.drawable.ic_student_avatar`. Resource IDs are Ints.

```kotlin
data class Course(
    val courseName: String,
    val courseCode: String,
    val instructor: String,
    val progress: Float // 0.0f to 1.0f
)
```
- `progress` is a Float between 0 and 1 because `LinearProgressIndicator` expects that range (0.72f = 72%).

```kotlin
data class Assignment(
    val id: Int,
    val title: String,
    val course: String,
    val dueDate: String,      // yyyy-MM-dd
    val priority: String,     // "Low", "Medium" or "High"
    val description: String = "",
    val isCompleted: Boolean = false,
    val reminderEnabled: Boolean = false
)
```
- `id` uniquely identifies each assignment. It is used as the **key** in `LazyColumn` and to find which one to toggle.
- **Default values** (`= ""`, `= false`) mean we can leave those arguments out when creating one.
- `dueDate` is stored as `"2026-10-12"` (year-month-day). Dates in this format **sort correctly as plain text**, which the Dashboard uses to find urgent deadlines.
- All properties are `val` (read-only). To "change" an assignment we create a new copy, which makes the change visible to Compose (see 2.4).

```kotlin
val sampleStudent = Student(
    name = "Pramiti Udupa",
    studentId = "PES1UG24CS331",
    major = "Computer Science & Engineering",
    avatarRes = R.drawable.ic_student_avatar,
    gpa = 3.82, completedCredits = 84, currentSemester = "Fall 2026"
)
val sampleCourses = listOf( /* 4 courses */ )
val sampleAssignments = listOf( /* 7 pending + 2 completed */ )
```
- The assignment says **"no Room database or network calls – use UI state variables or sample data models"**. These lists are that sample data.
- 4 courses and 7 pending assignments, so the Dashboard shows **"Active Courses (4)"** and **"Pending Tasks (7)"** exactly as the spec says.

---

## 2.4 `StudentOrganizerApp.kt`: central navigation + Scaffold (Task 6)

This is the **most important file for the viva**. It wires together navigation, the Scaffold and all the shared state.

### (a) The NavHostController
```kotlin
val navController = rememberNavController()
```
- **NavHostController** maintains the back stack, tracks the current destination, and handles navigation between destinations.
- It is stateful, so we create it with `rememberNavController()`, which keeps the same controller across recompositions (L27, Step 1).

### (b) The current route
```kotlin
val backStackEntry by navController.currentBackStackEntryAsState()
val currentRoute = backStackEntry?.destination?.route
```
- `currentBackStackEntryAsState()` gives the **top entry of the back stack as Compose State**. When we navigate, it changes and everything that reads it recomposes.
- `currentRoute` is then `"dashboard"`, `"assignments"`, and so on.
- `?.` is Kotlin's **safe call**: if `backStackEntry` is null (the very first frame), the result is null instead of a crash.
- **We need `currentRoute` for four things:**
  1. choosing the top-bar title,
  2. highlighting the selected bottom tab,
  3. **hiding the BottomBar** on add_assignment,
  4. showing the FAB only on Dashboard and Assignments.

### (c) App-wide state
```kotlin
var student by remember { mutableStateOf(sampleStudent) }
var assignments by remember { mutableStateOf(sampleAssignments) }
var notificationsEnabled by remember { mutableStateOf(true) }
var defaultStartScreen by remember { mutableStateOf("Dashboard") }
val courses = sampleCourses
```
- **Why here?** Dashboard, Assignments and Add Assignment all use the same `assignments` list. Ticking a card on the Assignments screen must change the "Pending Tasks" count on the Dashboard. So the list is **hoisted to their common parent**, which is this function.
- Settings values are here too, so they are **not lost when you switch tabs**. If they lived inside SettingsScreen, they would reset every time you left that screen, because a screen's `remember` is forgotten when it leaves the composition.
- `courses` never changes, so it's a plain `val`, not state.

### (d) Toggling an assignment
```kotlin
val toggleComplete: (Int) -> Unit = { id ->
    assignments = assignments.map { assignment ->
        if (assignment.id == id) assignment.copy(isCompleted = !assignment.isCompleted) else assignment
    }
}
```
- `toggleComplete` is a **lambda stored in a variable**. Its type `(Int) -> Unit` means it takes an id and returns nothing.
- `map` builds a **new list**: the matching assignment is replaced by a copy with `isCompleted` flipped (`!`), and all others stay the same.
- Assigning the new list to `assignments` (which is a `mutableStateOf`) **tells Compose the state changed**, so every screen reading `assignments` recomposes.
- Why not just set `assignment.isCompleted = true`? Because (1) it's a `val`, and (2) changing an object inside a list does not notify Compose. Replacing the whole value does.

### (e) Leaving the Add screen safely
```kotlin
val closeAddScreen: () -> Boolean = {
    navController.popBackStack(Screen.AddAssignment.route, inclusive = true)
}
```
- `popBackStack(route, inclusive = true)` removes destinations **down to and including** `add_assignment`. (L27 summary table: `popBackStack(route)` = back to a specific destination.)
- It **returns `true`** if it removed something, and `false` if `add_assignment` isn't on the stack any more.
- **Why not just `popBackStack()`?** If the user double-taps Save, plain `popBackStack()` would run twice: the first removes the Add screen, and the second would remove the **Dashboard**, leaving a blank screen. With the route version, the second call finds nothing, returns `false`, and does nothing.

### (f) Title for the top bar
```kotlin
val isAddScreen = currentRoute == Screen.AddAssignment.route

val title = when (currentRoute) {
    Screen.Dashboard.route -> "Welcome back, ${student.name.substringBefore(" ")}!"
    Screen.Assignments.route -> "My Assignments"
    Screen.Profile.route -> "Student Profile"
    Screen.Settings.route -> "Settings"
    Screen.AddAssignment.route -> "Add New Assignment"
    else -> "Student Organizer"
}
```
- `when` is Kotlin's switch statement. Each screen gets the title the spec asks for.
- `substringBefore(" ")` takes the first name: "Pramiti Udupa" → "Pramiti", so the greeting reads **"Welcome back, Pramiti!"**.
- It reads `student`, so after **Edit Profile** the greeting changes automatically (recomposition).

### (g) The Scaffold
```kotlin
Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
        AppBar(
            title = title,
            canNavigateBack = isAddScreen,
            onBackClick = { closeAddScreen() }
        )
    },
```
- **Scaffold** is the "skeleton" of a screen, with slots for `topBar`, `bottomBar`, `floatingActionButton` and the content (L26).
- `topBar`: our custom **AppBar**. The back arrow appears only when `canNavigateBack` is true, which is only on the Add screen.

```kotlin
    bottomBar = {
        if (!isAddScreen) {
            BottomBar(navController = navController, currentRoute = currentRoute)
        }
    },
```
- **Task 6: "Conditionally hide the BottomBar when on the add_assignment screen."** If we're on the Add screen, `BottomBar` simply isn't called, so nothing is drawn.

```kotlin
    floatingActionButton = {
        if (currentRoute == Screen.Dashboard.route || currentRoute == Screen.Assignments.route) {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddAssignment.route) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Assignment")
            }
        }
    }
```
- The **'+' FAB** appears only on Dashboard and Assignments, as the spec says.
- `navigate(route)` **pushes** `add_assignment` onto the back stack and makes it the current destination.
- Scaffold places the FAB at the **bottom-right** by default.

### (h) NavHost and the navigation graph
```kotlin
) { innerPadding ->
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = Modifier.padding(innerPadding)
    ) {
```
- `innerPadding`: Scaffold calculates how much space the top bar, bottom bar and system bars take and gives it to us. Applying it to NavHost stops the screens from being **hidden behind the bars**.
- **NavHost** is the container where the current destination is displayed. It needs three things:
  1. the **navController**,
  2. the **startDestination** (`"dashboard"`, the first screen on launch),
  3. the **navigation graph** (the lambda below).

```kotlin
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                courses = courses,
                assignments = assignments,
                onToggleComplete = toggleComplete
            )
        }
        composable(Screen.Assignments.route) {
            AssignmentsScreen(assignments = assignments, onToggleComplete = toggleComplete)
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
                        val nextId = assignments.size + 1
                        assignments = assignments + newAssignment.copy(id = nextId)
                    }
                },
                onCancel = { closeAddScreen() }
            )
        }
    }
}
```
- Each `composable(route) { ... }` adds **one destination** to the graph: "when the route is X, show this composable".
- Notice that **screens never receive the navController**. They only receive **data** (state down) and **callbacks** (events up). The screens don't know about navigation at all, which makes them reusable and previewable. This is **unidirectional data flow**.
- `onSave`: first close the Add screen. Only if that succeeded, append the new assignment.
  - `assignments + newAssignment` creates a **new list** with one more item (the `+` operator on lists).
  - `copy(id = nextId)` gives it a fresh id. Assignments are never deleted, so `size + 1` is always unused.
- `onProfileUpdate`: replaces `student` with a copy that has the new name and major, so both the greeting and the profile header update.

> **Viva summary:** "`rememberNavController()` creates the controller, `NavHost` with `startDestination = dashboard` holds the graph, each route is added with `composable()`, I move forward with `navigate()` and back with `popBackStack()`, and I read `currentBackStackEntryAsState()` to hide the bottom bar on the add screen."
