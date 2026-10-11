# Screen 3 – Profile (`profile`)

**File:** `screens/ProfileScreen.kt`
**Route:** `"profile"`, reached from the BottomBar tab.
**Purpose (from the spec):** displays student information and academic statistics.

| On screen | Code |
|---|---|
| "Student Profile" top bar | `AppBar` in the Scaffold |
| Avatar, Pramiti Udupa, PES1UG24CS331, CSE | the reusable `StudentProfile` composable |
| Academic Stats: 3.82 GPA, 84 Credits, Fall 2026 | a `Card` with a `Row` of 3 `StatItem`s |
| **Edit Profile** button | `Button` → opens `EditProfileDialog` (an AlertDialog) |
| **Export Academic Summary** button | `OutlinedButton` → implicit `Intent` (share sheet) |

Spec checklist: 'Student Profile' AppBar ✔, `StudentProfile` header (Avatar, Name, ID, Major) ✔, Academic Stats card with GPA 3.82 / Credits 84 / Fall 2026 ✔, Quick Action buttons 'Edit Profile' and 'Export Academic Summary' ✔

---

## Code walkthrough

```kotlin
@Composable
fun ProfileScreen(
    student: Student,
    onProfileUpdate: (name: String, major: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }
```
- `student` comes from the hoisted state in `StudentOrganizerApp`.
- `onProfileUpdate` is the event the screen sends **up** when the user saves new details. The parameter names `name:` and `major:` are just labels for readability.
- `LocalContext.current` gets the Android **Context** inside a composable. We need it to start an Activity (the share sheet). The same idea as `LocalContext.current` for Toast in our class ToDo app.
- `showEditDialog` is local UI state: is the dialog open? It starts `false`.

```kotlin
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
```
- A LazyColumn makes the screen **scroll** on small phones or with large font sizes. It has 3 `item`s.

### Item 1: Profile Header
```kotlin
        item {
            StudentProfile(
                studentName = student.name,
                studentId = student.studentId,
                major = student.major,
                avatarRes = student.avatarRes
            )
        }
```
- **Reuses the custom composable** `StudentProfile` (Part 3.5), passing each field of the `Student` object.

### Item 2: Academic Stats Card
```kotlin
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Academic Stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(value = "${student.gpa}", label = "GPA")
                        StatItem(value = "${student.completedCredits}", label = "Credits")
                        StatItem(value = student.currentSemester, label = "Semester")
                    }
                }
            }
        }
```
- A **Card** with the theme's `secondaryContainer` colour (green-teal), set with `CardDefaults.cardColors` (L25).
- Inside: a heading, then a **Row** of 3 stats spread out with `Arrangement.SpaceEvenly`.
- `"${student.gpa}"` converts the Double 3.82 into the String "3.82".

### Item 3: Quick Actions
```kotlin
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile")
                }
```
- A filled **Button** (L17: the 5 button types are Button, FilledTonalButton, OutlinedButton, ElevatedButton and TextButton).
- `onClick = { showEditDialog = true }` **changes state**, and on recomposition the dialog appears (see below).
- A button's content is a **slot**, so we can put an Icon + Spacer + Text inside, and they're laid out in a row.

```kotlin
                OutlinedButton(
                    onClick = {
                        val summary = "Academic Summary\n" +
                                "Name: ${student.name}\n" +
                                "Student ID: ${student.studentId}\n" +
                                "Major: ${student.major}\n" +
                                "GPA: ${student.gpa}\n" +
                                "Completed Credits: ${student.completedCredits}\n" +
                                "Current Semester: ${student.currentSemester}"
                        val sendIntent = Intent(Intent.ACTION_SEND)
                        sendIntent.type = "text/plain"
                        sendIntent.putExtra(Intent.EXTRA_TEXT, summary)
                        context.startActivity(Intent.createChooser(sendIntent, "Export Academic Summary"))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Academic Summary")
                }
            }
        }
    }
```
- An **OutlinedButton**, which is lower emphasis than the filled Edit button.
- **This uses Intents, the "Activating Components" topic from Unit 2:**
  1. Build a text summary (`\n` = new line).
  2. `Intent(Intent.ACTION_SEND)` is an **implicit intent**. We say *what* we want to do ("send some data"), not *which app* should do it.
  3. `type = "text/plain"` means we're sending plain text.
  4. `putExtra(Intent.EXTRA_TEXT, summary)` attaches the text to the intent.
  5. `Intent.createChooser(...)` makes Android show **the share sheet** (WhatsApp, Gmail, Notes and so on) with our title.
  6. `context.startActivity(...)` launches it, using the "pass an Intent to Context.startActivity()" pattern from the slides.
