package com.example.pycharmfilestructuregenerate.dirdoc

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.roots.ProjectRootManager

/**
 * Scans the project directory structure and builds a representation of it.
 */
class DirectoryScanner(private val project: Project) {

    /**
     * Represents a node in the directory structure
     */
    data class FileNode(
        val name: String,
        val isPythonFile: Boolean = false,
        val isInitFile: Boolean = false,
        val isPythonPackage: Boolean = false,
        val children: MutableList<FileNode> = mutableListOf()
    )

    /**
     * Scans the project and returns a tree structure of FileNodes
     */
    fun scanProject(): List<FileNode> {
        val result = mutableListOf<FileNode>()
        val projectRootManager = ProjectRootManager.getInstance(project)

        // Get content roots (top-level directories in the project)
        val contentRoots = projectRootManager.contentRoots

        for (root in contentRoots) {
            val rootNode = scanDirectory(root)
            result.add(rootNode)
        }

        return result
    }

    /**
     * Recursively scans a directory and returns a FileNode representing it
     */
    private fun scanDirectory(directory: VirtualFile): FileNode {
        val children = mutableListOf<FileNode>()
        var hasInitPy = false

        // Scan all children of this directory
        for (child in directory.children) {
            if (child.isDirectory) {
                // Recursively scan subdirectories
                children.add(scanDirectory(child))
            } else if (!child.isDirectory) {
                // Check for Python files and __init__.py
                val isPythonFile = child.extension == "py"
                val isInitFile = child.name == "__init__.py"

                if (isInitFile) {
                    hasInitPy = true
                }

                if (isPythonFile) {
                    children.add(
                        FileNode(
                            name = child.name,
                            isPythonFile = true,
                            isInitFile = isInitFile
                        )
                    )
                }
            }
        }

        // Sort children: directories first, then files
        children.sortWith(compareBy({ !it.children.any() }, { it.name }))

        return FileNode(
            name = directory.name,
            isPythonPackage = hasInitPy,
            children = children
        )
    }
}
