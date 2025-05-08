package com.templater.service

import org.docx4j.TextUtils
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.docx4j.wml.P
import org.springframework.stereotype.Service

@Service
object DocxSectionUtils {
    fun getSectionContentByHeading(doc: WordprocessingMLPackage, heading: String): List<Any> {
        val bodyContent = doc.mainDocumentPart.jaxbElement.body.content
        var found = false
        val sectionContent = mutableListOf<Any>()

        for (content in bodyContent) {
            if (content is P && isHeading(content)) {
                val currentHeading = TextUtils.getText(content).trim()
                if (found) break // End of section
                if (currentHeading.equals(heading, ignoreCase = true)) {
                    found = true
                }
            } else if (found) {
                sectionContent.add(content)
            }
        }
        return sectionContent
    }

    private fun isHeading(paragraph: P): Boolean {
        val style = paragraph.pPr?.pStyle?.`val` ?: return false
        return style.startsWith("Heading") || style.matches(Regex("Heading ?\\d+"))
    }
} 