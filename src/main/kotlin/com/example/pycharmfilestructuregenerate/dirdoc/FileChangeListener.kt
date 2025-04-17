package com.example.pycharmfilestructuregenerate.dirdoc

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.TimeUnit
import java.io.File

class FileChangeListener : BulkFileListener {
    private var lastUpdateTime = 0L
    private val updateDelayMs = 2000 // Debounce time in milliseconds

    override fun after(events: List<VFileEvent>) {
        // Skip if auto-update is disabled
        val settings = ApplicationManager.getApplication().getService(SettingsState::class.java) ?: return
        if (!settings.autoUpdate) return

        // Process only relevant events
        val relevantEvents = events.filter { event ->
            // Skip events for the directory structure file itself to avoid loops
            val path = event.path
            val fileName = settings.outputFileName
            !path.endsWith(fileName) &&
                    // Only process Python files or directories if configured this way
                    (!settings.pythonFilesOnly || path.endsWith(".py") || event.file?.isDirectory == true)
        }

        if (relevantEvents.isEmpty()) return

        // Debounce updates to avoid excessive regeneration
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTime < updateDelayMs) {
            return
        }
        lastUpdateTime = currentTime

        // Determine which project(s) to update
        val affectedProjects = mutableSetOf<Project>()
        for (event in relevantEvents) {
            val eventPath = event.path
            for (project in ProjectManager.getInstance().openProjects) {
                val basePath = project.basePath ?: continue
                if (eventPath.startsWith(basePath)) {
                    affectedProjects.add(project)
                    break
                }
            }
        }

        // Schedule delayed update to allow for batching of changes
        if (affectedProjects.isNotEmpty()) {
            AppExecutorUtil.getAppScheduledExecutorService().schedule({
                ApplicationManager.getApplication().invokeLater {
                    for (project in affectedProjects) {
                        updateDirectoryStructure(project, settings)
                    }
                }
            }, updateDelayMs.toLong(), TimeUnit.MILLISECONDS)
        }
    }

    private fun updateDirectoryStructure(project: Project, settings: SettingsState) {
        try {
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
            val outputPath = if (settings.outputPath.isNotEmpty()) {
                settings.outputPath
            } else {
                project.basePath ?: ""
            }

            val fileName = settings.outputFileName
            val fullPath = if (outputPath.endsWith(fileName)) {
                outputPath
            } else {
                "$outputPath${File.separator}$fileName"
            }

            // Generate content based on selected format
            val content = when {
                fileName.endsWith(".md") -> generator.generateMarkdown(structure, generationSettings)
                else -> generator.generateText(structure, generationSettings)
            }

            // Write to file
            File(fullPath).writeText(content)

            // Refresh the virtual file system to see the updated file
            LocalFileSystem.getInstance().refreshAndFindFileByPath(fullPath)

        } catch (ex: Exception) {
            // Log error but don't show UI since this is automatic
            ex.printStackTrace()
        }
    }
}
