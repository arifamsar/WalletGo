package com.moneylite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.moneylite.core.data.local.database.initRoomDatabase
import com.moneylite.core.data.service.initPreferencesDataStore

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initPreferencesDataStore(applicationContext)
        initRoomDatabase(applicationContext)
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, arrayOf(permission), 1)
            }
        }
        enableEdgeToEdge()
        setContent { 
            App(
                onThemeChanged = { isDark ->
                    val view = LocalView.current
                    if (!view.isInEditMode) {
                        SideEffect {
                            val window = this@AppActivity.window
                            val insetsController = WindowCompat.getInsetsController(window, view)
                            insetsController.isAppearanceLightStatusBars = !isDark
                            insetsController.isAppearanceLightNavigationBars = !isDark
                        }
                    }
                }
            )
        }
    }
}

