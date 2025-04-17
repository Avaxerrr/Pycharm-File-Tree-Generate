package com.example.pycharmfilestructuregenerate.dirdoc

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File

/**
 * Action that generates the directory structure documentation
 */
class GenerateAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        // Show configuration dialog
        val dialog = GenerationConfigDialog(project)
        if (!dialog.showAndGet()) {
            return // User cancelled
        }

        // Get settings from dialog
        val outputPath = dialog.outputPath
        val outputFormat = dialog.outputFormat
        val settings = dialog.getGenerationSettings()

        // Scan the project directory
        val scanner = DirectoryScanner(project)
        val scanFilter = DirectoryScanner.ScanFilter(
            includeHidden = settings.includeHidden,
            excludePatterns = settings.excludePatterns,
            maxDepth = settings.maxDepth
        )

        val structure = scanner.scanProject(scanFilter)

        // Generate the documentation
        val generator = DocumentGenerator()

        // Determine file path
        val filePath = if (outputPath.isNotEmpty()) {
            outputPath
        } else {
            project.basePath ?: ""
        }

        val fileName = "directory-structure.$outputFormat"
        val fullPath = if (filePath.endsWith(fileName)) {
            filePath
        } else {
            "$filePath${File.separator}$fileName"
        }

        try {
            // Generate content based on selected format
            val content = when (outputFormat) {
                "md" -> generator.generateMarkdown(structure, settings)
                else -> generator.generateText(structure, settings)
            }

            // Write to file
            File(fullPath).writeText(content)

            // Refresh the virtual file system to see the new file
            val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(fullPath)

            if (virtualFile != null) {
                // Open the file in the editor
                FileEditorManager.getInstance(project).openFile(virtualFile, true)

                Messages.showInfoMessage(
                    "Directory structure documentation saved to $fullPath and opened in editor.",
                    "Directory Structure Generated"
                )
            } else {
                Messages.showInfoMessage(
                    "Directory structure documentation saved to $fullPath.",
                    "Directory Structure Generated"
                )
            }
        } catch (ex: Exception) {
            Messages.showErrorDialog(
                "Failed to write output file: ${ex.message}",
                "Error"
            )
        }
    }

    override fun update(e: AnActionEvent) {
        // Only enable this action if we have an open project
        e.presentation.isEnabled = e.project != null
    }
}
