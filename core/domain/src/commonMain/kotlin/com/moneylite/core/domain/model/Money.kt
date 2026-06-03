package com.moneylite.core.domain.model

fun Long.formatToRupiah(): String {
    val isNegative = this < 0
    val absVal = if (isNegative) -this else this
    val str = absVal.toString()
    val builder = StringBuilder()
    var count = 0
    for (i in str.length - 1 downTo 0) {
        builder.append(str[i])
        count++
        if (count % 3 == 0 && i > 0) {
            builder.append('.')
        }
    }
    val formatted = builder.reverse().toString()
    return if (isNegative) "-Rp $formatted" else "Rp $formatted"
}

fun String.formatRupiahInput(): String {
    val digits = rupiahDigits()
    if (digits.isEmpty()) return ""

    val builder = StringBuilder()
    var count = 0
    for (i in digits.length - 1 downTo 0) {
        builder.append(digits[i])
        count++
        if (count % 3 == 0 && i > 0) {
            builder.append('.')
        }
    }
    return builder.reverse().toString()
}

fun String.parseRupiahToLong(): Long? {
    val cleaned = rupiahDigits()
    return cleaned.toLongOrNull()
}

fun String.rupiahDigits(): String = filter { it.isDigit() }
