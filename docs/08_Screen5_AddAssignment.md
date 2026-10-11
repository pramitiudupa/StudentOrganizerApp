# Screen 5 – Add Assignment (`add_assignment`)

**Files:** `screens/AddAssignmentScreen.kt` (the **stateful parent**) + `components/AssignmentForm.kt` (the **stateless form**)
**Route:** `"add_assignment"`, reached from the '+' FAB on Dashboard or Assignments. The back arrow, Cancel or Save return to the caller.
**Purpose (from the spec):** a form screen to enter and save a new assignment.

| On screen | Code |
|---|---|
| "Add New Assignment" + ← back arrow | `AppBar(canNavigateBack = true)` in the Scaffold |
| No bottom bar, no FAB | Scaffold hides them when `currentRoute == "add_assignment"` |
| 1. Assignment Title * | `OutlinedTextField`, `singleLine = true` |
| 2. Course Name/Code * with ▼ | `OutlinedTextField` + `DropdownMenu` |
| 3. Due Date * with 📅 icon | `OutlinedTextField` with `trailingIcon` |
| 4. Priority: ○ Low ● Medium ○ High | Label + `Row` of `RadioButton`s |
| 5. Description / Notes | `OutlinedTextField`, `minLines = 3`, `maxLines = 5` |
| 6. ☐ Enable Reminder Notification | `Row` with `Checkbox` |
| [Cancel] [Save Assignment] | `OutlinedButton` + `Button`, passed in through the `actions` **slot** |
| Red "* Title, Course and Due Date are required" | shown only while the form is invalid |

Spec checklist: TopAppBar with back IconButton ✔, Form container `LazyColumn` ✔, all 6 fields exactly as listed ✔, Cancel `OutlinedButton` + Save `Button` in a Row ✔, **Save disabled until Title, Course and Due Date are filled** ✔, BottomBar hidden ✔

---

## The big idea: **state hoisting** (Task 5)
```
AddAssignmentScreen  (STATEFUL: owns title, course, dueDate, priority, description, reminderEnabled)
     │  values go DOWN ↓        ↑ events come UP (onTitleChange = { title = it } ...)
     ▼
AssignmentForm       (STATELESS: just draws the fields from its parameters)
```
This is exactly the L21 slides' pattern: *"State hoisting means moving state to the caller/parent so that a Composable becomes stateless."* The 5 steps from the slide:
1. **Identify the state:** the 6 form values.
2. **Lift it to a parent:** `AddAssignmentScreen`.
3. **Pass it as parameters:** `title = title, ...`
4. **Update it from the child via callbacks:** `onTitleChange = { title = it }`
5. **Observe and react:** `isFormValid` recalculates, and the Save button enables.

---

## Part A: `AddAssignmentScreen.kt` (the parent)

```kotlin
@Composable
fun AddAssignmentScreen(
    courseOptions: List<Course>,
    onSave: (Assignment) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
```
- `courseOptions`: the 4 courses for the dropdown.
- `onSave: (Assignment) -> Unit`: hands the new assignment **up** to `StudentOrganizerApp`, which adds it to the list and navigates back.
- `onCancel`: just goes back.
- `context` is needed for the **Toast**.

```kotlin
    var title by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var description by remember { mutableStateOf("") }
    var reminderEnabled by remember { mutableStateOf(false) }
```
- **Task 5: "Manage form state using `remember { mutableStateOf() }`."** There is one state variable per field.
- `mutableStateOf` = **observable state**: when it changes, Compose recomposes whatever reads it.
- `remember` = keep the value across recompositions. Without it, every recomposition would reset the field to "".
- `by` = property delegation. We write `title` instead of `title.value`.
- The text fields start empty, priority defaults to "Medium", and the reminder starts off.

```kotlin
    val isFormValid = title.isNotBlank() && course.isNotBlank() && dueDate.isNotBlank()
```
- **Validation rule:** all three required fields must contain some non-space text.
- It is recalculated on **every recomposition**, which happens on every keystroke, so the Save button enables or disables **live** as you type.

```kotlin
    AssignmentForm(
        title = title,
        course = course,
        dueDate = dueDate,
        priority = priority,
        description = description,
        reminderEnabled = reminderEnabled,
        onTitleChange = { title = it },
        onCourseChange = { course = it },
        onDueDateChange = { dueDate = it },
        onPriorityChange = { priority = it },
        onDescriptionChange = { description = it },
        onReminderChange = { reminderEnabled = it },
        courseOptions = courseOptions,
        modifier = modifier,
```
- **State down:** each current value is passed to the form.
- **Events up:** each `onXChange` lambda receives the new value as `it` and **writes it into the parent's state**.
- Typing one letter in the title goes `OutlinedTextField.onValueChange("K")` → `onTitleChange("K")` → `title = "K"` → recomposition → the form draws "K".

```kotlin
        actions = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
```
- `actions = { ... }` fills the form's **slot** (explained in Part B).
- A Row with **Cancel** (an `OutlinedButton`, low emphasis) and Save. Both have `weight(1f)`, so they're equal width.
- `onClick = onCancel` goes back without saving.

