package com.templater.model

import org.docx4j.TextUtils
import org.docx4j.wml.P

/**
 * Represents a section in a Word document, identified by its heading and containing all content until the next heading.
 */
data class Section(
    val heading: String,
    val content: List<Any>
) {
    val isEmpty: Boolean = run {
        val combinedText = content
            .filterIsInstance<P>() // keep only paragraphs
            .joinToString(separator = " ") { TextUtils.getText(it) }
            .trim()

        combinedText.isBlank() ||
        combinedText.contains("data not available", ignoreCase = true)
    }
} 