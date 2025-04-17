package com.example.pycharmfilestructuregenerate.dirdoc

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Generates text or markdown documentation from the directory structure
 */
class DocumentGenerator {

    /**
     * Settings for document generation
     */
    data class GenerationSettings(
        val includeHidden: Boolean = false,
        val includePythonFilesOnly: Boolean = false,
        val maxDepth: Int = -1,  // -1 means no limit
        val includeTimestamp: Boolean = true,
        val includeFileCount: Boolean = true,
        val includeEmptyDirs: Boolean = true,
        val excludePatterns: List<String> = listOf("__pycache__", "*.pyc", ".git", ".idea", "venv")
    )

    /**
     * Generate a text representation of the directory structure
     */
    fun generateText(nodes: List<DirectoryScanner.FileNode>, settings: GenerationSettings = GenerationSettings()): String {
        val builder = StringBuilder()

        // Add header
        builder.append("PROJECT DIRECTORY STRUCTURE\n")
        builder.append("==========================\n\n")

        // Add timestamp if requested
        if (settings.includeTimestamp) {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            builder.append("Generated: $timestamp\n\n")
        }

        // Add file summary if requested
        if (settings.includeFileCount) {
            val counts = countFiles(nodes)
            builder.append("Summary: ${counts.first} directories, ${counts.second} files\n\n")
        }

        // Generate the tree
        for (node in nodes) {
            appendNode(builder, node, 0, settings)
        }

        return builder.toString()
    }

    /**
     * Generate a markdown representation of the directory structure
     */
    fun generateMarkdown(nodes: List<DirectoryScanner.FileNode>, settings: GenerationSettings = GenerationSettings()): String {
        val builder = StringBuilder()

        // Add header
        builder.append("# Project Directory Structure\n\n")

        // Add timestamp if requested
        if (settings.includeTimestamp) {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            builder.append("*Generated: $timestamp*\n\n")
        }

        // Add file summary if requested
        if (settings.includeFileCount) {
            val counts = countFiles(nodes)
            builder.append("*Summary: ${counts.first} directories, ${counts.second} files*\n\n")
        }

        // Generate the tree
        for (node in nodes) {
            appendNodeMarkdown(builder, node, 0, settings)
        }

        return builder.toString()
    }

    private fun appendNode(builder: StringBuilder, node: DirectoryScanner.FileNode, depth: Int, settings: GenerationSettings) {
        // Skip excluded patterns
        if (shouldExclude(node.name, settings.excludePatterns)) {
            return
        }

        // Skip hidden files unless explicitly included
        if (!settings.includeHidden && node.name.startsWith(".")) {
            return
        }

        // Skip non-Python files if Python only is selected
        if (settings.includePythonFilesOnly && !node.isPythonFile && !node.isPythonPackage && node.children.isEmpty()) {
            return
        }

        // Respect max depth setting
        if (settings.maxDepth > 0 && depth > settings.maxDepth) {
            return
        }

        // Skip empty directories if requested
        if (!settings.includeEmptyDirs && node.children.isEmpty() && !node.isPythonFile) {
            return
        }

        val indent = "    ".repeat(depth)
        val prefix = if (node.children.isEmpty() && !node.isPythonPackage) "- " else "+ "

        builder.append("$indent$prefix${node.name}")

        // Add indicators for Python files and packages
        if (node.isPythonPackage) {
            builder.append(" [Python Package]")
        } else if (node.isPythonFile) {
            if (node.isInitFile) {
                builder.append(" [Module Initializer]")
            } else {
                builder.append(" [Python Module]")
            }
        }

        builder.append("\n")

        // Recursively append children
        for (child in node.children) {
            appendNode(builder, child, depth + 1, settings)
        }
    }

    private fun appendNodeMarkdown(builder: StringBuilder, node: DirectoryScanner.FileNode, depth: Int, settings: GenerationSettings) {
        // Skip excluded patterns
        if (shouldExclude(node.name, settings.excludePatterns)) {
            return
        }

        // Skip hidden files unless explicitly included
        if (!settings.includeHidden && node.name.startsWith(".")) {
            return
        }

        // Skip non-Python files if Python only is selected
        if (settings.includePythonFilesOnly && !node.isPythonFile && !node.isPythonPackage && node.children.isEmpty()) {
            return
        }

        // Respect max depth setting
        if (settings.maxDepth > 0 && depth > settings.maxDepth) {
            return
        }

        // Skip empty directories if requested
        if (!settings.includeEmptyDirs && node.children.isEmpty() && !node.isPythonFile) {
            return
        }

        val indent = "    ".repeat(depth)
        val prefix = if (node.children.isEmpty() && !node.isPythonPackage) "- " else "- "

        if (node.isPythonPackage) {
            builder.append("$indent$prefix📦 **${node.name}**")
        } else if (node.isPythonFile) {
            if (node.isInitFile) {
                builder.append("$indent$prefix🔧 *${node.name}*")
            } else {
                builder.append("$indent$prefix📄 `${node.name}`")
            }
        } else if (node.children.isNotEmpty()) {
            builder.append("$indent$prefix📁 **${node.name}**")
        } else {
            builder.append("$indent$prefix📄 ${node.name}")
        }

        builder.append("\n")

        // Recursively append children
        for (child in node.children) {
            appendNodeMarkdown(builder, child, depth + 1, settings)
        }
    }

    private fun countFiles(nodes: List<DirectoryScanner.FileNode>): Pair<Int, Int> {
        var dirCount = 0
        var fileCount = 0

        for (node in nodes) {
            if (node.children.isNotEmpty()) {
                dirCount++
                val (dirs, files) = countFiles(node.children)
                dirCount += dirs
                fileCount += files
            } else {
                fileCount++
            }
        }

        return Pair(dirCount, fileCount)
    }

    private fun shouldExclude(name: String, patterns: List<String>): Boolean {
        for (pattern in patterns) {
            if (pattern.contains("*")) {
                val regex = pattern.replace(".", "\\.").replace("*", ".*")
                if (name.matches(Regex(regex))) {
                    return true
                }
            } else if (name == pattern) {
                return true
            }
        }
        return false
    }
}
