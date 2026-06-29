package com.moneylite.core.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import android.widget.Toast
import java.io.IOException

@Composable
actual fun FileSaverButton(
    onRequestFileContent: (onReady: (String) -> Unit) -> Unit,
    fileName: String,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) {
            Log.d("FileSaver", "Saver cancelled: returned URI is null")
            return@rememberLauncherForActivityResult
        }
        
        Log.d("FileSaver", "Saving file to URI: $uri")
        onRequestFileContent { contentText ->
            try {
                val contentResolver = context.contentResolver
                val outputStream = contentResolver.openOutputStream(uri)
                    ?: throw IOException("Could not open output stream for destination URI: $uri")
                
                outputStream.use { stream ->
                    stream.write(contentText.toByteArray(Charsets.UTF_8))
                }
                Log.d("FileSaver", "Successfully wrote ${contentText.length} characters (UTF-8 bytes) to $uri")
                
            } catch (e: Exception) {
                Log.e("FileSaver", "Failed to write file content", e)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    Toast.makeText(
                        context,
                        "Failed to save file: ${e.localizedMessage ?: e.message ?: "Unknown error"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    Box(
        modifier = modifier.clickable {
            try {
                launcher.launch(fileName)
            } catch (e: Exception) {
                Log.e("FileSaver", "Failed to launch save dialog", e)
                Toast.makeText(
                    context,
                    "Failed to save file: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    ) {
        content()
    }
}
