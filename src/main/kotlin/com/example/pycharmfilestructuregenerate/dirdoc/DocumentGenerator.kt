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
        val excludePatterns: List<String> = listOf("__pycache__", "*.pyc", ".git", ".idea", "venv"),
        val treeStyle: TreeStyle = TreeStyle.SIMPLE // Default to current style
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
        for (i in nodes.indices) {
            appendNode(builder, nodes[i], 0, settings, i == nodes.size - 1, mutableListOf())
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

        // Add code block for tree (helps with formatting in Markdown)
        // Adding an explicit newline after the code fence
        builder.append("```\n")

        // Generate the tree
        for (i in nodes.indices) {
            appendNodeMarkdown(builder, nodes[i], 0, settings, i == nodes.size - 1, mutableListOf())
        }

        builder.append("```")

            return builder.toString()
    }

    private fun appendNode(
        builder: StringBuilder,
        node: DirectoryScanner.FileNode,
        depth: Int,
        settings: GenerationSettings,
        isLastChild: Boolean = false,
        parentConnections: MutableList<Boolean> = mutableListOf()
    ) {
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

        // Add root node name with trailing slash if using box or ascii extended styles
        val isRoot = depth == 0 && (settings.treeStyle == TreeStyle.BOX_DRAWING || settings.treeStyle == TreeStyle.ASCII_EXTENDED)
        if (isRoot) {
            builder.append("${node.name}/\n")
        } else {
            when (settings.treeStyle) {
                TreeStyle.SIMPLE -> {
                    val indent = "    ".repeat(depth)
                    val prefix = if (node.children.isNotEmpty() || node.isPythonPackage) "+ " else "- "
                    builder.append("$indent$prefix${node.name}")
                }
                TreeStyle.BOX_DRAWING -> {
                    val indentBuilder = StringBuilder()

                    // Build the indentation with appropriate vertical lines
                    for (i in 0 until depth - 1) {
                        if (i < parentConnections.size && parentConnections[i]) {
                            indentBuilder.append("│   ")
                        } else {
                            indentBuilder.append("    ")
                        }
                    }

                    // Add the appropriate connector for this level
                    val connector = if (isLastChild) "└── " else "├── "

                    builder.append("${indentBuilder}$connector${node.name}")
                }
                TreeStyle.ASCII_EXTENDED -> {
                    val indentBuilder = StringBuilder()

                    // Build the indentation with appropriate vertical lines
                    for (i in 0 until depth - 1) {
                        if (i < parentConnections.size && parentConnections[i]) {
                            indentBuilder.append("|    ")
                        } else {
                            indentBuilder.append("     ")
                        }
                    }

                    // Add the appropriate connector for this level
                    val connector = if (isLastChild) "\\--- " else "+--- "

                    builder.append("${indentBuilder}$connector${node.name}")
                }
            }

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
        }

        // Recursively append children
        if (node.children.isNotEmpty()) {
            val lastIndex = node.children.size - 1

            // Create a new copy of parent connections for children
            val newParentConnections = parentConnections.toMutableList()
            if (depth > 0) {
                // Add whether current node has more siblings coming after it
                newParentConnections.add(!isLastChild)
            }

            node.children.forEachIndexed { index, child ->
                appendNode(builder, child, depth + 1, settings, index == lastIndex, newParentConnections)
            }
        }
    }

    private fun appendNodeMarkdown(
        builder: StringBuilder,
        node: DirectoryScanner.FileNode,
        depth: Int,
        settings: GenerationSettings,
        isLastChild: Boolean = false,
        parentConnections: MutableList<Boolean> = mutableListOf()
    ) {
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

        // Add root node name with trailing slash if using box or ascii extended styles
        val isRoot = depth == 0 && (settings.treeStyle == TreeStyle.BOX_DRAWING || settings.treeStyle == TreeStyle.ASCII_EXTENDED)
        if (isRoot) {
            builder.append("${node.name}/\n")
        } else {
            when (settings.treeStyle) {
                TreeStyle.SIMPLE -> {
                    val indent = "    ".repeat(depth)
                    val prefix = if (node.children.isNotEmpty() || node.isPythonPackage) "+ " else "- "

                    if (node.isPythonPackage || node.children.isNotEmpty()) {
                        builder.append("$indent$prefix**${node.name}**")
                    } else if (node.isPythonFile) {
                        if (node.isInitFile) {
                            builder.append("$indent$prefix*${node.name}*")
                        } else {
                            builder.append("$indent$prefix`${node.name}`")
                        }
                    } else {
                        builder.append("$indent$prefix${node.name}")
                    }
                }
                TreeStyle.BOX_DRAWING -> {
                    val indentBuilder = StringBuilder()

                    // Build the indentation with appropriate vertical lines
                    for (i in 0 until depth - 1) {
                        if (i < parentConnections.size && parentConnections[i]) {
                            indentBuilder.append("│   ")
                        } else {
                            indentBuilder.append("    ")
                        }
                    }

                    // Add the appropriate connector for this level
                    val connector = if (isLastChild) "└── " else "├── "

                    builder.append("${indentBuilder}$connector${node.name}")
                }
                TreeStyle.ASCII_EXTENDED -> {
                    val indentBuilder = StringBuilder()

                    // Build the indentation with appropriate vertical lines
                    for (i in 0 until depth - 1) {
                        if (i < parentConnections.size && parentConnections[i]) {
                            indentBuilder.append("|    ")
                        } else {
                            indentBuilder.append("     ")
                        }
                    }

                    // Add the appropriate connector for this level
                    val connector = if (isLastChild) "\\--- " else "+--- "

                    builder.append("${indentBuilder}$connector${node.name}")
                }
            }

            // Add indicators for Python files and packages in simple mode
            if (settings.treeStyle == TreeStyle.SIMPLE) {
                if (node.isPythonPackage) {
                    builder.append(" [Python Package]")
                } else if (node.isPythonFile) {
                    if (node.isInitFile) {
                        builder.append(" [Module Initializer]")
                    } else {
                        builder.append(" [Python Module]")
                    }
                }
            } else {
                // For box drawing and ASCII extended, add indicators without special formatting
                if (node.isPythonPackage) {
                    builder.append(" [Python Package]")
                } else if (node.isPythonFile) {
                    if (node.isInitFile) {
                        builder.append(" [Module Initializer]")
                    } else {
                        builder.append(" [Python Module]")
                    }
                }
            }

            builder.append("\n")
        }

        // Recursively append children
        if (node.children.isNotEmpty()) {
            val lastIndex = node.children.size - 1

            // Create a new copy of parent connections for children
            val newParentConnections = parentConnections.toMutableList()
            if (depth > 0) {
                // Add whether current node has more siblings coming after it
                newParentConnections.add(!isLastChild)
            }

            node.children.forEachIndexed { index, child ->
                appendNodeMarkdown(builder, child, depth + 1, settings, index == lastIndex, newParentConnections)
            }
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
