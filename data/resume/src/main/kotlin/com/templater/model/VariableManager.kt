package com.templater.model

import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.docx4j.TextUtils
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.docx4j.wml.P

@Component
class VariableManager {
    private val variables: MutableMap<String, String> = mutableMapOf()

    init {
        // Format: 5 October, 2025
        val formatter = DateTimeFormatter.ofPattern("d MMMM, yyyy", Locale.ENGLISH)
        val today = LocalDate.now()
        variables["current_date"] = today.format(formatter)
        // Add more variables as needed
    }

    fun getVariableValue(name: String): String {
        return variables[name] ?: "<data_$name>"
    }

    fun setVariableValue(name: String, value: String) {
        variables[name] = value
    }
} 