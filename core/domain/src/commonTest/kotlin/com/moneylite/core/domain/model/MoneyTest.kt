package com.moneylite.core.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class MoneyTest {

    @Test
    fun formatRupiahInput_groupsDigitsWithRupiahSeparators() {
        assertEquals("", "".formatRupiahInput())
        assertEquals("999", "999".formatRupiahInput())
        assertEquals("1.000", "1000".formatRupiahInput())
        assertEquals("12.000", "12000".formatRupiahInput())
        assertEquals("1.234.567", "Rp 1.234.567".formatRupiahInput())
    }
}
