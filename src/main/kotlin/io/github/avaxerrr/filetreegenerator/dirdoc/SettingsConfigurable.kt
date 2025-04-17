// version 1.0

package io.github.avaxerrr.filetreegenerator.dirdoc

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

/**
 * Integrates the plugin settings into the IDE's settings dialog
 */
class SettingsConfigurable : Configurable {
    private var settingsComponent: SettingsComponent? = null

    override fun getDisplayName(): String = "File Tree Generator"

    override fun getPreferredFocusedComponent(): JComponent? {
        return settingsComponent?.getPreferredFocusedComponent()
    }

    override fun createComponent(): JComponent? {
        settingsComponent = SettingsComponent()
        return settingsComponent!!.getPanel()
    }

    override fun isModified(): Boolean {
        val settings = SettingsState.Companion.getInstance()
        val component = settingsComponent ?: return false

        return component.isAutoUpdateEnabled != settings.autoUpdate ||
                component.outputPath != settings.outputPath ||
                component.outputFileName != settings.outputFileName ||
                component.includeHidden != settings.includeHidden ||
                component.pythonFilesOnly != settings.pythonFilesOnly ||
                component.includeTimestamp != settings.includeTimestamp ||
                component.includeFileCount != settings.includeFileCount ||
                component.excludePatterns != settings.excludePatterns ||
                component.maxDepth != settings.maxDepth ||
                component.treeStyle != settings.treeStyle
    }

    override fun apply() {
        val settings = SettingsState.Companion.getInstance()
        val component = settingsComponent ?: return

        settings.autoUpdate = component.isAutoUpdateEnabled
        settings.outputPath = component.outputPath
        settings.outputFileName = component.outputFileName
        settings.includeHidden = component.includeHidden
        settings.pythonFilesOnly = component.pythonFilesOnly
        settings.includeTimestamp = component.includeTimestamp
        settings.includeFileCount = component.includeFileCount
        settings.excludePatterns = component.excludePatterns
        settings.maxDepth = component.maxDepth
        settings.treeStyle = component.treeStyle
    }

    override fun reset() {
        val settings = SettingsState.Companion.getInstance()
        val component = settingsComponent ?: return

        component.setAutoUpdateEnabled(settings.autoUpdate)
        component.setOutputPath(settings.outputPath)
        component.setOutputFileName(settings.outputFileName)
        component.setIncludeHidden(settings.includeHidden)
        component.setPythonFilesOnly(settings.pythonFilesOnly)
        component.setIncludeTimestamp(settings.includeTimestamp)
        component.setIncludeFileCount(settings.includeFileCount)
        component.setExcludePatterns(settings.excludePatterns)
        component.setMaxDepth(settings.maxDepth)
        component.setTreeStyle(settings.treeStyle)
    }

    override fun disposeUIResources() {
        settingsComponent = null
    }
}
