package com.builderbears.align.ui.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

fun buildParticipantNamesAnnotated(rawNames: List<String>): AnnotatedString = buildAnnotatedString {
    val names = rawNames.map { it.trim() }.filter { it.isNotBlank() }
    val visibleNames = names.take(3)

    fun appendBoldName(name: String) {
        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
        append(name)
        pop()
    }

    when (visibleNames.size) {
        0 -> Unit
        1 -> appendBoldName(visibleNames[0])
        2 -> {
            appendBoldName(visibleNames[0])
            append(" and ")
            appendBoldName(visibleNames[1])
        }
        else -> {
            appendBoldName(visibleNames[0])
            append(", ")
            appendBoldName(visibleNames[1])
            val moreCount = names.size - 3
            if (moreCount > 0) {
                append(", ")
                appendBoldName(visibleNames[2])
                append(" +$moreCount more")
            } else {
                append(", and ")
                appendBoldName(visibleNames[2])
            }
        }
    }
}
