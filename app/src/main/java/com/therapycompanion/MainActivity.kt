package com.therapycompanion

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.therapycompanion.data.model.UserSettings
import com.therapycompanion.ui.navigation.EXTRA_NAVIGATE_TO
import com.therapycompanion.ui.navigation.NAVIGATE_TO_TODAY
import com.therapycompanion.ui.navigation.TherapyCompanionNavGraph
import com.therapycompanion.ui.theme.TherapyCompanionTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled in Settings screen */}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission on Android 13+ — once, with context
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Read deep-link destination from notification tap
        val startDestination = when (intent?.getStringExtra(EXTRA_NAVIGATE_TO)) {
            NAVIGATE_TO_TODAY -> NAVIGATE_TO_TODAY
            else -> null
        }

        setContent {
            val app = applicationContext as TherapyCompanionApp
            val settings by app.userSettingsRepository.getUserSettings()
                .collectAsState(initial = UserSettings.Default)

            val darkTheme = when (settings.themeMode) {
                "Dark" -> true
                "Light" -> false
                else -> isSystemInDarkTheme()
            }

            TherapyCompanionTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TherapyCompanionNavGraph(
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