```kotlin
                    Button(
                        enabled = isFormValid,
                        onClick = {
                            val newAssignment = Assignment(
                                id = 0,
                                title = title.trim(),
                                course = course.trim(),
                                dueDate = dueDate.trim(),
                                priority = priority,
                                description = description.trim(),
                                reminderEnabled = reminderEnabled
                            )
                            Toast.makeText(context, "Assignment \"${newAssignment.title}\" saved", Toast.LENGTH_SHORT).show()
                            onSave(newAssignment)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Assignment")
                    }
                }
```
- **`enabled = isFormValid`**: the key requirement. *"The Save button must be disabled until required fields (Title, Course, Due Date) are populated."* The Button's `enabled` parameter makes it look greyed out and ignore taps when false (L17: Button parameters `onClick`, `enabled`, `colors`, `contentPadding`). It's the same as the `LoginButton(enabled)` example in the slides.
- On click:
  1. Build an `Assignment` from the current state. `trim()` removes extra spaces. `id = 0` is a placeholder; the real id is assigned in `StudentOrganizerApp`.
  2. Show a **Toast**, a short pop-up message at the bottom.
  3. `onSave(newAssignment)` sends it **up**. The parent closes this screen with `popBackStack(...)` and appends the item (Part 2.4(h)).
- `\"` inside a string is an escaped quote character.

```kotlin
                if (!isFormValid) {
                    Text(
                        text = "* Title, Course and Due Date are required",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )
}
```
- **Error/helper text** (L23 "Supporting elements: Error/helper text"). It tells the user *why* Save is disabled, and disappears once the form is valid.
- `colorScheme.error` is the theme's red.

---

## Part B: `AssignmentForm.kt` (the stateless form)

```kotlin
val priorityOptions = listOf("Low", "Medium", "High")

@Composable
fun AssignmentForm(
    title: String,
    course: String,
    dueDate: String,
    priority: String,
    description: String,
    reminderEnabled: Boolean,
    onTitleChange: (String) -> Unit,
    onCourseChange: (String) -> Unit,
    onDueDateChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onReminderChange: (Boolean) -> Unit,
    courseOptions: List<Course>,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {}
) {
```
- Matches the assignment's contract: `title, course, dueDate, priority, description, onTitleChange: (String) -> Unit, ...`
- **Every value is a parameter and every change is a callback**, so it owns no form data. That makes it **reusable**: the same form could be used for an "Edit Assignment" screen, fed with existing values.
- **`actions: @Composable () -> Unit = {}` is a Slot API.** The parameter's type is *a composable function*. The caller decides which buttons to show. Scaffold's `topBar = { }` and a Button's content `{ Text(...) }` are slots too. The default `{}` means "no buttons" if none are passed.

```kotlin
    var courseMenuExpanded by remember { mutableStateOf(false) }
```
- The **only** state inside the form: whether the course dropdown is open. It's purely visual, and no one outside needs it, so it doesn't need hoisting.

```kotlin
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
```
- **"Form Container: LazyColumn"**, as the spec requires. It lets the form **scroll** when the keyboard covers half the screen.
- **`imePadding()`**: IME = the on-screen keyboard. On Android 15+ apps draw edge-to-edge, so the keyboard is drawn **on top of** the app. `imePadding()` adds bottom padding equal to the keyboard's height, so the list shrinks and the field you're typing in stays visible. (Tested on the S22 with the keyboard open.)
- Each field below is one `item { }`.

### Field 1: Assignment Title
```kotlin
        item {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Assignment Title *") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
```
- **OutlinedTextField** (L23): `value` = the current text, and `onValueChange` = called on every change. We pass the parent's callback directly.
- `label`: the floating hint ("Assignment Title *"; * = required). It moves up into the border when you type.
- **`singleLine = true`**, as the spec requires: no Enter or new lines.
- **KeyboardOptions** (L23 "Set keyboard options"):
  - `capitalization = Sentences` capitalises the first letter.
  - `imeAction = ImeAction.Next` makes the keyboard's Enter key show **"Next"** and jump to the next field.

### Field 2: Course (text field + dropdown)
```kotlin
        item {
            Box {
                OutlinedTextField(
                    value = course,
                    onValueChange = onCourseChange,
                    label = { Text("Course Name/Code *") },
                    placeholder = { Text("e.g. CS101") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Next
                    ),
                    trailingIcon = {
                        IconButton(onClick = { courseMenuExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Choose course")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
```
- The spec says "OutlinedTextField **or** Dropdown selector (e.g. CS101, MATH201, ENG102)". This field does **both**: you can type a course, or pick one.
- `placeholder`: grey example text shown while the field is empty and focused.
- `capitalization = Characters` makes the keyboard type capitals (CS101).
- **`trailingIcon`** is the slot on the right end of the field. It holds an **IconButton** with a ▼ arrow, and tapping it sets `courseMenuExpanded = true`.
- **Why a `Box`?** A DropdownMenu appears **anchored to its parent**. Putting the text field and the menu in the same Box makes the menu pop up at the text field.

