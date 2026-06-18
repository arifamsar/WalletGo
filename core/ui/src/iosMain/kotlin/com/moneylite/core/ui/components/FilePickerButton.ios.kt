@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package com.moneylite.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSString
import platform.Foundation.stringWithContentsOfURL
import platform.darwin.NSObject

class DocumentPickerDelegate(
    private val onPicked: (String) -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: return
        val shouldStopAccessing = url.startAccessingSecurityScopedResource()
        try {
            val content = NSString.stringWithContentsOfURL(url, NSUTF8StringEncoding, null)
            if (content != null) {
                onPicked(content)
            }
        } catch (e: Exception) {
            // Fail-safe
        } finally {
            if (shouldStopAccessing) {
                url.stopAccessingSecurityScopedResource()
            }
        }
    }
}

@Composable
actual fun FilePickerButton(
    onFileContentPicked: (String) -> Unit,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    val delegate = remember { DocumentPickerDelegate(onFileContentPicked) }

    Box(
        modifier = modifier.clickable {
            val picker = UIDocumentPickerViewController(
                documentTypes = listOf("public.data", "public.item"),
                inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
            )
            picker.delegate = delegate

            val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
                ?: UIApplication.sharedApplication.windows.firstOrNull()?.let { (it as? UIWindow)?.rootViewController }

            rootViewController?.presentViewController(picker, true, null)
        }
    ) {
        content()
    }
}
