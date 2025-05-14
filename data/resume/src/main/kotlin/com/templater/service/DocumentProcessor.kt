package com.templater.service

import com.templater.model.VariableManager
import org.docx4j.Docx4J
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart
import org.docx4j.wml.*
import org.docx4j.openpackaging.exceptions.Docx4JException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.util.regex.Pattern
import org.docx4j.TextUtils
import com.templater.model.Section

@Service
class DocumentProcessor(private val variableManager: VariableManager) {
    private val logger = LoggerFactory.getLogger(DocumentProcessor::class.java)
    private val variablePattern = Pattern.compile("\\bdata_([\\w_]+)\\b")

    @Throws(Docx4JException::class)
    fun processDocument(baseDocumentPath: String, concreteDocumentPath: String, targetDocumentPath: String) {
        try {
            logger.info("Starting document merge process")
            val baseDocument = Docx4J.load(File(baseDocumentPath))
            val concreteDocument = Docx4J.load(File(concreteDocumentPath))

            processHeadersAndFooters(baseDocument)
            mergeStyles(baseDocument, concreteDocument)
            mergeContent(baseDocument, concreteDocument)
            Docx4J.save(baseDocument, File(targetDocumentPath))
            logger.info("Document merge completed successfully")
        } catch (e: Docx4JException) {
            logger.error("Document processing failed: {}", e.message, e)
            throw e
        }
    }

    @Throws(Docx4JException::class)
    private fun processHeadersAndFooters(document: WordprocessingMLPackage) {
        val mainDocumentPart = document.mainDocumentPart
        val relationshipsPart = mainDocumentPart.relationshipsPart

        // Process headers and footers silently
        relationshipsPart.getRelationshipsByType("http://schemas.openxmlformats.org/officeDocument/2006/relationships/header")
            .forEach { relationship ->
                val headerPart = relationshipsPart.getPart(relationship) as HeaderPart
                replaceVariablesInPart(headerPart)
            }

        relationshipsPart.getRelationshipsByType("http://schemas.openxmlformats.org/officeDocument/2006/relationships/footer")
            .forEach { relationship ->
                val footerPart = relationshipsPart.getPart(relationship) as FooterPart
                replaceVariablesInPart(footerPart)
            }
    }

    private fun replaceVariablesInPart(documentPart: org.docx4j.openpackaging.parts.JaxbXmlPart<*>) {
        when (val element = documentPart.jaxbElement) {
            is Hdr -> processContent(element.content)
            is Ftr -> processContent(element.content)
        }
    }

    private fun replaceVariablesRecursive(obj: Any?, visited: MutableSet<Any> = mutableSetOf()) {
        if (obj == null || obj in visited) return
        visited.add(obj)

        when (obj) {
            is List<*> -> obj.forEach { replaceVariablesRecursive(it, visited) }
            is org.docx4j.wml.Text -> {
                val textValue = obj.value
                val matcher = variablePattern.matcher(textValue)
                val stringBuffer = StringBuffer()
                while (matcher.find()) {
                    val variableName = matcher.group(1)
                    val replacement = variableManager.getVariableValue(variableName)
                    matcher.appendReplacement(stringBuffer, replacement)
                }
                matcher.appendTail(stringBuffer)
                obj.value = stringBuffer.toString()
            }
            else -> {
                obj.javaClass.declaredFields.forEach { field ->
                    if (field.name == "parent") return@forEach // skip parent references
                    field.isAccessible = true
                    val value = field.get(obj)
                    if (value is List<*> || value is org.docx4j.wml.Text || value?.javaClass?.name?.startsWith("org.docx4j") == true) {
                        replaceVariablesRecursive(value, visited)
                    }
                }
            }
        }
    }

    private fun processContent(contentList: List<Any>?) {
        replaceVariablesRecursive(contentList)
    }

    private fun isHeading(paragraph: P): Boolean {
        val style = paragraph.pPr?.pStyle?.`val` ?: return false
        // Accepts Heading1, Heading2, Heading 1, etc.
        return style.startsWith("Heading") || style.matches(Regex("Heading ?\\d+"))
    }

    private fun getSections(document: WordprocessingMLPackage): List<Section> {
        val sections = mutableListOf<Section>()
        var currentHeading: String? = null
        val currentSection = mutableListOf<Any>()

        document.mainDocumentPart.jaxbElement.body.content.forEach { content ->
            if (content is P) {
                if (isHeading(content)) {
                    if (currentHeading != null) {
                        sections.add(Section(currentHeading!!, currentSection.toList()))
                        currentSection.clear()
                    }
                    currentHeading = TextUtils.getText(content).trim()
                }
            }
            currentSection.add(content)
        }
        if (currentHeading != null) {
            sections.add(Section(currentHeading!!, currentSection.toList()))
        }
        return sections
    }

