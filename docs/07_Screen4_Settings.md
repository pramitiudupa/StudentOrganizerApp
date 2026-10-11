# Screen 4 – Settings (`settings`)

**File:** `screens/SettingsScreen.kt`
**Route:** `"settings"`, reached from the BottomBar tab.
**Purpose (from the spec):** lets users configure app preferences.

| On screen | Code |
|---|---|
| "Settings" top bar | `AppBar` in the Scaffold |
| Dark Mode + switch | `Row` with `SettingLabel` + `Switch` |
| Push Notifications + checkbox | `Row` with `SettingLabel` + `Checkbox` |
| Default Start Screen: ○ Dashboard ○ Assignments | `RadioButton` group |

Spec checklist: 'Settings' AppBar ✔, Preferences List as a `Column` ✔, Dark Mode `Row` + `Switch` ✔, Push Notifications `Row` + `Checkbox` ✔, Default Start Screen radio group (Dashboard vs Assignments) ✔

---

## Code walkthrough

```kotlin
val startScreenOptions = listOf("Dashboard", "Assignments")

@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    notificationsEnabled: Boolean,
    onNotificationsChange: (Boolean) -> Unit,
    defaultStartScreen: String,
    onDefaultStartScreenChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
```
- **A fully stateless screen.** Every setting arrives as a **value + callback pair**, which is the clearest example of **state hoisting** in the app.
- **Why hoist settings instead of `remember` inside this screen?** When you switch to another tab, SettingsScreen leaves the composition and its `remember` values would be **lost**. Coming back would reset every switch. Hoisted in `StudentOrganizerApp` (and dark mode in `MainActivity`), they survive tab changes.

```kotlin
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
```
- The spec asks for a **Column** containing the preference items, so that's what this is.

### Dark Mode: Row + Switch
```kotlin
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingLabel(
                icon = Icons.Default.DarkMode,
                title = "Dark Mode",
                subtitle = "Use a dark colour theme",
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = isDarkMode,
                onCheckedChange = onDarkModeChange
            )
        }
        HorizontalDivider()
```
- **Row:** the label on the left, the Switch on the right.
- `Modifier.weight(1f)` on the label makes it take all the spare space, which **pushes the Switch to the right edge** (the "pushing elements on the main axis" idea from L19).
- **Switch** (a selection element from L23) works like a Checkbox: `checked` shows the state, and `onCheckedChange` gives the new Boolean.
- `onCheckedChange = onDarkModeChange` passes the callback straight through. The new value travels **up to MainActivity**, `isDarkMode` changes, `StudentOrganizerTheme` recomposes with the dark colour scheme, and **the whole app turns dark instantly**.

### Push Notifications: Row + Checkbox
```kotlin
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingLabel(
                icon = Icons.Default.Notifications,
                title = "Push Notifications",
                subtitle = "Get alerts for upcoming deadlines",
                modifier = Modifier.weight(1f)
            )
            Checkbox(
                checked = notificationsEnabled,
                onCheckedChange = onNotificationsChange
            )
        }
        HorizontalDivider()
```
- The same pattern with a **Checkbox**, as the spec asks.
- This stores the user's **preference** in state. Actually sending notifications is outside this assignment's scope (UI only).

### Default Start Screen: RadioButton group
```kotlin
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            SettingLabel(
                icon = Icons.Default.Home,
                title = "Default Start Screen",
                subtitle = "Screen shown when the app opens"
            )
            for (option in startScreenOptions) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDefaultStartScreenChange(option) }
                        .padding(start = 28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = defaultStartScreen == option,
                        onClick = { onDefaultStartScreenChange(option) }
                    )
                    Text(option)
                }
            }
        }
    }
}
```
- A loop creates one Row per option: **RadioButton + Text in a Row** (the L23 pattern).
- `selected = defaultStartScreen == option`: only one can be selected at a time (that's what makes it a radio *group*).
- `.clickable { }` makes the whole row tappable.
- **Modifier order:** `clickable` comes **before** `padding`, so the clickable area includes the padding.
- Because there's no database (out of scope), this preference is held in state for the current session.

### Helper: SettingLabel
```kotlin
@Composable
fun SettingLabel(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
```
- Icon + title + subtitle, written **once** and used 3 times, so every setting row looks consistent.
- `ImageVector` is the type of Material icons like `Icons.Default.DarkMode`.

## Dark mode, the full journey (likely question!)
```
Switch tapped
  → onCheckedChange(true)
  → SettingsScreen's onDarkModeChange(true)
  → StudentOrganizerApp passes it through (onDarkModeChange)
  → MainActivity: isDarkMode = true          (state changes)
  → recomposition of StudentOrganizerTheme(darkTheme = true)
  → colorScheme = DarkColorScheme → every MaterialTheme.colorScheme colour changes
  → SideEffect sets status-bar icons to white
```

## Possible viva questions
- *Switch vs Checkbox?* Both are Boolean toggles with `checked` + `onCheckedChange`. A Switch is usually used for on/off settings, and a Checkbox for selecting options.
- *Why is this screen stateless?* So the values survive tab changes and dark mode can affect the theme above it.
- *How does `weight(1f)` push the switch right?* The weighted child takes all the remaining width in the Row.
- *Are these preferences saved after closing the app?* No. Persistence (DataStore or Room) is outside this assignment's scope, which says to use UI state only.
