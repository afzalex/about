package com.templater.model

import org.docx4j.TextUtils
import org.docx4j.wml.P

/**
 * Represents a section in a Word document, identified by its heading and containing all content until the next heading.
 */
data class Section(
    private val rawHeading: String,
    val content: List<Any>
) {
    companion object {
        private const val ABSTRACT_TAG = "#abstract"
    }

    val isAbstract: Boolean = rawHeading.contains(ABSTRACT_TAG, ignoreCase = true)

    val heading: String = rawHeading
        .replace(ABSTRACT_TAG, "", ignoreCase = true)
        .trim()

    val isEmpty: Boolean = run {
        val combinedText = content.filterIsInstance<P>()
            .joinToString(separator = " ") { TextUtils.getText(it) }
            .trim()

        combinedText.isBlank() || combinedText.contains("data not available", ignoreCase = true)
    }
}