    private fun mergeStyles(baseDocument: WordprocessingMLPackage, concreteDocument: WordprocessingMLPackage) {
        val baseStyles = baseDocument.mainDocumentPart.styleDefinitionsPart
        val concreteStyles = concreteDocument.mainDocumentPart.styleDefinitionsPart

        if (baseStyles != null && concreteStyles != null) {
            val baseStyleMap = baseStyles.jaxbElement.style.associateBy { it.styleId }
            val concreteStyleMap = concreteStyles.jaxbElement.style.associateBy { it.styleId }

            // Process each style from concrete document
            concreteStyleMap.forEach { (styleId, concreteStyle) ->
                val baseStyle = baseStyleMap[styleId]
                if (baseStyle == null) {
                    // Style doesn't exist in base document, add it
                    baseStyles.jaxbElement.style.add(concreteStyle)
                    logger.info("Added new style: {}", styleId)
                } else {
                    // Style exists, check if it's a list style and update if needed
                    if (concreteStyle.type == STStyleType.LIST) {
                        // For list styles, always use the concrete document's style
                        val index = baseStyles.jaxbElement.style.indexOf(baseStyle)
                        if (index != -1) {
                            baseStyles.jaxbElement.style[index] = concreteStyle
                            logger.info("Updated list style: {}", styleId)
                        }
                    } else if (concreteStyle.type == STStyleType.PARAGRAPH && 
                             concreteStyle.name?.`val`?.startsWith("List") == true) {
                        // For paragraph styles that are part of lists, update them too
                        val index = baseStyles.jaxbElement.style.indexOf(baseStyle)
                        if (index != -1) {
                            baseStyles.jaxbElement.style[index] = concreteStyle
                            logger.info("Updated list paragraph style: {}", styleId)
                        }
                    }
                }
            }
        }
    }

    private fun mergeContent(baseDocument: WordprocessingMLPackage, concreteDocument: WordprocessingMLPackage) {

        // Get sections from both documents
        val baseSections = getSections(baseDocument)
        val concreteSections = getSections(concreteDocument)

        logger.info("Processing {} sections from base document and {} sections from concrete document", 
            baseSections.size, concreteSections.size)

        // Prepare the merged/target content
        val mergedContent = baseDocument.mainDocumentPart.jaxbElement.body.content
        mergedContent.clear()

        val concreteSectionIterator = concreteSections.iterator()
        val concreteSectionsKeySet = concreteSections.map { it.heading }.toSet()

        // Process each section from base document
        baseSections.forEach { baseSection ->
            val baseHeading = baseSection.heading
            if (concreteSectionsKeySet.contains(baseHeading)) {
                // Section exists in both documents
                var concreteSection: Section = Section(baseHeading, emptyList())

                // Insert concrete sections until the matched base section
                while (concreteSectionIterator.hasNext()) {
                    concreteSection = concreteSectionIterator.next()
                    if (concreteSection.heading == baseHeading) {
                        break;
                    }
                    if (!concreteSection.isEmpty) {
                        // Use concrete section if it has content
                        logger.info("Section '{}': Using concrete content", concreteSection.heading)
                        mergedContent.addAll(concreteSection.content)
                    } else {
                        // Skip empty concrete sections
                        logger.info("Section '{}': Skipping empty concrete section", concreteSection.heading)
                    }
                }

                if (!concreteSection.isEmpty) {
                    // Use concrete section if it has content
                    logger.info("Section '{}': Using concrete content", concreteSection.heading)
                    mergedContent.addAll(concreteSection.content)
                } else if (!baseSection.isEmpty) {
                    // Use base section if it has content
                    logger.info("Section '{}': Using base content", baseSection.heading)
                    mergedContent.addAll(baseSection.content)
                } else {
                    // Both sections are empty
                    logger.info("Section '{}': Both sections are empty", baseSection.heading)
                }
            } else if (!baseSection.isEmpty) {
                // Section only exists in base document
                logger.info("Section '{}': Using base content", baseSection.heading)
                mergedContent.addAll(baseSection.content)
            } else {
                // Skip empty base sections
                logger.info("Section '{}': Skipping empty base section", baseSection.heading)
            }
        }

        // Handle any remaining concrete sections
        while (concreteSectionIterator.hasNext()) {
            val concreteSection = concreteSectionIterator.next()
            if (!concreteSection.isEmpty) {
                // Use concrete section if it has content
                logger.info("Section '{}': Using concrete content", concreteSection.heading)
                mergedContent.addAll(concreteSection.content)
            } else {
                // Skip empty concrete sections
                logger.info("Section '{}': Skipping empty concrete section", concreteSection.heading)
            }
        }

        // Process variables in the final document
        processContent(mergedContent)
    }
} 