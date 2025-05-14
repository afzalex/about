package com.templater.service

import jakarta.xml.bind.JAXBElement
import org.docx4j.TextUtils
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.docx4j.wml.P
import org.docx4j.wml.R
import org.docx4j.wml.Text
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

    fun paragraphToMarkdown(p: P): String {
        val styleId = p.pPr?.pStyle?.`val`
        val numPr = p.pPr?.numPr
        val isList = numPr != null

        val indentLevel = numPr?.ilvl?.`val`?.toInt() ?: 0
        val indent = "  ".repeat(indentLevel)

        val prefix = when {
            isList -> "$indent* " // you can use "-" or numbered logic here too
            styleId == "Heading1" -> "# "
            styleId == "Heading2" -> "## "
            styleId == "Heading3" -> "### "
            else -> ""
        }

        return prefix + p.content.filterIsInstance<R>()
                .joinToString("", transform = ::runToMarkdown)
    }

    private fun runToMarkdown(r: R): String {
        val textElement = r.content
                .find { it is JAXBElement<*> && it.declaredType == Text::class.java }
                ?.let { (it as JAXBElement<*>).value as? Text }
                ?: return ""


        val text = textElement.value

        val isBold = r.rPr?.b != null
        val isItalic = r.rPr?.i != null

        return when {
            isBold && isItalic -> "***${text}***"
            isBold -> "**${text}**"
            isItalic -> "_${text}_"
            else -> text
        }
    }
} 