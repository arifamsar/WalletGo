package com.moneylite

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moneylite.core.data.di.dataModule
import com.moneylite.core.data.service.UserPreferences
import com.moneylite.core.ui.theme.AppTheme
import com.moneylite.navigation.AppNavigation
import com.moneylite.ui.di.viewModelModule
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
fun App(
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {}
) {
    KoinApplication(
        application = {
            modules(dataModule, viewModelModule)
        }
    ) {
        val appState: AppState = rememberAppState()
        val userPreferences = koinInject<UserPreferences>()
        val isDark by userPreferences.darkModeEnabledFlow().collectAsStateWithLifecycle(initialValue = null)
        val themeTemplate by userPreferences.themeTemplateFlow().collectAsStateWithLifecycle(initialValue = null)

        // Only render when theme preference is loaded to prevent flash
        val darkMode = isDark
        val template = themeTemplate
        if (darkMode != null && template != null) {
            AppTheme(
                isDark = darkMode,
                themeTemplate = template,
                onThemeChanged = onThemeChanged
            ) {
                AppNavigation(
                    appState = appState
                )
            }
        }
    }
}
