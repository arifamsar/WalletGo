package com.moneylite.core.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun FileSaverButton(
    onRequestFileContent: (onReady: (String) -> Unit) -> Unit,
    fileName: String,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            onRequestFileContent { contentText ->
                try {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(contentText.toByteArray())
                    }
                } catch (e: Exception) {
                    // Fail-safe
                }
            }
        }
    }

    Box(
        modifier = modifier.clickable {
            launcher.launch(fileName)
        }
    ) {
        content()
    }
}
