# Part 9 – Viva Quick Revision

## 9.1 Syllabus topic → where it is in the app

| Unit 2 topic (lecture) | Where used |
|---|---|
| Activity, `onCreate`, lifecycle callbacks, Logcat | `MainActivity.kt` |
| AndroidManifest, MAIN/LAUNCHER intent-filter | `AndroidManifest.xml` |
| Intents (implicit) | Profile → Export (`Intent.ACTION_SEND`) |
| L16 Composables, `setContent`, `@Preview`, recomposition | every file; `@Preview` at the bottom of each |
| L17 Text, Button types, FAB, IconButton, Image + `painterResource`, `stringResource` | everywhere; avatar in `StudentProfile.kt` |
| L18 Column, Row, Box | all screens; Box in `PriorityBadge`, the course dropdown, the empty state |
| L19 Modifiers (padding, size, fillMaxWidth, weight, clip, background, border, alpha, clickable), modifier order, Arrangement & Alignment | `AssignmentCard.kt`, `PriorityBadge`, `StudentProfile.kt`, the stat Row |
| L21 State: `remember`, `mutableStateOf`, `by`, stateful vs stateless, **state hoisting**, recomposition | `AddAssignmentScreen` → `AssignmentForm`; `StudentOrganizerApp` |
| L22 Custom composables, parameters, `modifier: Modifier = Modifier` rules | everything in `components/` |
| L23 Forms: OutlinedTextField, KeyboardOptions, Checkbox, RadioButton, Switch, DropdownMenu, Buttons, Divider, error text | `AssignmentForm.kt`, `SettingsScreen.kt` |
| L24 LazyColumn, LazyRow, `item{}`, `items()`, `key`, `contentPadding`, `spacedBy` | `DashboardScreen.kt`, `AssignmentsScreen.kt` |
| L25 Card, ElevatedCard, CardDefaults colors/elevation, RoundedCornerShape | `CourseInfo`, `AssignmentCard`, `StatCard`, `StudentProfile` |
| L26 Scaffold (topBar, bottomBar, floatingActionButton, innerPadding), TopAppBar | `StudentOrganizerApp.kt`, `AppBar.kt` |
| L27 Navigation: NavHostController, `rememberNavController`, NavHost, `composable()`, `navigate`, `popBackStack`, `popUpTo` | `StudentOrganizerApp.kt`, `BottomBar.kt` |
| L28 Side effects: `SideEffect` | `Theme.kt` (status-bar icon colour) |

## 9.2 Three small things not shown in the slides (know these one-liners)
| Thing | One-line answer |
|---|---|
| `currentBackStackEntryAsState()` | Gives the current back-stack entry as State, so I can read the current route. The spec needs it to highlight the tab and hide the BottomBar. |
| `NavigationBar` / `NavigationBarItem` | Material 3's version of BottomNavigation (which is in the L16 list). It's a bottom bar for tabs with a `selected` highlight. |
| `imePadding()` | Pads the form by the keyboard height so the keyboard doesn't hide the field (apps are edge-to-edge on Android 15+). |

---

## 9.3 Likely questions & model answers

**1. Explain the architecture of your app in one minute.**
One Activity (`MainActivity`) calls `setContent` and wraps the app in the theme. `StudentOrganizerApp` creates the NavController, holds the shared state (assignments, student, settings) and builds a Scaffold with my AppBar, BottomBar and FAB. Inside the Scaffold, a NavHost shows one of 5 screens, chosen by routes from the `Screen` sealed class. Screens receive data and callbacks: state goes down, events come up.

**2. Why a sealed class for routes?**
It's a fixed set of subclasses known at compile time. I write `Screen.Dashboard.route` instead of a raw string, so typos become compile errors.

**3. What is NavHostController? Why `rememberNavController()`?**
It manages the back stack and the current destination, and performs navigation. It's stateful, so `remember...` keeps the same instance across recompositions.

**4. What does NavHost need?**
navController, startDestination (`"dashboard"`), and the graph, which is one `composable(route) { Screen() }` per destination.

**5. Explain the back stack when I tap + and then Save.**
`[dashboard]` → `navigate("add_assignment")` → `[dashboard, add_assignment]` → Save → `popBackStack("add_assignment", inclusive = true)` → `[dashboard]`.

**6. How is the BottomBar hidden on Add Assignment?**
`currentRoute` comes from `currentBackStackEntryAsState()`. In the Scaffold's bottomBar slot I only call `BottomBar` if `currentRoute != "add_assignment"`.

**7. What does `popUpTo` do in your BottomBar?**
Before navigating to a tab, it pops everything above Dashboard. The stack stays "Dashboard + one tab", and Back always returns to Dashboard.

**8. Why `popBackStack(route, inclusive = true)` instead of `popBackStack()`?**
It only pops if add_assignment is actually on the stack, and returns false otherwise. A double-tap on Save can't pop the Dashboard or add the item twice.

**9. What is state? What do `mutableStateOf` and `remember` do?**
State is any value that can change and affect the UI (UI = f(State)). `mutableStateOf` makes it observable, so a change schedules recomposition. `remember` keeps the value across recompositions. `by` hides `.value`.

**10. What is recomposition?**
Re-running composable functions when their inputs change. Compose skips the functions whose inputs didn't change.

