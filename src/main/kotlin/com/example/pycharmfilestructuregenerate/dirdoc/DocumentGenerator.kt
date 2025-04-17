package com.example.pycharmfilestructuregenerate.dirdoc

/**
 * Generates text or markdown documentation from the directory structure
 */
class DocumentGenerator {

    /**
     * Generate a text representation of the directory structure
     */
    fun generateText(nodes: List<DirectoryScanner.FileNode>): String {
        val builder = StringBuilder()
        builder.append("Project Directory Structure\n")
        builder.append("==========================\n\n")

        for (node in nodes) {
            appendNode(builder, node, 0)
        }

        return builder.toString()
    }

    /**
     * Generate a markdown representation of the directory structure
     */
    fun generateMarkdown(nodes: List<DirectoryScanner.FileNode>): String {
        val builder = StringBuilder()
        builder.append("# Project Directory Structure\n\n")

        for (node in nodes) {
            appendNodeMarkdown(builder, node, 0)
        }

        return builder.toString()
    }

    private fun appendNode(builder: StringBuilder, node: DirectoryScanner.FileNode, depth: Int) {
        val indent = "    ".repeat(depth)
        val prefix = if (node.children.isEmpty()) "- " else "+ "

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
            appendNode(builder, child, depth + 1)
        }
    }

    private fun appendNodeMarkdown(builder: StringBuilder, node: DirectoryScanner.FileNode, depth: Int) {
        val indent = "    ".repeat(depth)
        val prefix = if (node.children.isEmpty()) "- " else "- "

        builder.append("$indent$prefix**${node.name}**")

        // Add indicators for Python files and packages
        if (node.isPythonPackage) {
            builder.append(" *[Python Package]*")
        } else if (node.isPythonFile) {
            if (node.isInitFile) {
                builder.append(" *[Module Initializer]*")
            } else {
                builder.append(" *[Python Module]*")
            }
        }

        builder.append("\n")

        // Recursively append children
        for (child in node.children) {
            appendNodeMarkdown(builder, child, depth + 1)
        }
    }
}
