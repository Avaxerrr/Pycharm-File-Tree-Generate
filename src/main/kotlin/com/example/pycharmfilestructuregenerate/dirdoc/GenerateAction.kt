package com.example.pycharmfilestructuregenerate.dirdoc

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import java.io.File
import kotlin.compareTo

/**
 * Action that generates the directory structure documentation
 */
class GenerateAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        // Scan the project directory
        val scanner = DirectoryScanner(project)
        val structure = scanner.scanProject()

        // Generate the documentation
        val generator = DocumentGenerator()
        val textContent = generator.generateText(structure)
        val markdownContent = generator.generateMarkdown(structure)

        // For now, just show a preview of the text content
        // In Phase 3, we'll save this to a file based on user settings
        Messages.showInfoMessage(
            "Directory structure scanned successfully! Preview:\n\n" +
                    textContent.take(500) + (if (textContent.length > 500) "..." else ""),
            "Directory Structure Scan"
        )

        // For demonstration, let's write the files to the project root
        // Later we'll make this configurable
        try {
            val basePath = project.basePath
            if (basePath != null) {
                File("$basePath/directory-structure.txt").writeText(textContent)
                File("$basePath/directory-structure.md").writeText(markdownContent)
                Messages.showInfoMessage(
                    "Directory structure documentation files created at project root.",
                    "Files Created"
                )
            }
        } catch (ex: Exception) {
            Messages.showErrorDialog(
                "Failed to write output files: ${ex.message}",
                "Error"
            )
        }
    }

    override fun update(e: AnActionEvent) {
        // Only enable this action if we have an open project
        e.presentation.isEnabled = e.project != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }
}
