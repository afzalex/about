package com.templater

import com.templater.service.DocumentProcessor
import com.templater.service.DocxSectionUtils
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.docx4j.TextUtils
import org.docx4j.wml.P
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import kotlin.system.exitProcess

@SpringBootApplication
class TemplaterApplication {
    private val logger = LoggerFactory.getLogger(TemplaterApplication::class.java)

    @Bean
    fun commandLineRunner(documentProcessor: DocumentProcessor): CommandLineRunner {
        return CommandLineRunner { args ->
            if (args.isEmpty()) {
                logger.error("No command specified. Available commands: merge, extract-section")
                exitProcess(1)
            }

            when (args[0]) {
                "merge" -> {
                    if (args.size != 4) {
                        logger.error("Invalid number of arguments for merge. Expected: merge <base-template> <concrete-template> <output-file>")
                        exitProcess(1)
                    }

                    try {
                        logger.info("Starting document merge with arguments:")
                        logger.info("  Base template: {}", args[1])
                        logger.info("  Concrete template: {}", args[2])
                        logger.info("  Output file: {}", args[3])

                        documentProcessor.processDocument(args[1], args[2], args[3])
                        exitProcess(0)
                    } catch (e: Exception) {
                        logger.error("Document merge failed: {}", e.message, e)
                        exitProcess(1)
                    }
                }
                "extract-section" -> {
                    if (args.size != 3) {
                        logger.error("Invalid number of arguments for extract-section. Expected: extract-section <docx-file> <heading>")
                        exitProcess(1)
                    }
                    
                    val docxPath = args[1]
                    val heading = args[2]

                    val docx = WordprocessingMLPackage.load(java.io.File(docxPath))
                    val sectionContent = DocxSectionUtils.getSectionContentByHeading(docx, heading)

                    if (sectionContent.isEmpty()) {
                        System.err.println("No content found for heading: $heading")
                        exitProcess(1)
                    } else {
                        val text = sectionContent
                            .filterIsInstance<P>()
                            .joinToString("\n") { TextUtils.getText(it).trim() }
                            .trim()
                        println(text)
                        exitProcess(0)
                    }
                }
                else -> {
                    logger.error("Unknown command: {}. Available commands: merge, extract-section", args[0])
                    exitProcess(1)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<TemplaterApplication>(*args)
        }
    }
} 