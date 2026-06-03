package com.moneylite.core.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeTemplateTest {

    @Test
    fun templates_haveStableIdsAndLabels() {
        val templates = ThemeTemplate.entries.map { it.id to it.label }

        assertEquals(
            listOf(
                "default" to "Default",
                "dynamic" to "Dynamic",
                "green_apple" to "Green Apple",
                "lavender" to "Lavender",
                "strawberry" to "Strawberry"
            ),
            templates
        )
    }

    @Test
    fun seededTemplates_useExpectedSeedColors() {
        assertEquals(Color(0xFF5EA61F), ThemeTemplate.GreenApple.seedColor)
        assertEquals(Color(0xFF7C5CFF), ThemeTemplate.Lavender.seedColor)
        assertEquals(Color(0xFFD9435F), ThemeTemplate.Strawberry.seedColor)
    }

    @Test
    fun fromId_returnsMatchingTemplateOrDefaultFallback() {
        assertEquals(ThemeTemplate.Lavender, ThemeTemplate.fromId("lavender"))
        assertEquals(ThemeTemplate.Default, ThemeTemplate.fromId(null))
        assertEquals(ThemeTemplate.Default, ThemeTemplate.fromId("unknown"))
    }
}
