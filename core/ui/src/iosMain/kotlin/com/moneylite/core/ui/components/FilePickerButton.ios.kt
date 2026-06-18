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
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfURL
import platform.darwin.NSObject
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.memcpy

class DocumentPickerDelegate(
    private val onPicked: (String) -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: return
        val shouldStopAccessing = url.startAccessingSecurityScopedResource()
        try {
            println("FilePickerButton: Picked file URL = ${url.absoluteString}")
            val nsData = NSData.dataWithContentsOfURL(url)
            if (nsData != null) {
                val length = nsData.length.toInt()
                val bytes = nsData.bytes?.reinterpret<ByteVar>()
                if (bytes != null && length > 0) {
                    val byteArray = ByteArray(length)
                    byteArray.usePinned { pinned ->
                        memcpy(pinned.addressOf(0), bytes, nsData.length)
                    }
                    val content = byteArray.decodeToString()
                    println("FilePickerButton: Successfully read $length bytes")
                    onPicked(content)
                } else {
                    println("FilePickerButton: Bytes or length is null/empty")
                }
            } else {
                println("FilePickerButton: NSData.dataWithContentsOfURL returned null")
            }
        } catch (e: Exception) {
            println("FilePickerButton ERROR: ${e.message}")
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
