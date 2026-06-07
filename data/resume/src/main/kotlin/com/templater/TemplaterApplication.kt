package com.templater

import com.templater.service.DocumentProcessor
import com.templater.service.DocxSectionUtils
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
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
            var argsCopy = args;
            if (argsCopy.isEmpty()) {
                logger.warn("No command specified. Available commands: merge, extract")
                argsCopy = arrayOf("merge", "./docs/base.template.docx", "./docs/sde.template.docx", "./docs/resume-sde.docx")
                logger.warn("Using default argsCopy : {}", argsCopy.contentToString())
            } else {

            }

            when (argsCopy[0]) {
                "merge" -> {
                    if (argsCopy.size != 4) {
                        logger.error("Invalid number of arguments for merge. Expected: merge <base-template> <concrete-template> <output-file>")
                        exitProcess(1)
                    }

                    try {
                        logger.info("Starting document merge with arguments:")
                        logger.info("  Base template: {}", argsCopy[1])
                        logger.info("  Concrete template: {}", argsCopy[2])
                        logger.info("  Output file: {}", argsCopy[3])

                        documentProcessor.mergeDocuments(argsCopy[1], argsCopy[2], argsCopy[3])
                        exitProcess(0)
                    } catch (e: Exception) {
                        logger.error("Document merge failed: {}", e.message, e)
                        exitProcess(1)
                    }
                }
                "extract" -> {
                    if (argsCopy.size != 3) {
                        logger.error("Invalid number of arguments for extract. Expected: extract <docx-file> <heading>")
                        exitProcess(1)
                    }
                    
                    val docxPath = argsCopy[1]
                    val heading = argsCopy[2]

                    val docx = WordprocessingMLPackage.load(java.io.File(docxPath))
                    val sectionContent = DocxSectionUtils.getSectionContentByHeading(docx, heading)

                    if (sectionContent.isEmpty()) {
                        System.err.println("No content found for heading: $heading")
                        exitProcess(1)
                    } else {
                        val text = sectionContent
                                .filterIsInstance<P>()
                                .joinToString("    \n") { DocxSectionUtils.paragraphToMarkdown(it) }
                                .trim()
                        println(text)
                        exitProcess(0)
                    }
                }
                else -> {
                    logger.error("Unknown command: {}. Available commands: merge, extract", argsCopy[0])
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