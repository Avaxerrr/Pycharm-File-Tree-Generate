package com.example.pycharmfilestructuregenerate.dirdoc

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File
import java.nio.file.Path

/**
 * Action that generates the directory structure documentation
 */
class GenerateAction : AnAction() {
    private val logger = Logger.getInstance(GenerateAction::class.java)

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        try {
            // Get saved settings
            val settings = SettingsState.getInstance()

            // Show configuration dialog
            val dialog = GenerationConfigDialog(project)

            // Pre-populate dialog with saved settings
            dialog.outputPath = settings.outputPath.ifEmpty { project.basePath ?: "" }
            dialog.outputFormat = if (settings.outputFileName.endsWith(".md")) "md" else "txt"
            dialog.includeHidden = settings.includeHidden
            dialog.pythonOnly = settings.pythonFilesOnly
            dialog.includeTimestamp = settings.includeTimestamp
            dialog.includeFileCount = settings.includeFileCount
            dialog.excludePatterns = settings.excludePatterns
            dialog.maxDepth = settings.maxDepth

            if (!dialog.showAndGet()) {
                return // User cancelled
            }

            // Save settings for next time
            settings.outputPath = dialog.outputPath
            settings.outputFileName = "directory-structure." + dialog.outputFormat
            settings.includeHidden = dialog.includeHidden
            settings.pythonFilesOnly = dialog.pythonOnly
            settings.includeTimestamp = dialog.includeTimestamp
            settings.includeFileCount = dialog.includeFileCount
            settings.excludePatterns = dialog.excludePatterns
            settings.maxDepth = dialog.maxDepth

            // Generate directory structure documentation
            generateDocumentation(project, settings)
        } catch (ex: Exception) {
            logger.error("Error generating directory structure", ex)
            NotificationHelper.showError(
                project,
                "An unexpected error occurred: ${ex.message}. See logs for details."
            )
        }
    }

    private fun generateDocumentation(project: Project, settings: SettingsState) {
        // Scan the project directory
        val scanner = DirectoryScanner(project)
        val scanFilter = DirectoryScanner.ScanFilter(
            includeHidden = settings.includeHidden,
            excludePatterns = settings.excludePatterns.split(",").map { it.trim() },
            maxDepth = settings.maxDepth
        )

        val structure = scanner.scanProject(scanFilter)

        // Generate the documentation
        val generator = DocumentGenerator()
        val generationSettings = DocumentGenerator.GenerationSettings(
            includeHidden = settings.includeHidden,
            includePythonFilesOnly = settings.pythonFilesOnly,
            maxDepth = settings.maxDepth,
            includeTimestamp = settings.includeTimestamp,
            includeFileCount = settings.includeFileCount,
            excludePatterns = settings.excludePatterns.split(",").map { it.trim() }
        )

        // Determine file path
        val fileName = settings.outputFileName
        val outputPath = settings.outputPath
        val fullPath = getFullOutputPath(outputPath, fileName)

        try {
            // Ensure the directory exists
            val outputFile = File(fullPath)
            outputFile.parentFile?.mkdirs()

            // Generate content based on selected format
            val content = when {
                fileName.endsWith(".md") -> generator.generateMarkdown(structure, generationSettings)
                else -> generator.generateText(structure, generationSettings)
            }

            // Write to file
            outputFile.writeText(content)

            // Refresh the virtual file system to see the new file
            val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(fullPath)

            if (virtualFile != null) {
                // Open the file in the editor
                FileEditorManager.getInstance(project).openFile(virtualFile, true)

                NotificationHelper.showInfo(
                    project,
                    "Directory structure documentation saved to $fullPath and opened in editor."
                )
            } else {
                NotificationHelper.showInfo(
                    project,
                    "Directory structure documentation saved to $fullPath."
                )
            }
        } catch (ex: Exception) {
            logger.error("Failed to write directory structure file", ex)
            NotificationHelper.showError(
                project,
                "Failed to write output file: ${ex.message}"
            )
        }
    }

    private fun getFullOutputPath(outputPath: String, fileName: String): String {
        return if (outputPath.endsWith(fileName)) {
            outputPath
        } else {
            val path = Path.of(outputPath)
            path.resolve(fileName).toString()
        }
    }

    override fun update(e: AnActionEvent) {
        // Only enable this action if we have an open project
        e.presentation.isEnabled = e.project != null
    }
}
