package com.example.pycharmfilestructuregenerate.dirdoc

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.Insets
import javax.swing.*
import javax.swing.border.EmptyBorder

class GenerationConfigDialog(project: Project) : DialogWrapper(project) {

    private val txtOutputPath = JBTextField(project.basePath ?: "", 30)
    private val cmbOutputFormat = ComboBox(arrayOf("Text (.txt)", "Markdown (.md)"))
    private val chkIncludeHidden = JBCheckBox("Include hidden files", false)
    private val chkPythonOnly = JBCheckBox("Show Python files only", true)
    private val chkIncludeTimestamp = JBCheckBox("Include timestamp", true)
    private val chkIncludeFileCount = JBCheckBox("Include file counts", true)
    private val txtExcludePatterns = JBTextField("__pycache__, *.pyc, .git, .idea, venv", 30)
    private val txtMaxDepth = JBTextField("-1", 5)

    var outputPath: String = ""
        set(value) {
            field = value
            txtOutputPath.text = value
        }

    var outputFormat: String = "txt"
        set(value) {
            field = value
            cmbOutputFormat.selectedIndex = if (value == "md") 1 else 0
        }

    var includeHidden: Boolean = false
        set(value) {
            field = value
            chkIncludeHidden.isSelected = value
        }

    var pythonOnly: Boolean = true
        set(value) {
            field = value
            chkPythonOnly.isSelected = value
        }

    var includeTimestamp: Boolean = true
        set(value) {
            field = value
            chkIncludeTimestamp.isSelected = value
        }

    var includeFileCount: Boolean = true
        set(value) {
            field = value
            chkIncludeFileCount.isSelected = value
        }

    var excludePatterns: String = "__pycache__, *.pyc, .git, .idea, venv"
        set(value) {
            field = value
            txtExcludePatterns.text = value
        }

    var maxDepth: Int = -1
        set(value) {
            field = value
            txtMaxDepth.text = value.toString()
        }

    init {
        title = "Directory Structure Generator Settings"
        init()
        setOKButtonText("Generate")
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())

        // Create settings panel
        val settingsPanel = JPanel(GridLayout(0, 2, 5, 5))

        settingsPanel.add(JBLabel("Output Path:"))
        settingsPanel.add(txtOutputPath)

        settingsPanel.add(JBLabel("Output Format:"))
        settingsPanel.add(cmbOutputFormat)

        settingsPanel.add(JBLabel("Max Depth (-1 = unlimited):"))
        settingsPanel.add(txtMaxDepth)

        settingsPanel.add(JBLabel("Exclude Patterns (comma separated):"))
        settingsPanel.add(txtExcludePatterns)

        settingsPanel.add(chkIncludeHidden)
        settingsPanel.add(JBLabel("")) // Empty cell for alignment

        settingsPanel.add(chkPythonOnly)
        settingsPanel.add(JBLabel("")) // Empty cell for alignment

        settingsPanel.add(chkIncludeTimestamp)
        settingsPanel.add(JBLabel("")) // Empty cell for alignment

        settingsPanel.add(chkIncludeFileCount)
        settingsPanel.add(JBLabel("")) // Empty cell for alignment

        panel.add(settingsPanel, BorderLayout.CENTER)

        // Add help text
        val helpText = JTextArea(
            "Generate a documentation file of your project's directory structure.\n\n" +
                    "- Output Path: Where to save the generated file\n" +
                    "- Max Depth: Limit the depth of directories shown\n" +
                    "- Exclude Patterns: Skip files/folders matching these patterns\n" +
                    "- Show Python Only: Only include Python files and packages"
        )
        helpText.isEditable = false
        helpText.lineWrap = true
        helpText.wrapStyleWord = true
        helpText.margin = Insets(10, 10, 10, 10)
        helpText.background = UIManager.getColor("Panel.background")

        panel.add(helpText, BorderLayout.NORTH)

        return panel
    }

    override fun doOKAction() {
        // Save settings
        outputPath = txtOutputPath.text
        outputFormat = when (cmbOutputFormat.selectedIndex) {
            1 -> "md"
            else -> "txt"
        }
        includeHidden = chkIncludeHidden.isSelected
        pythonOnly = chkPythonOnly.isSelected
        includeTimestamp = chkIncludeTimestamp.isSelected
        includeFileCount = chkIncludeFileCount.isSelected
        excludePatterns = txtExcludePatterns.text

        try {
            maxDepth = txtMaxDepth.text.toInt()
        } catch (e: NumberFormatException) {
            maxDepth = -1
        }

        super.doOKAction()
    }

    fun getGenerationSettings(): DocumentGenerator.GenerationSettings {
        return DocumentGenerator.GenerationSettings(
            includeHidden = includeHidden,
            includePythonFilesOnly = pythonOnly,
            maxDepth = maxDepth,
            includeTimestamp = includeTimestamp,
            includeFileCount = includeFileCount,
            excludePatterns = excludePatterns.split(",").map { it.trim() }
        )
    }
}
