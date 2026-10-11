# Part 1 – Project Setup, Manifest, Resources & Theme

These are the files that are not screens: they set up the project and give the app its look.

---

## 1.1 `app/build.gradle.kts`: the app module's build file

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}
```
- `android.application` says this module builds an Android **app** (an .apk).
- `kotlin.compose` turns on the **Compose compiler**, which processes every `@Composable` function.

```kotlin
android {
    namespace = "com.pes.studentorganizer"
    compileSdk { version = release(37) }
    defaultConfig {
        applicationId = "com.pes.studentorganizer"
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        ...
    }
    ...
    buildFeatures { compose = true }
}
```
| Setting | Meaning |
|---|---|
| `namespace` | Package used for the generated `R` class (`com.pes.studentorganizer.R`) |
| `applicationId` | The app's unique ID on the phone / Play Store |
| `compileSdk 37` | The Android version whose APIs we compile against |
| `minSdk 29` | Oldest Android version that can install the app (Android 10) |
| `targetSdk 37` | Android version the app is designed and tested for |
| `compose = true` | Enables Jetpack Compose in this module |

```kotlin
dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    ...
    // Task 1: Jetpack Compose Navigation
    implementation(libs.androidx.navigation.compose)
}
```
- **Compose BOM** (Bill of Materials) picks matching versions for all Compose libraries, so we don't write a version for each.
- `activity-compose` provides `ComponentActivity.setContent { }`.
- `material3` provides the Material Design components: `Button`, `Card`, `Scaffold`, `TopAppBar` and so on.
- `material-icons-extended` provides icons like `Icons.Default.School` and `Icons.Default.CalendarMonth`.
- **`navigation-compose`** provides `NavHost`, `rememberNavController()` and `composable()`. **Task 1 of the assignment asks for exactly this dependency.**

The real version numbers live in `gradle/libs.versions.toml` (the "version catalog"), for example `navigationCompose = "2.10.1"`.

---

## 1.2 `AndroidManifest.xml`

```xml
<application
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:theme="@style/Theme.StudentOrganizer">

    <activity
        android:name=".MainActivity"
        android:exported="true"
        android:windowSoftInputMode="adjustResize">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
</application>
```
- The manifest tells Android **which components the app has** (here, one Activity) and **which Activity to launch when the icon is tapped**.
- `MAIN` + `LAUNCHER` in the intent-filter = "put this Activity on the home screen and start it when tapped".
- `exported="true"` means other apps (the launcher) may start it. It is required for the launcher activity.
- There is **only one Activity**. All 5 screens are composables shown inside `NavHost`, so there's no need for 5 Activities (the "single-activity" approach).
- No `<uses-permission>` is needed, because the app has no internet, camera or storage access.

---

## 1.3 Resources (`res/` folder)

| File | What it holds | Used where |
|---|---|---|
| `values/strings.xml` | `app_name` = "Student Organizer", `student_avatar_description` | App label; `stringResource()` in `StudentProfile` |
| `drawable/ic_student_avatar.xml` | A **vector drawable** (blue circle + white person) | `painterResource(R.drawable.ic_student_avatar)` in `StudentProfile` |
| `values/themes.xml` | The XML theme for the window before Compose draws | Manifest `android:theme` |
| `mipmap-*/ic_launcher` | The app icon | Manifest `android:icon` |

**Why a vector drawable?** It is drawn from path data, so it stays sharp at any size and works with `painterResource` (as in the L17 slides: "load a PNG, JPEG, WEBP **or vector resource**").

---

## 1.4 `ui/theme/Color.kt`

```kotlin
val Navy40 = Color(0xFF1F4E79)
val NavyContainer90 = Color(0xFFD3E4FF)
...
val PriorityHigh = Color(0xFFC62828)
val PriorityMedium = Color(0xFFEF6C00)
val PriorityLow = Color(0xFF2E7D32)
```
- Just named colour constants. `0xFF1F4E79` is ARGB: `FF` = fully opaque, then red `1F`, green `4E`, blue `79`.
- The numbers 40/80/90 follow Material's naming: lower = darker shade.
- `PriorityHigh/Medium/Low` are the red/orange/green badge colours used in `AssignmentCard`.

## 1.5 `ui/theme/Type.kt`

```kotlin
val Typography = Typography(
    bodyLarge = TextStyle(fontSize = 16.sp, ...),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, ...)
)
```
- Defines text styles. Screens use them with `style = MaterialTheme.typography.titleLarge`.
- `sp` = scale-independent pixels, the unit for text sizes (respects the user's font-size setting).

## 1.6 `ui/theme/Theme.kt`

```kotlin
private val LightColorScheme = lightColorScheme(
    primary = Navy40,
    primaryContainer = NavyContainer90,
    ...
)
private val DarkColorScheme = darkColorScheme(
    primary = Navy80,
    ...
)
```
- Two **colour schemes**. Every component asks the theme for colours like `MaterialTheme.colorScheme.primary`, so switching the scheme recolours the whole app.

```kotlin
@Composable
fun StudentOrganizerTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
```
- The theme is **itself a composable** that wraps the whole app (`content`).
- `darkTheme` is a **parameter**. MainActivity passes `isDarkMode`, so the Settings switch can change it while the app is running.
- `content: @Composable () -> Unit` is a **slot**: whatever UI we put inside `StudentOrganizerTheme { ... }`.

```kotlin
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }
```
- **This is the app's side effect (L28: Side Effects).** Changing the status-bar icon colour affects something **outside Compose** (the Android window), so it must go in a side-effect handler, not directly in the composable body.
- `SideEffect { }` runs **after every successful recomposition**. When dark mode flips, the theme recomposes, and SideEffect updates the icons.
- `isAppearanceLightStatusBars = true` means "the bar behind the icons is light, so draw **dark** icons". In dark mode we set it to `false`, so the icons become white.
- `view.isInEditMode` is true inside Android Studio's **@Preview**, where there is no real Activity window. The check stops the preview from crashing.
- `LocalView.current` gives the Android View that hosts our Compose UI. We use it to reach the Activity's window.

```kotlin
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```
- `MaterialTheme` makes the colours and typography available to every composable inside `content`.

> **Viva line:** "The theme is a composable with a `darkTheme` parameter. Because the dark-mode state is hoisted above the theme in MainActivity, toggling the Switch recomposes the theme with the dark colour scheme, and a `SideEffect` updates the status-bar icon colour."
