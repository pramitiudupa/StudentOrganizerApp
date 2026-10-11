# Part 3 – Custom Composables (Task 2)

All in `app/src/main/java/com/pes/studentorganizer/components/`.

**What is a custom composable?** A reusable Kotlin function marked `@Composable` that represents one part of the UI. We write it once and use it many times (L22).

**Rules every component here follows (L22 slides):**
1. It takes **parameters** for its data, so it can show anything.
2. It takes **event callbacks** like `onBackClick: () -> Unit`, so the **parent decides what happens** (state/event hoisting).
3. It has `modifier: Modifier = Modifier` as the **first optional parameter**, with an **empty Modifier as the default**, and the modifier is **applied to the first composable inside**.
4. It has a **`@Preview`** function at the bottom, so it can be seen in Android Studio's Design view without running the app.

| Component | Parameters (the "contract" from the assignment) | Used in |
|---|---|---|
| `AppBar` | `title`, `canNavigateBack`, `onBackClick` | Scaffold `topBar` (all screens) |
| `BottomBar` | `navController`, `currentRoute` | Scaffold `bottomBar` |
| `CourseInfo` | `courseName`, `courseCode`, `instructor`, `progress` | Dashboard LazyRow |
| `AssignmentCard` | `title`, `course`, `dueDate`, `priority`, `isCompleted`, `onToggleComplete` | Dashboard, Assignments |
| `StudentProfile` | `studentName`, `studentId`, `major`, `avatarRes` | Profile |
| `AssignmentForm` | all form values + `onXChange` callbacks + `actions` slot | Add Assignment (explained in Part 8) |

**About imports:** the many `import` lines at the top of each file just tell Kotlin where each class comes from. For example, `import androidx.compose.material3.Card` means "Card from the Material 3 library". Android Studio adds them automatically (Alt+Enter).

---

## 3.1 `AppBar.kt`

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    title: String,
    canNavigateBack: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
