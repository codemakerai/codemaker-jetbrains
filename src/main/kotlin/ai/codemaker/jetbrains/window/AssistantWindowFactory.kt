/*
 * Copyright 2024 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.window

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel


class AssistantWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val assistantWindow = AssistantWindow(toolWindow)
        val content: Content = ContentFactory.getInstance().createContent(assistantWindow.contentPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }

    class AssistantWindow(private val toolWindow: ToolWindow) {

        val contentPanel = JPanel()

        init {
            contentPanel.setLayout(BorderLayout(0, 20))
            contentPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0))
            contentPanel.add(JLabel("CodeMaker AI"), BorderLayout.CENTER)
        }
    }
}