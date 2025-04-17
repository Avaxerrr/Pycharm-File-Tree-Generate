// version 1.0

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
import java.awt.Dimension
import javax.swing.*

class GenerationConfigDialog(project: Project) : DialogWrapper(project) {

    private val txtOutputPath = JBTextField(project.basePath ?: "", 30)
    private val txtOutputFileName = JBTextField("directory-structure", 20)
    private val cmbOutputFormat = ComboBox(arrayOf("Text (.txt)", "Markdown (.md)"))
    private val chkIncludeHidden = JBCheckBox("Include hidden files", false)
    private val chkPythonOnly = JBCheckBox("Show Python files only", true)
    private val chkIncludeTimestamp = JBCheckBox("Include timestamp", true)
    private val chkIncludeFileCount = JBCheckBox("Include file counts", true)
    private val txtExcludePatterns = JBTextField("__pycache__, *.pyc, .git, .idea, venv", 30)
    private val txtMaxDepth = JBTextField("-1", 5)

    // Add radio buttons for tree style
    private val radioSimple = JRadioButton("Simple (+ and -)", true)
    private val radioBoxDrawing = JRadioButton("Box Drawing (├── and │)", false)
    private val radioAsciiExtended = JRadioButton("ASCII Extended (+--- and |)", false)
    private val treeStyleGroup = ButtonGroup()

    var outputPath: String = ""
        set(value) {
            field = value
            txtOutputPath.text = value
        }

    var outputFileName: String = "directory-structure"
        set(value) {
            field = value
            txtOutputFileName.text = value
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

    var treeStyle: TreeStyle = TreeStyle.SIMPLE
        set(value) {
            field = value
            when (value) {
                TreeStyle.BOX_DRAWING -> radioBoxDrawing.isSelected = true
                TreeStyle.ASCII_EXTENDED -> radioAsciiExtended.isSelected = true
                else -> radioSimple.isSelected = true
            }
        }

    init {
        title = "File Tree Generator Settings"

        // Set up tree style radio group
        treeStyleGroup.add(radioSimple)
        treeStyleGroup.add(radioBoxDrawing)
        treeStyleGroup.add(radioAsciiExtended)

        init()
        setOKButtonText("Generate")

    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout(5, 5))

        // Create settings panel
        val settingsPanel = JPanel(GridLayout(0, 2, 5, 5))

        settingsPanel.add(JBLabel("Output Path:"))
        settingsPanel.add(txtOutputPath)

        settingsPanel.add(JBLabel("Output Filename:"))
        settingsPanel.add(txtOutputFileName)

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

        // Create a more compact tree style panel
        val treeStylePanel = JPanel(GridLayout(1, 3, 5, 0))
        treeStylePanel.border = BorderFactory.createTitledBorder("Tree Style")

        treeStylePanel.add(radioSimple)
        treeStylePanel.add(radioBoxDrawing)
        treeStylePanel.add(radioAsciiExtended)

        // Add compact help text
        val helpText = JTextArea("Generate a documentation file with the selected tree style. The file will be saved at the specified path.")
        helpText.isEditable = false
        helpText.lineWrap = true
        helpText.wrapStyleWord = true
        helpText.margin = Insets(5, 5, 5, 5)
        helpText.background = UIManager.getColor("Panel.background")

        // Create a fixed size for help text to prevent it from expanding
        helpText.preferredSize = Dimension(500, 40)

        // Combine panels in a compact South panel
        val southPanel = JPanel(BorderLayout(5, 5))
        southPanel.add(helpText, BorderLayout.NORTH)
        southPanel.add(treeStylePanel, BorderLayout.CENTER)

        panel.add(southPanel, BorderLayout.SOUTH)

        //panel.preferredSize = Dimension(500, 400)
        panel.minimumSize = Dimension(600, 500)
        //panel.maximumSize = Dimension(600, 500)


        return panel
    }

    override fun doOKAction() {
        // Save settings
        outputPath = txtOutputPath.text
        outputFileName = txtOutputFileName.text
        outputFormat = when (cmbOutputFormat.selectedIndex) {
            1 -> "md"
            else -> "txt"
        }
        includeHidden = chkIncludeHidden.isSelected
        pythonOnly = chkPythonOnly.isSelected
        includeTimestamp = chkIncludeTimestamp.isSelected
        includeFileCount = chkIncludeFileCount.isSelected
        excludePatterns = txtExcludePatterns.text

        treeStyle = when {
            radioBoxDrawing.isSelected -> TreeStyle.BOX_DRAWING
            radioAsciiExtended.isSelected -> TreeStyle.ASCII_EXTENDED
            else -> TreeStyle.SIMPLE
        }

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
            excludePatterns = excludePatterns.split(",").map { it.trim() },
            treeStyle = treeStyle
        )
    }
}
