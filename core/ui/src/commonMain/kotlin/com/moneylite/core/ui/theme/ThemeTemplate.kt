package com.moneylite.core.ui.theme

import androidx.compose.ui.graphics.Color

enum class ThemeTemplate(
    val id: String,
    val label: String,
    val seedColor: Color
) {
    Default(
        id = "default",
        label = "Default",
        seedColor = BluePrimary
    ),
    Dynamic(
        id = "dynamic",
        label = "Dynamic",
        seedColor = BluePrimary
    ),
    GreenApple(
        id = "green_apple",
        label = "Green Apple",
        seedColor = Color(0xFF5EA61F)
    ),
    Lavender(
        id = "lavender",
        label = "Lavender",
        seedColor = Color(0xFF7C5CFF)
    ),
    Strawberry(
        id = "strawberry",
        label = "Strawberry",
        seedColor = Color(0xFFD9435F)
    );

    companion object {
        fun fromId(id: String?): ThemeTemplate =
            entries.firstOrNull { it.id == id } ?: Default
    }
}
