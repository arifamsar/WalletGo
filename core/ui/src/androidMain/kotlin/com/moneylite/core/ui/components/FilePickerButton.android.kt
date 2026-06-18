package com.moneylite.core.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun FilePickerButton(
    onFileContentPicked: (String) -> Unit,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val text = inputStream?.bufferedReader()?.use { it.readText() }
                if (text != null) {
                    onFileContentPicked(text)
                }
            } catch (e: Exception) {
                // Fail-safe
            }
        }
    }

    Box(
        modifier = modifier.clickable {
            launcher.launch("*/*")
        }
    ) {
        content()
    }
}
