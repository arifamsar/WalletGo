package com.moneylite.core.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.moneylite.core.domain.model.formatRupiahInput

object RupiahAmountVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        val formatted = original.formatRupiahInput()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val safeOffset = offset.coerceIn(0, original.length)
                if (safeOffset == 0) return 0

                val remainingDigits = original.length - safeOffset
                val transformedOffset = formatted.length - remainingDigits - (remainingDigits / 3)
                return transformedOffset.coerceIn(0, formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val safeOffset = offset.coerceIn(0, formatted.length)
                return formatted
                    .take(safeOffset)
                    .count { it.isDigit() }
                    .coerceIn(0, original.length)
            }
        }

        return TransformedText(
            text = AnnotatedString(formatted),
            offsetMapping = offsetMapping
        )
    }
}
