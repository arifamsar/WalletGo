@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package com.moneylite.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.writeToFile

@Composable
actual fun FileSaverButton(
    onRequestFileContent: (onReady: (String) -> Unit) -> Unit,
    fileName: String,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.clickable {
            onRequestFileContent { contentText ->
                try {
                    val tempDir = NSTemporaryDirectory()
                    val tempFilePath = tempDir + fileName
                    val nsString = (contentText as Any) as NSString
                    
                    nsString.writeToFile(
                        path = tempFilePath,
                        atomically = true,
                        encoding = NSUTF8StringEncoding,
                        error = null
                    )
                    
                    val fileUrl = NSURL.fileURLWithPath(tempFilePath)
                    val picker = UIDocumentPickerViewController(forExportingURLs = listOf(fileUrl))
                    
                    val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
                        ?: UIApplication.sharedApplication.windows.firstOrNull()?.let { (it as? UIWindow)?.rootViewController }
                    
                    rootViewController?.presentViewController(picker, true, null)
                } catch (e: Exception) {
                    // Fail-safe
                }
            }
        }
    ) {
        content()
    }
}