```
- `@OptIn(ExperimentalMaterial3Api::class)`: `TopAppBar` is marked *experimental* in Material 3, so we must **opt in** to use it.
- The parameters exactly match the assignment's contract.

```kotlin
    TopAppBar(
        title = {
            Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        modifier = modifier,
```
- `TopAppBar(title = ...)`: the title is a **slot** (a composable lambda), so we put a `Text` in it.
- `maxLines = 1` + `TextOverflow.Ellipsis`: if the title is too long, it shows "..." instead of wrapping.
- `modifier = modifier`: the caller's modifier is applied to the first composable (rule 3).

```kotlin
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
```
- `navigationIcon` is the slot on the **left** of the app bar (L26: TopAppBar parameters).
- Only if `canNavigateBack` is true do we draw an **IconButton with a back arrow**. Otherwise the slot is empty.
- `onClick = onBackClick`: AppBar doesn't know *how* to go back. It just calls the callback, and the parent passes `{ closeAddScreen() }`.
- `AutoMirrored` means the arrow flips automatically for right-to-left languages.
- `contentDescription = "Back"` is read aloud by screen readers (accessibility).

```kotlin
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
```
- `colors` (L26: "colors: determines how the app bar appears"). We take colours **from the theme**, so the bar is light blue in light mode and dark navy in dark mode automatically.

---

## 3.2 `BottomBar.kt`

```kotlin
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
```
- A small data class describing one tab: its text, its icon and which `Screen` it opens.
- The 4 tabs the spec asks for. `add_assignment` is **not** a tab; it is reached through the FAB.

```kotlin
@Composable
fun BottomBar(
    navController: NavHostController,
    currentRoute: String?,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        for (item in bottomNavItems) {
            NavigationBarItem(
                selected = currentRoute == item.screen.route,
```
- `currentRoute: String?`: the `?` means it can be null (the first frame before navigation starts).
- `NavigationBar` is **Material 3's name for BottomNavigation** (which is listed in the L16 Material components). It is a bottom bar made for tabs.
- `for (item in bottomNavItems)` creates one `NavigationBarItem` per tab.
- `selected = currentRoute == item.screen.route`: the tab is **highlighted** when its route is the current route. That's why BottomBar needs `currentRoute`.

```kotlin
                onClick = {
                    if (currentRoute != item.screen.route) {
                        if (item.screen == Screen.Dashboard) {
                            navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                        } else {
                            navController.navigate(item.screen.route) {
                                popUpTo(Screen.Dashboard.route)
                            }
                        }
                    }
                },
```
- `if (currentRoute != item.screen.route)`: tapping the tab you're already on does nothing.
- **Other tabs:** `navigate(route) { popUpTo("dashboard") }`. Before pushing the new tab, it removes everything above Dashboard (L27: "Controlling the Back Stack"). So the back stack is always **Dashboard → one tab**, and pressing Back from any tab returns to Dashboard instead of going through every tab you visited.
- **Dashboard tab:** Dashboard is the start destination and is always at the bottom of the stack, so we just **pop back to it**. `inclusive = false` keeps Dashboard itself. Using `navigate` here would put a *second* Dashboard on the stack.

```kotlin
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
```
- `icon` and `label` are slots: the icon and the text under it.

**Back stack example:**
```
Launch:                [dashboard]
Tap Assignments:       [dashboard, assignments]
Tap Profile:           [dashboard, profile]          ← popUpTo removed "assignments"
Press Back:            [dashboard]
Tap + on Dashboard:    [dashboard, add_assignment]
Tap Save:              [dashboard]                   ← popBackStack
```

---

## 3.3 `CourseInfo.kt`

```kotlin
@Composable
fun CourseInfo(
    courseName: String,
    courseCode: String,
    instructor: String,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
```
- A **Card** groups related content (L25). The three Card parameters used are all from the slides:
  - `shape = RoundedCornerShape(16.dp)`: rounded corners.
  - `colors = CardDefaults.cardColors(containerColor = ...)`: background colour from the theme.
  - `elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)`: a shadow so it looks raised.
- `dp` = density-independent pixels, the unit for sizes, so the card looks the same size on every phone.

```kotlin
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = courseCode,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
```
- **Column** stacks children vertically, and `padding(16.dp)` adds space inside the card.
- **Row** puts the cap icon and the course code side by side. `verticalAlignment = Alignment.CenterVertically` centres them on the cross axis (for a Row, the cross axis is vertical).
- `contentDescription = null`: the icon is only decoration, so screen readers skip it.
- `Spacer(Modifier.width(8.dp))`: an empty gap between the icon and the text.

```kotlin
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = courseName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = instructor, style = MaterialTheme.typography.bodySmall)
```
- The course name is limited to one line with "..." so all cards in the row keep the same height.

```kotlin
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(progress * 100).toInt()}% complete",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
```
- **LinearProgressIndicator** (listed in L16 Material components) draws the completion bar. `progress` must be between 0 and 1.
- `progress = { progress }`: in Material 3 the progress is passed as a **lambda** that returns the value. (The slide shows the older `LinearProgressIndicator(progress)` form, and the idea is the same.)
- `fillMaxWidth()` makes the bar take the full card width.
- `"${(progress * 100).toInt()}% complete"`: **string template**. 0.72 × 100 = 72 → "72% complete".

---

## 3.4 `AssignmentCard.kt`

```kotlin
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
```
- **A stateless composable.** It does *not* own `isCompleted`. The parent passes the current value **down**, and when the checkbox is tapped it sends the event **up** through `onToggleComplete`. This is the stateless Counter example from L21, applied to a card.

```kotlin
    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isCompleted) 0.6f else 1f),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
```
- **Modifier chain:** the caller's `modifier`, then `fillMaxWidth()` (full width), then `alpha(...)`.
- `alpha` (an appearance modifier from L19) sets transparency: **completed cards are faded to 60%** so they look "done".
- `if (isCompleted) 0.6f else 1f`: in Kotlin, `if` is an expression that returns a value.

```kotlin
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isCompleted,
                onCheckedChange = { onToggleComplete() }
            )
