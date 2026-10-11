package com.pes.studentorganizer

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.pes.studentorganizer.ui.theme.StudentOrganizerTheme

// The Activity is still the entry point of a Compose app.
// It is the only Activity - the 5 screens are composables inside NavHost.
class MainActivity : ComponentActivity() {

    private val tag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate called")
        enableEdgeToEdge()
        // setContent - sets composables (instead of an XML layout) as the UI of this Activity
        setContent {
            // Dark mode state is kept here, ABOVE the theme,
            // because the theme itself must change when the Switch is toggled.
            var isDarkMode by remember { mutableStateOf(false) }

            StudentOrganizerTheme(darkTheme = isDarkMode) {
                StudentOrganizerApp(
                    isDarkMode = isDarkMode,
                    onDarkModeChange = { isDarkMode = it }
                )
            }
        }
    }

    // Lifecycle callbacks - called automatically by the ActivityManager (see Logcat)
    override fun onStart() {
        super.onStart()
        Log.d(tag, "onStart called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(tag, "onResume called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(tag, "onPause called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(tag, "onStop called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "onDestroy called")
    }
}
