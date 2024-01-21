/*
 * Copyright 2024 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.window

import ai.codemaker.jetbrains.assistant.Message
import ai.codemaker.jetbrains.assistant.Role
import ai.codemaker.jetbrains.service.CodeMakerService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.*
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.JTextField


class AssistantWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val assistantWindow = AssistantWindow(project)
        val content: Content = ContentFactory.getInstance().createContent(assistantWindow.contentPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }

    class AssistantWindow(val project: Project) {

        val contentPanel = JPanel()
        val chatArea = JTextArea()
        val messageTextField = JTextField()

        val messages = mutableListOf(
                Message("1", "Hello", Role.Assistant, Date()),
                Message("2", "What is Java?", Role.User, Date()),
                Message("3", "Java is programming language", Role.User, Date()),
        )

        init {
            contentPanel.setLayout(BorderLayout(0, 20))
            contentPanel.border = JBUI.Borders.empty(5)
            contentPanel.add(createChatPanel(), BorderLayout.CENTER)
            contentPanel.add(createMessagePanel(), BorderLayout.SOUTH)
        }

        private fun createChatPanel(): Component {
            chatArea.isEditable = false
            chatArea.lineWrap = true
            chatArea.wrapStyleWord = true
            return JBScrollPane(chatArea)
        }

        private fun createMessagePanel(): Component {
            val messagePanel = JPanel()
            messagePanel.layout = BorderLayout()
            messagePanel.border = JBUI.Borders.empty(10)
            messagePanel.add(messageTextField, BorderLayout.CENTER)

            val sendButton = JButton("Send")
            sendButton.addActionListener(SendActionListener())
            messagePanel.add(sendButton, BorderLayout.EAST)

            return messagePanel
        }

        private fun addMessage(input: String, role: Role) {
            messages.add(Message(UUID.randomUUID().toString(), input, role, Date()))

            if (chatArea.text.isNotEmpty()) {
                chatArea.insert("\n", chatArea.text.length)
            }

            chatArea.insert(input, chatArea.text.length)
        }

        inner class SendActionListener : ActionListener {
            override fun actionPerformed(e: ActionEvent?) {
                val input = messageTextField.text.trim()

                if (input.isNotEmpty()) {
                    messageTextField.text = ""
                    addMessage(input, Role.User)

                    ApplicationManager.getApplication().executeOnPooledThread {
                        val service: CodeMakerService = project.getService(CodeMakerService::class.java)
                        val output = service.assistantCompletion(input)

                        addMessage(output, Role.Assistant)
                    }
                }
            }
        }
    }
}