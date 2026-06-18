package com.moneylite.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun FileSaverButton(
    onRequestFileContent: (onReady: (String) -> Unit) -> Unit,
    fileName: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)
