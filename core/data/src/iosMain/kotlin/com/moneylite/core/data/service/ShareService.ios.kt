package com.moneylite.core.data.service

import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow

actual fun shareText(text: String, title: String) {
    val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        ?: UIApplication.sharedApplication.windows.firstOrNull()?.let { (it as? UIWindow)?.rootViewController }

    if (rootViewController != null) {
        val activityViewController = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null
        )
        activityViewController.popoverPresentationController?.sourceView = rootViewController.view
        rootViewController.presentViewController(
            viewControllerToPresent = activityViewController,
            animated = true,
            completion = null
        )
    }
}
