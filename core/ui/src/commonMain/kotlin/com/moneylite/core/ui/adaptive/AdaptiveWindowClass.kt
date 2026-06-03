package com.moneylite.core.ui.adaptive

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AdaptiveWindowClass {
    Compact,
    Medium,
    Expanded
}

val AdaptiveWindowClass.isMediumOrExpanded: Boolean
    get() = this != AdaptiveWindowClass.Compact

val AdaptiveWindowClass.isExpanded: Boolean
    get() = this == AdaptiveWindowClass.Expanded

fun adaptiveWindowClass(width: Dp): AdaptiveWindowClass = when {
    width >= 840.dp -> AdaptiveWindowClass.Expanded
    width >= 600.dp -> AdaptiveWindowClass.Medium
    else -> AdaptiveWindowClass.Compact
}

@Composable
fun AdaptiveWindowBox(
    modifier: Modifier = Modifier,
    content: @Composable (AdaptiveWindowClass) -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        content(adaptiveWindowClass(maxWidth))
    }
}