- **Explicit vs implicit intent:** `Intent(context, MyActivity::class.java)` (explicit) names the exact class. `ACTION_SEND` (implicit) lets Android find any app that can handle the action.

### Showing the dialog
```kotlin
    if (showEditDialog) {
        EditProfileDialog(
            currentName = student.name,
            currentMajor = student.major,
            onDismiss = { showEditDialog = false },
            onSave = { name, major ->
                onProfileUpdate(name, major)
                showEditDialog = false
            }
        )
    }
}
```
- **Declarative UI in action:** we don't "call show()". We just say *if the state is true, the dialog is part of the UI*. Setting `showEditDialog = false` removes it.
- `onSave` passes the new values **up** (`onProfileUpdate`), and `StudentOrganizerApp` does `student = student.copy(name = name, major = major)`. The header **and** the Dashboard greeting both update.

### Helper: StatItem
```kotlin
@Composable
fun StatItem(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}
```
- A small reusable composable: a big bold value with a small label under it, centred. It's used 3 times.

### Helper: EditProfileDialog
```kotlin
@Composable
fun EditProfileDialog(
    currentName: String,
    currentMajor: String,
    onDismiss: () -> Unit,
    onSave: (name: String, major: String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var major by remember { mutableStateOf(currentMajor) }
```
- **Draft state:** the dialog keeps its own copies of name and major while you type. The real `student` only changes when you press **Save**. Cancel throws the draft away.

```kotlin
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                OutlinedTextField(
                    value = major,
                    onValueChange = { major = it },
                    label = { Text("Major") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && major.isNotBlank(),
                onClick = { onSave(name.trim(), major.trim()) }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
```
- **AlertDialog** (listed in the L16 Material components) is a pop-up with slots: `title`, `text` (the body), `confirmButton` and `dismissButton`.
- `onDismissRequest`: called if the user taps outside the dialog or presses Back, so we close it.
- **OutlinedTextField** with `value` + `onValueChange` is the standard TextField pattern from L23. Every keystroke updates the state, which recomposes the field and shows the new text.
- `KeyboardOptions(capitalization = KeyboardCapitalization.Words)` auto-capitalises each word (L23: keyboard options: capitalization, autoCorrect, keyboardType, imeAction).
- `enabled = name.isNotBlank() && major.isNotBlank()`: **validation**. You can't save an empty name. `isNotBlank()` is false for "" and for "   ".
- `trim()` removes accidental spaces at the start and end.

## Possible viva questions
- *How does Edit Profile change the Dashboard greeting too?* The `student` state is hoisted in `StudentOrganizerApp`. The dialog sends the new values up with `onProfileUpdate`, the state is replaced with `copy(...)`, and every screen that reads it recomposes.
- *What kind of Intent is Export?* An implicit intent, `ACTION_SEND`, wrapped in `createChooser`.
- *Why `LocalContext.current`?* Composables aren't Activities. We need a Context to call `startActivity`.
- *How is the dialog shown and hidden?* With a Boolean state and `if (showEditDialog)`. Declarative UI.
- *How is the avatar loaded?* `Image(painterResource(R.drawable.ic_student_avatar))` with `clip(CircleShape)`.