```
- The Row lays out **[checkbox] [text column] [badge]** horizontally.
- `Checkbox(checked, onCheckedChange)`: the two key parameters from L23. `checked` shows the current state, and `onCheckedChange` runs when tapped. We ignore the Boolean it gives us and just call `onToggleComplete()`, because the parent flips the value.

```kotlin
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
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCompleted) "Completed" else "Due: $dueDate",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
```
- `Modifier.weight(1f)` (from L19: "weight – shares available space in Row/Column"): the text column takes **all the space left over** after the checkbox and the badge, which pushes the badge to the right edge.
- Inside: the title, the course code (in the primary colour), and a small Row with a calendar icon and either "Due: 2026-10-12" or "Completed".

```kotlin
            Spacer(modifier = Modifier.width(8.dp))
            PriorityBadge(priority = priority)
        }
    }
}
```

### `PriorityBadge`: a second, smaller custom composable
```kotlin
@Composable
fun PriorityBadge(priority: String, modifier: Modifier = Modifier) {
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
```
- `when` picks red, orange or green from `Color.kt`.
- **Box** with `contentAlignment = Alignment.Center` centres the text inside.
- **Modifier order matters (L19):**
  1. `clip(RoundedCornerShape(50))` cuts the shape to a pill (50% rounded corners).
  2. `background(badgeColor)` paints the colour **inside the clipped shape**.
  3. `padding(...)` adds space **inside** the coloured area, around the text.
  If `padding` came before `background`, the padding area would not be coloured.

---

## 3.5 `StudentProfile.kt`

```kotlin
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
```
- **ElevatedCard** is one of the three Material 3 card types (L25: Card, ElevatedCard, OutlinedCard). It has a stronger shadow, which suits a header.
- `horizontalAlignment = Alignment.CenterHorizontally`: centres every child of the Column (for a Column, the cross axis is horizontal).

```kotlin
            Image(
                painter = painterResource(id = avatarRes),
                contentDescription = stringResource(id = R.string.student_avatar_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .border(BorderStroke(3.dp, MaterialTheme.colorScheme.primary), CircleShape)
            )
```
- **Image + painterResource**, exactly as in L17 ("Loading images"). `avatarRes` is `R.drawable.ic_student_avatar`.
- `contentDescription` comes from **strings.xml** via `stringResource()` (L17: "use string resources instead of hardcoding").
- `contentScale = ContentScale.Crop` (L17 "Customize Images"): fills the area and crops the edges.
- Modifiers: `size(110.dp)` makes it 110×110, `clip(CircleShape)` makes it round, and `border(...)` adds a 3 dp coloured ring around the circle.

```kotlin
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = studentName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = studentId, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = major, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }
    }
}
```
- Name (big and bold), USN (in the primary colour) and major, one under another.
- `textAlign = TextAlign.Center` keeps a long major centred if it wraps onto two lines.

---

## 3.6 `@Preview` functions (at the bottom of every file)

```kotlin
@Preview(showBackground = true)
@Composable
private fun AppBarPreview() {
    StudentOrganizerTheme {
        AppBar(title = "Add New Assignment", canNavigateBack = true, onBackClick = {})
    }
}
```
- `@Preview` shows the composable **inside Android Studio** (the Split/Design tab) **without running the app**. It does not run the application (L16).
- `showBackground = true` draws a white background. `showSystemUi = true` (used on the screens) shows a phone frame. `heightDp = 800` makes the preview taller.
- We pass **fake data and empty callbacks** (`onBackClick = {}`). Because the components are stateless, this is easy, which is one of the benefits of state hoisting.
- `private` means only this file uses the preview function.
