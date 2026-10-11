# Student Organizer App – Study Guide (start here)

Read these in order. Each file explains the code **block by block**, says *why* it is written that way, and ends with likely viva questions.

| # | File | Covers |
|---|---|---|
| 1 | [01_Setup_Manifest_Theme.md](01_Setup_Manifest_Theme.md) | Gradle, manifest, resources, colours, theme, `SideEffect` |
| 2 | [02_MainActivity_Navigation_Models.md](02_MainActivity_Navigation_Models.md) | MainActivity + lifecycle, `Screen` sealed class, data models, **NavHost + Scaffold** (most important) |
| 3 | [03_Custom_Composables.md](03_Custom_Composables.md) | AppBar, BottomBar, CourseInfo, AssignmentCard, StudentProfile, @Preview |
| 4 | [04_Screen1_Dashboard.md](04_Screen1_Dashboard.md) | Screen 1 – Dashboard |
| 5 | [05_Screen2_Assignments.md](05_Screen2_Assignments.md) | Screen 2 – Assignments list + filter |
| 6 | [06_Screen3_Profile.md](06_Screen3_Profile.md) | Screen 3 – Profile, Edit dialog, Export intent |
| 7 | [07_Screen4_Settings.md](07_Screen4_Settings.md) | Screen 4 – Settings, dark mode journey |
| 8 | [08_Screen5_AddAssignment.md](08_Screen5_AddAssignment.md) | Screen 5 – Add Assignment + AssignmentForm (state hoisting, validation, slot) |
| 9 | [09_Viva_Questions.md](09_Viva_Questions.md) | Syllabus map + 37 likely questions with answers |

**If you only have 1 hour:** read Part 2 section 2.4, Part 8 "The big idea", and Part 9.

---

## Where everything is on your laptop

**Project folder:** `C:\Users\smith\AndroidStudioProjects\StudentOrganizerApp`

```
StudentOrganizerApp/
├── app/
│   ├── build.gradle.kts                      ← dependencies (navigation-compose etc.)
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/                              ← strings, avatar drawable, icons
│       └── java/com/pes/studentorganizer/
│           ├── MainActivity.kt               ← entry point
│           ├── StudentOrganizerApp.kt        ← NavHost + Scaffold + shared state
│           ├── navigation/Screen.kt          ← 5 routes (sealed class)
│           ├── model/Models.kt               ← data classes + sample data (your name is here)
│           ├── components/                   ← 6 reusable composables
│           ├── screens/                      ← the 5 screens
│           └── ui/theme/                     ← colours, typography, theme
├── docs/                                     ← THIS study guide + screenshots
├── gradle/libs.versions.toml                 ← library versions
├── README.md                                 ← GitHub front page
└── .gitignore                                ← keeps build files out of Git
```

**Installed APK (after a build):** `app\build\outputs\apk\debug\app-debug.apk`. This is ignored by Git, which is correct.

---

## Recording the demo video
1. On the S22, swipe down twice for Quick Settings, then tap **Screen recorder** (choose "No sound" or "Media sounds").
2. Demo in this order (about 2 minutes):
   - Dashboard
   - tick an urgent deadline (Pending 7 → 6)
   - the Assignments tab with its filters
   - **+**, then try Save while it's disabled, fill the form (pick a course from ▼) and Save
   - Profile, then Edit Profile (change the name and see the greeting change), then Export (show the share sheet and cancel)
   - Settings, then Dark Mode
3. The video is saved in **Gallery → Screen recordings** (phone folder `DCIM/Screen recordings`).
4. Copy it into the project: plug in USB and copy it in File Explorer to `StudentOrganizerApp\demo\demo.mp4`.
   - **GitHub's limit is 100 MB per file.** A 2-minute recording is usually 30–80 MB. If it's bigger, upload it to Google Drive or YouTube (unlisted) and paste the link in `README.md` instead.

## Pushing to GitHub
1. On github.com, click **New repository**, name it `StudentOrganizerApp`, and **don't** tick "Add a README" (we already have one). Click Create.
2. In Android Studio, open **Terminal** (bottom bar) and run these one at a time:
```bash
git init
git add .
git commit -m "Student Organizer App - Level 2 assignment"
git branch -M main
git remote add origin https://github.com/<your-username>/StudentOrganizerApp.git
git push -u origin main
```
3. If you add the video later:
```bash
git add demo/demo.mp4
git commit -m "Add demo video"
git push
```
`.gitignore` already keeps out `build/`, `.gradle/`, `local.properties` (your SDK path) and other machine-specific files, so only the clean source code is pushed.

## Practising before Monday
- Run from Android Studio: connect the S22, choose it in the device dropdown, and press the green **Run ▶**.
- Keep **auto-rotate off** during the demo. Rotating recreates the Activity, and `remember` state (like added assignments) resets. If asked, explain it with Q33 in Part 9.
- Open **Logcat**, type `MainActivity` in the filter, and press Home / reopen the app to show the lifecycle callbacks.
- Open any screen file and click **Split** (top right of the editor) to show the `@Preview`.
