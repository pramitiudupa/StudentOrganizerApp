# Screen 1 – Dashboard (`dashboard`)

**File:** `screens/DashboardScreen.kt`
**Route:** `Screen.Dashboard.route` = `"dashboard"`. This is the **start destination**, the first screen on launch.
**Purpose (from the spec):** the main hub showing the student overview, course list and quick assignment highlights.

## What the examiner sees → which code draws it

| On screen | Where it comes from |
|---|---|
| "Welcome back, Pramiti!" top bar | `AppBar` in the Scaffold (`StudentOrganizerApp.kt`); title chosen by `when(currentRoute)` |
| Two stat cards: **4** Active Courses, **7** Pending Tasks | `StatCard` × 2 in a `Row` |
| "Active Courses" + sideways-scrolling cards | `LazyRow` of `CourseInfo` |
| "Urgent Deadlines" + 2 cards | `Column` of 2 `AssignmentCard`s |
| '+' button | `FloatingActionButton` in the Scaffold → navigates to `add_assignment` |
| Bottom tabs | `BottomBar` in the Scaffold |

Spec checklist: Greeting header ✔, Quick Stats Row with two elevated cards ✔, `LazyRow` of `CourseInfo` ✔, `Column` with top 2 upcoming `AssignmentCard`s ✔, '+' FAB → `add_assignment` ✔

---

## Code walkthrough

### Function signature
```kotlin
@Composable
fun DashboardScreen(
    courses: List<Course>,
    assignments: List<Assignment>,
    onToggleComplete: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
```
- The screen **receives** the data (courses, assignments) and a **callback** (`onToggleComplete`). It does not own the list.
- `onToggleComplete: (Int) -> Unit` takes the **id** of the assignment to toggle.

### Calculated values
```kotlin
    val pendingCount = assignments.count { !it.isCompleted }

    val urgentAssignments = assignments
        .filter { !it.isCompleted }
        .sortedBy { it.dueDate }
        .take(2)
```
- `count { !it.isCompleted }` counts the assignments that are **not** completed. Initially that's 7. `it` means "the current item".
- **Urgent deadlines** is a chain of three steps:
  1. `filter { !it.isCompleted }` keeps only the pending assignments.
  2. `sortedBy { it.dueDate }` sorts by due date, earliest first. This works because dates are stored as `yyyy-MM-dd`, so alphabetical order = date order.
  3. `take(2)` keeps the first 2. The spec says "top 2 upcoming".
- These are plain `val`s, not state. They are **recalculated on every recomposition**. When you tick a card, `assignments` changes, the screen recomposes, and `pendingCount` and `urgentAssignments` update automatically. **UI = f(State).**

### The scrollable container
```kotlin
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
```
- The whole dashboard is a **LazyColumn**, so it **scrolls** if the content is taller than the screen (a plain Column does not scroll by default, L24).
- `contentPadding` adds space around the whole list (L24: "Content Padding and Spacing"). The **bottom is 88 dp** so you can scroll the last card above the FAB, which would otherwise cover it.
- `Arrangement.spacedBy(16.dp)` puts 16 dp **between** items (the main axis is vertical for a column).

### Section 1: Quick Stats Row
```kotlin
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
```
- `item { }` adds **one** entry to a LazyColumn (L24: "Adding Items to Lazy List").
- A **Row** holds two `StatCard`s side by side.
- **Both have `Modifier.weight(1f)`**, so they **share the width equally** (50/50).
- `courses.size` = 4, and `pendingCount` = 7 initially.

### Section 2: Active Courses (LazyRow)
```kotlin
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
```
- **LazyRow** is a horizontal list that **scrolls sideways** and only composes the cards that are visible (L24: Row vs LazyRow).
- `items(courses, key = { it.courseCode })` creates one entry per course. The **key** (the unique course code) keeps each item's identity stable if the list changes (L24: "Item Keys").
- `course ->` names the current item `course` (instead of `it`).
- Each `CourseInfo` card gets a **fixed width of 250 dp**, so the next card peeks in from the right. That hints to the user that they can scroll.
- A LazyRow **inside** a LazyColumn is allowed because they scroll in different directions.

### Section 3: Urgent Deadlines (Column)
```kotlin
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
```
- **The spec says a plain `Column`** here, not a LazyColumn. That makes sense: it's only ever 2 items, and the slides say Column is best for "small layouts".
- If everything is completed, a friendly message shows instead.
- `for (assignment in urgentAssignments)`: a normal Kotlin loop calling `AssignmentCard` for each item.
- `onToggleComplete = { onToggleComplete(assignment.id) }`: `AssignmentCard`'s callback takes no arguments, so we wrap it in a lambda that adds **this card's id** before passing the event up to `StudentOrganizerApp`.

### Helper composable: StatCard
```kotlin
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
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "$value", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
```
- The spec says "two **elevated** cards", so this uses **ElevatedCard** with 6 dp elevation (L25: "Card Elevation").
- Written once and used twice: the point of a custom composable (**reusability**).
- `tint` colours the icon with the theme's primary colour.
- `"$value"` turns the Int into a String for `Text`.

### Helper composable: SectionTitle
```kotlin
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text = text, modifier = modifier, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
}
```
- Used for "Active Courses" and "Urgent Deadlines", and it guarantees both headings look the same (**consistency**).

---

## What happens when you tick an urgent assignment (explain this flow!)
1. You tap the Checkbox in `AssignmentCard` → `onCheckedChange` → `onToggleComplete()`.
2. Dashboard's lambda → `onToggleComplete(assignment.id)` → `toggleComplete(id)` in `StudentOrganizerApp`.
3. `assignments = assignments.map { ... copy(isCompleted = true) ... }`: the **state changes**.
4. Compose **recomposes** everything that reads `assignments`: the Dashboard's `pendingCount` becomes 6, `urgentAssignments` now picks the next two, and the card disappears from Urgent Deadlines.
5. On the Assignments tab, the same item shows as **completed**, because both screens read the same hoisted list.

**State goes down, events go up: unidirectional data flow.**

## Possible viva questions on this screen
- *Why LazyRow for courses but Column for urgent deadlines?* LazyRow is for a scrollable horizontal list. The spec asks for a vertical `Column` for just the top 2 items, and Column is fine for a small fixed number.
- *How are the counts updated?* They're computed from the hoisted `assignments` state on every recomposition.
- *Why `weight(1f)` on both stat cards?* So they split the Row's width equally.
- *Why `bottom = 88.dp` padding?* So the last card can scroll above the FAB.
- *Where is the FAB/greeting code?* In the central Scaffold in `StudentOrganizerApp.kt`, chosen by `currentRoute`.
