package com.example.pycharmfilestructuregenerate.dirdoc

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.application.ReadAction

/**
 * Scans the project directory structure and builds a representation of it.
 */
class DirectoryScanner(private val project: Project) {

    /**
     * Represents a node in the directory structure
     */
    data class FileNode(
        val name: String,
        val path: String = "",
        val isPythonFile: Boolean = false,
        val isInitFile: Boolean = false,
        val isPythonPackage: Boolean = false,
        val isHidden: Boolean = false,
        val children: MutableList<FileNode> = mutableListOf()
    )

    /**
     * Filter options for scanning
     */
    data class ScanFilter(
        val includeHidden: Boolean = false,
        val excludePatterns: List<String> = listOf("__pycache__", "*.pyc", ".git", ".idea", "venv"),
        val maxDepth: Int = -1  // -1 means no limit
    )

    /**
     * Scans the project and returns a tree structure of FileNodes
     */
    fun scanProject(filter: ScanFilter = ScanFilter()): List<FileNode> {
        return ReadAction.compute<List<FileNode>, Throwable> {
            val result = mutableListOf<FileNode>()
            val projectRootManager = ProjectRootManager.getInstance(project)

            // Get content roots (top-level directories in the project)
            val contentRoots = projectRootManager.contentRoots

            for (root in contentRoots) {
                val rootNode = scanDirectory(root, filter, 0)
                result.add(rootNode)
            }

            result
        }
    }

    /**
     * Recursively scans a directory and returns a FileNode representing it
     */
    private fun scanDirectory(directory: VirtualFile, filter: ScanFilter, currentDepth: Int): FileNode {
        val children = mutableListOf<FileNode>()
        var hasInitPy = false

        // Check if we've reached the maximum depth
        if (filter.maxDepth > 0 && currentDepth >= filter.maxDepth) {
            return FileNode(
                name = directory.name,
                path = directory.path,
                isPythonPackage = false,
                isHidden = directory.name.startsWith("."),
                children = mutableListOf()
            )
        }

        // Skip directories that match exclusion patterns
        if (shouldExclude(directory.name, filter.excludePatterns)) {
            return FileNode(
                name = directory.name,
                path = directory.path,
                isPythonPackage = false,
                isHidden = directory.name.startsWith("."),
                children = mutableListOf()
            )
        }

        // Skip hidden directories unless explicitly included
        if (!filter.includeHidden && directory.name.startsWith(".")) {
            return FileNode(
                name = directory.name,
                path = directory.path,
                isPythonPackage = false,
                isHidden = true,
                children = mutableListOf()
            )
        }

        // Scan all children of this directory
        for (child in directory.children) {
            if (child.isDirectory) {
                // Skip excluded directories
                if (shouldExclude(child.name, filter.excludePatterns)) {
                    continue
                }

                // Skip hidden directories unless explicitly included
                if (!filter.includeHidden && child.name.startsWith(".")) {
                    continue
                }

                // Recursively scan subdirectories
                children.add(scanDirectory(child, filter, currentDepth + 1))
            } else if (!child.isDirectory) {
                // Skip excluded files
                if (shouldExclude(child.name, filter.excludePatterns)) {
                    continue
                }

                // Skip hidden files unless explicitly included
                if (!filter.includeHidden && child.name.startsWith(".")) {
                    continue
                }

                // Check for Python files and __init__.py
                val isPythonFile = child.extension == "py"
                val isInitFile = child.name == "__init__.py"

                if (isInitFile) {
                    hasInitPy = true
                }

                children.add(
                    FileNode(
                        name = child.name,
                        path = child.path,
                        isPythonFile = isPythonFile,
                        isInitFile = isInitFile,
                        isHidden = child.name.startsWith(".")
                    )
                )
            }
        }

        // Sort children: directories first, then files
        children.sortWith(compareBy({ !it.children.any() }, { it.name }))

        return FileNode(
            name = directory.name,
            path = directory.path,
            isPythonPackage = hasInitPy,
            isHidden = directory.name.startsWith("."),
            children = children
        )
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
