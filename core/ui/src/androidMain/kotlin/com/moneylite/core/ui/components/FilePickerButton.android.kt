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
import java.nio.ByteBuffer

@Composable
actual fun FilePickerButton(
    onFileContentPicked: (String) -> Unit,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            Log.d("FilePicker", "Picker cancelled: returned URI is null")
            return@rememberLauncherForActivityResult
        }
        
        Log.d("FilePicker", "Picked file URI: $uri")
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
                ?: throw IOException("Could not open input stream for selected file: $uri")
            
            val bytes = inputStream.use { it.readBytes() }
            Log.d("FilePicker", "Read ${bytes.size} bytes from $uri")
            
            if (bytes.isEmpty()) {
                throw IOException("Selected file is empty (0 bytes).")
            }
            
            // Try UTF-8 first, fallback to ISO-8859-1 if it fails
            val decodedText = try {
                val decoder = Charsets.UTF_8.newDecoder()
                decoder.decode(ByteBuffer.wrap(bytes)).toString()
            } catch (e: Exception) {
                Log.w("FilePicker", "UTF-8 decoding failed, falling back to ISO-8859-1", e)
                String(bytes, Charsets.ISO_8859_1)
            }
            
            Log.d("FilePicker", "Successfully decoded text: ${decodedText.take(100)}...")
            onFileContentPicked(decodedText)
            
        } catch (e: Exception) {
            Log.e("FilePicker", "Failed to process picked file", e)
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                Toast.makeText(
                    context,
                    "Failed to read file: ${e.localizedMessage ?: e.message ?: "Unknown error"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    Box(
        modifier = modifier.clickable {
            try {
                launcher.launch(arrayOf("application/json", "*/*"))
            } catch (e: Exception) {
                Log.e("FilePicker", "Failed to launch file picker", e)
                Toast.makeText(
                    context,
                    "Failed to launch file picker: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    ) {
        content()
    }
}
