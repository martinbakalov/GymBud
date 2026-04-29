package com.gymbud.app

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymbud.app.domain.model.ThemeMode
import com.gymbud.app.ui.screens.MainScreen
import com.gymbud.app.ui.theme.GymBudTheme

class MainActivity : AppCompatActivity() {

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        maybeRequestNotificationPermission()
        val app = application as GymBudApplication

        // Apply edge-to-edge once with transparent scrims. The icon
        // appearance follows the configuration uiMode, which Application
        // already aligned with the saved theme. Runtime theme switches
        // only flip status-bar icons via Theme.kt's LaunchedEffect — we
        // never re-call enableEdgeToEdge at runtime to avoid window
        // layout invalidation.
        val nightMask = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val initialStyle = if (nightMask == Configuration.UI_MODE_NIGHT_YES) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        }
        enableEdgeToEdge(
            statusBarStyle = initialStyle,
            navigationBarStyle = initialStyle
        )

        setContent {
            val prefs = app.preferences
            val themeMode by prefs.themeMode.collectAsStateWithLifecycle(
                initialValue = app.initialThemeMode
            )

            val systemDark = isSystemInDarkTheme()
            val effectiveDark = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            GymBudTheme(darkTheme = effectiveDark) {
                MainScreen()
            }
        }
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}