**11. Stateful vs stateless? Give examples from your code.**
Stateful owns its state: `AddAssignmentScreen` (6 form states). Stateless gets its state and callbacks from the parent: `AssignmentForm`, `AssignmentCard`, `SettingsScreen`.

**12. What is state hoisting and why did you use it?**
Moving state up to the parent so the child becomes stateless. Benefits: one source of truth (the Dashboard count and the Assignments list always agree), reusable and previewable components, and state that survives tab switches (settings).

**13. Why is `assignments` stored in `StudentOrganizerApp`?**
Three screens need it. It must live in their lowest common parent.

**14. How does ticking a checkbox update the Dashboard count?**
Checkbox → `onToggleComplete(id)` → `assignments = assignments.map { copy(isCompleted = !...) }` → new list in state → recomposition → `pendingCount` is recomputed.

**15. Why create a new list with `map` and `copy` instead of changing the object?**
The data class uses `val`s, and Compose only notices when the State's value is **replaced**. Changing an object inside the list wouldn't trigger recomposition.

**16. How is the Save button validated?**
`val isFormValid = title.isNotBlank() && course.isNotBlank() && dueDate.isNotBlank()` and `Button(enabled = isFormValid)`. It's recalculated on every keystroke.

**17. What is a Slot API? Where did you use one?**
A parameter of type `@Composable () -> Unit`. `AssignmentForm(actions = { Cancel/Save buttons })`. Scaffold's `topBar` and `bottomBar` are slots too.

**18. Why does every custom composable have `modifier: Modifier = Modifier`?**
The caller can control size and padding. Rules: name it `modifier`, make it the first optional parameter, default to an empty `Modifier`, and apply it to the first child.

**19. Column vs LazyColumn? Row vs LazyRow?**
Column/Row compose all children and don't scroll by default, so they suit small layouts. Lazy versions compose only the visible items, scroll by themselves, and use `item{}` / `items()`. Urgent Deadlines (2 items) is a Column. The full assignment list is a LazyColumn. Courses are a LazyRow.

**20. Why `key = { it.id }` in `items()`?**
A stable identity per item. Without it, items are tracked by position, which breaks when filtering or adding changes positions.

**21. contentPadding vs spacedBy?**
contentPadding = space around the whole list. `Arrangement.spacedBy` = space between items.

**22. Arrangement vs Alignment?**
Arrangement = distribution along the main axis. Alignment = position on the cross axis. Row: horizontalArrangement + verticalAlignment. Column: verticalArrangement + horizontalAlignment. Box: contentAlignment.

**23. What does `weight(1f)` do?**
It shares the leftover space in a Row or Column. The two stat cards split 50/50. In settings rows it pushes the Switch to the right.

**24. Does modifier order matter? Example?**
Yes. In `PriorityBadge`: `clip` → `background` → `padding`, so the colour fills the rounded shape and the padding is inside the colour.

**25. What is `innerPadding`?**
The space Scaffold reserves for the top bar, bottom bar and system bars. I apply it to NavHost so content isn't hidden behind them.

**26. Card vs ElevatedCard vs OutlinedCard?**
Card = standard (I set elevation via CardDefaults). ElevatedCard = stronger shadow (StatCard, StudentProfile). OutlinedCard = border instead of shadow.

**27. How does dark mode work?**
`isDarkMode` state lives in MainActivity above the theme. The Switch callback changes it, the theme recomposes with DarkColorScheme, and every `MaterialTheme.colorScheme` colour updates. `SideEffect` fixes the status-bar icon colour.

**28. What is a side effect? Where did you use one?**
An operation that affects the outside world. `SideEffect { }` in Theme.kt sets the window's status-bar icon colour after each successful recomposition.

**29. What does Export Academic Summary do?**
It creates an implicit Intent (`ACTION_SEND`, `text/plain`, `EXTRA_TEXT`) and calls `startActivity(Intent.createChooser(...))`, which shows the share sheet. Implicit = I say *what* to do, Android finds the app.

**30. How is the avatar shown?**
`Image(painter = painterResource(R.drawable.ic_student_avatar), contentScale = Crop)` with `clip(CircleShape)` and `border`.

**31. What is `@Preview`? Does it run the app?**
It renders a composable inside Android Studio's Design view. It does not run the app. I pass fake data and empty lambdas.

**32. What is `LocalContext.current`?**
It gets the Android Context inside a composable, which is needed for `Toast` and `startActivity`.

**33. What happens if you rotate the phone or close the app?**
The data resets. `remember` doesn't survive configuration changes (rememberSaveable would), and there's no database, as per the assignment scope ("no Room or network").

**34. Why `@OptIn(ExperimentalMaterial3Api::class)` on AppBar?**
`TopAppBar` is marked experimental in Material 3, so we must opt in explicitly.

**35. Why only one Activity for 5 screens?**
With Compose Navigation each screen is a composable destination inside NavHost. That's lighter than separate Activities and makes it easy to share state.

**36. What does `enableEdgeToEdge()` do?**
It lets the app draw behind the status and navigation bars. Scaffold's innerPadding then keeps the content clear of them.

**37. Why is `dueDate` stored as `yyyy-MM-dd`?**
That format sorts correctly as plain text, so `sortedBy { it.dueDate }` gives the earliest deadline first.
