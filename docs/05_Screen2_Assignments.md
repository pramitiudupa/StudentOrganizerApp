# Screen 2 – Assignments List (`assignments`)

**File:** `screens/AssignmentsScreen.kt`
**Route:** `"assignments"`, reached from the BottomBar tab.
**Purpose (from the spec):** lists all assignments, with status filtering and quick completion toggles.

| On screen | Code |
|---|---|
| "My Assignments" top bar | `AppBar` in the Scaffold |
| ○ All ○ Pending ○ Completed | `Row` of `RadioButton`s |
| "Showing 9 of 9 assignments" | first `item { }` of the LazyColumn |
| List of cards with checkbox + priority badge | `LazyColumn` + `items(..., key)` + `AssignmentCard` |
| '+' button bottom-right | FAB in the Scaffold → `add_assignment` |

Spec checklist: 'My Assignments' AppBar ✔, Filter Row of RadioButtons ✔, `LazyColumn` of interactive `AssignmentCard`s ✔, '+' FAB anchored bottom-right ✔

---

## Code walkthrough

```kotlin
val filterOptions = listOf("All", "Pending", "Completed")
```
- The three filter names in one list, so the radio buttons can be created with a loop.

```kotlin
@Composable
fun AssignmentsScreen(
    assignments: List<Assignment>,
    onToggleComplete: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }
```
- `assignments` and `onToggleComplete` come from the parent, the same shared list the Dashboard uses.
- **`selectedFilter` is local state.** Only this screen cares which filter is chosen, so it lives here.
  - `mutableStateOf("All")` creates observable state (default "All").
  - `remember` keeps the value across **recompositions**.
  - `by` delegation means we read and write `selectedFilter` directly, without `.value`.
- Note: `remember` is forgotten when the screen **leaves the composition**. If you go to Add Assignment and come back, the filter resets to "All". `rememberSaveable` would keep it, but the assignment asks for `remember`.

```kotlin
    val filteredAssignments = when (selectedFilter) {
        "Pending" -> assignments.filter { !it.isCompleted }
        "Completed" -> assignments.filter { it.isCompleted }
        else -> assignments
    }
```
- Picks which assignments to show. `filter { }` returns only the items where the condition is true.
- This is a normal `val`, recalculated whenever `selectedFilter` **or** `assignments` changes (recomposition).

```kotlin
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
```
- The outer **Column** has the filter row on top and the list below.
- The **filter Row** spans the full width. `Arrangement.SpaceEvenly` spreads the 3 options with equal gaps (arrangement = main axis = horizontal for a Row).

```kotlin
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
```
- Exactly the RadioButton pattern from L23: **wrap each RadioButton and its label in a Row**.
  - `selected = selectedFilter == option`: only the chosen one is filled.
  - `onClick = { selectedFilter = option }`: **changes the state**, which causes recomposition, so the list re-filters.
- `Modifier.clickable { }` on the inner Row (an interaction modifier from L19) means tapping the **text** also selects the option, not just the small circle.
- `HorizontalDivider()` draws a thin line under the filters. It's the "Divider" from the L23 form elements.

```kotlin
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
```
- **Empty state:** if nothing matches (for example, "Completed" when nothing is done), it shows "No completed assignments", centred.
- **Box** with `contentAlignment = Alignment.Center` centres its child (L19: Box uses contentAlignment).

```kotlin
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
```
- **LazyColumn** is the main content the spec asks for. It scrolls by itself and only composes the visible cards, so it's efficient for long lists (L24).
- `item { }` adds a **header** line: "Showing 7 of 9 assignments".
- `items(filteredAssignments, key = { it.id })` adds one `AssignmentCard` per assignment.
  - **`key = { it.id }`** (L24 "Item Keys"): by default items are tracked by **position**. When the filter changes, positions change. A stable key (the id) keeps each card's identity correct.
- `bottom = 88.dp` stops the last card from being trapped under the FAB.
- `onToggleComplete = { onToggleComplete(assignment.id) }`: same pattern as the Dashboard, adding the id and passing the event up.

## Flow when you choose "Completed"
`RadioButton.onClick` → `selectedFilter = "Completed"` → state changed → **recomposition** → `filteredAssignments` recalculated → LazyColumn shows only the completed items and the header says "Showing 2 of 9".

## Flow when you tick a card here
Same as the Dashboard: event up to `toggleComplete(id)` → the `assignments` state is replaced → this list **and** the Dashboard's count update.

## Possible viva questions
- *Column vs LazyColumn?* Column composes all its children and doesn't scroll by default. LazyColumn composes only the visible items, scrolls, and uses `item{}` / `items()`. Use it for lists.
- *Why `key`?* For stable item identity when the data changes position (filtering or adding).
- *Where does the filter state live, and why?* In this screen, because no other screen needs it.
- *Why does the filter reset after adding an assignment?* `remember` state is lost when the composable leaves the composition (we navigated away). `rememberSaveable` would keep it.
- *What are `contentPadding` and `spacedBy`?* contentPadding = space around the whole list. spacedBy = space between items.