```kotlin
                DropdownMenu(
                    expanded = courseMenuExpanded,
                    onDismissRequest = { courseMenuExpanded = false }
                ) {
                    for (option in courseOptions) {
                        DropdownMenuItem(
                            text = { Text("${option.courseCode} - ${option.courseName}") },
                            onClick = {
                                onCourseChange(option.courseCode)
                                courseMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }
```
- **DropdownMenu** (a selection element from L23): shown when `expanded` is true.
- `onDismissRequest`: tapping outside closes it.
- One **DropdownMenuItem** per course, showing "CS101 - Data Structures".
- On selection: send the **code** up (`onCourseChange("CS101")`) and close the menu.

### Field 3: Due Date
```kotlin
        item {
            OutlinedTextField(
                value = dueDate,
                onValueChange = onDueDateChange,
                label = { Text("Due Date *") },
                placeholder = { Text("YYYY-MM-DD") },
                singleLine = true,
                trailingIcon = {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Due date")
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
```
- The spec asks for an "OutlinedTextField **with trailing calendar icon**". The `trailingIcon` slot holds a calendar `Icon`.
- The placeholder shows the expected format, **YYYY-MM-DD**. That format matters because the Dashboard sorts due dates as text to find the most urgent ones.

### Field 4: Priority (RadioButtons)
```kotlin
        item {
            Column {
                Text(
                    text = "Priority Level",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (option in priorityOptions) {
                        Row(
                            modifier = Modifier
                                .clickable { onPriorityChange(option) }
                                .padding(end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = priority == option,
                                onClick = { onPriorityChange(option) }
                            )
                            Text(option)
                        }
                    }
                }
            }
        }
```
- The spec: "Label + `Row` of `RadioButton` options ('Low', 'Medium', 'High')".
- An outer Column holds the label above the options. An outer Row puts the 3 options side by side. Each inner Row is **RadioButton + Text** (the L23 pattern: `selected` and `onClick`).
- `selected = priority == option`: exactly one is selected.
- The inner Row is `clickable`, so tapping the word "High" also works.

### Field 5: Description (multi-line)
```kotlin
        item {
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Description / Notes") },
                minLines = 3,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth()
            )
        }
```
- The spec says multi-line with **`minLines = 3, maxLines = 5`**. The box is always at least 3 lines tall, grows up to 5, and then scrolls inside.
- There's no `singleLine`, so Enter adds a new line. It is **optional** (not part of `isFormValid`).

### Field 6: Reminder (Checkbox)
```kotlin
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onReminderChange(!reminderEnabled) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = reminderEnabled,
                    onCheckedChange = onReminderChange
                )
                Text("Enable Reminder Notification")
            }
        }
```
- The spec: "`Row` with `Checkbox` ('Enable Reminder Notification')".
- `onCheckedChange = onReminderChange` passes the new Boolean straight up.
- Tapping the text also toggles it: `!reminderEnabled` = the opposite of the current value.

### The slot
```kotlin
        item {
            actions()
        }
    }
}
```
- Calls whatever composable the parent passed: here, the Cancel/Save row and the error text.

---

## Navigation on this screen
| Action | Code | Result |
|---|---|---|
| Tap '+' (Dashboard or Assignments) | `navController.navigate("add_assignment")` | pushed on the back stack, bottom bar hidden |
| Tap ← in the top bar | `AppBar.onBackClick` → `closeAddScreen()` | popped, back to the caller |
| Tap Cancel | `onCancel` → `closeAddScreen()` | popped, nothing saved |
| Tap Save | Toast → `onSave(newAssignment)` → `closeAddScreen()` + append to list | popped, new card appears |
| Phone's Back gesture | NavHost handles it automatically | popped |

"Back arrow returns to caller" works because `popBackStack` returns to **whichever screen opened it** (Dashboard or Assignments). That's how a back stack works.

## Possible viva questions
- *Which composable is stateful and which is stateless here?* AddAssignmentScreen is stateful (it owns 6 states). AssignmentForm is stateless (parameters + callbacks).
- *How does the Save button know when to enable?* `isFormValid` is recalculated on every recomposition, and every keystroke updates state, which triggers recomposition.
- *What is a slot API? Show it.* `actions: @Composable () -> Unit` in AssignmentForm. The screen passes its buttons in.
- *What do `remember` and `mutableStateOf` each do?* mutableStateOf = observable state that triggers recomposition. remember = keep the value across recompositions.
- *What does `singleLine`/`minLines`/`maxLines` do?* They control line behaviour: the title is one line, and the description is 3–5 lines.
- *What does `imeAction = Next` do?* The keyboard shows a "Next" key that moves focus to the next field.
- *Why `imePadding()`?* So the keyboard doesn't hide the field being typed in (edge-to-edge on Android 15+).
- *Why is the dropdown's `expanded` state not hoisted?* It's internal visual state that no parent needs.
- *What happens on rotation?* `remember` state is lost on configuration change. `rememberSaveable` would keep it. The spec asked for `remember`.
