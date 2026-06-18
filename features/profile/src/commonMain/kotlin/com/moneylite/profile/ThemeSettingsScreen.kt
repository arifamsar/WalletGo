@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class
)

package com.moneylite.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneylite.core.data.service.UserPreferences
import com.moneylite.core.ui.adaptive.AdaptiveWindowBox
import com.moneylite.core.ui.adaptive.AdaptiveWindowClass
import com.moneylite.core.ui.adaptive.isExpanded
import com.moneylite.core.ui.components.ThemePreviewCard
import com.moneylite.core.ui.theme.ThemeTemplate
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ThemeSettingsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val userPreferences = koinInject<UserPreferences>()
    val isDark by userPreferences.darkModeEnabledFlow().collectAsStateWithLifecycle(initialValue = false)
    val selectedTemplate by userPreferences.themeTemplateFlow().collectAsStateWithLifecycle(initialValue = ThemeTemplate.Default)
    val coroutineScope = rememberCoroutineScope()

    AdaptiveWindowBox(modifier = modifier) { windowClass ->
        ThemeSettingsScreenContent(
            isDark = isDark,
            selectedTemplate = selectedTemplate,
            windowClass = windowClass,
            onBack = onBack,
            onSelectTemplate = { template ->
                coroutineScope.launch {
                    userPreferences.setThemeTemplate(template)
                }
            }
        )
    }
}

@Composable
fun ThemeSettingsScreenContent(
    isDark: Boolean,
    selectedTemplate: ThemeTemplate,
    windowClass: AdaptiveWindowClass,
    onBack: () -> Unit,
    onSelectTemplate: (ThemeTemplate) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Theme",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (!windowClass.isExpanded) {
                        IconButton(
                            shapes = IconButtonDefaults.shapes(),
                            onClick = onBack
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(if (windowClass.isExpanded) 2 else 1),
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = if (windowClass.isExpanded) 760.dp else androidx.compose.ui.unit.Dp.Unspecified),
                contentPadding = PaddingValues(
                    horizontal = if (windowClass.isExpanded) 24.dp else 20.dp,
                    vertical = 8.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = ThemeTemplate.entries,
                    key = { it.id }
                ) { template ->
                    ThemePreviewCard(
                        template = template,
                        isDark = isDark,
                        selected = template == selectedTemplate,
                        onClick = { onSelectTemplate(template) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
