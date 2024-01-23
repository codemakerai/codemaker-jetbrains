/*
 * Copyright 2024 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.window

import ai.codemaker.jetbrains.assistant.Message
import ai.codemaker.jetbrains.assistant.Role
import ai.codemaker.jetbrains.service.CodeMakerService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.ui.JBUI
import org.cef.browser.CefBrowser
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.util.*
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextField


class AssistantWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val assistantWindow = AssistantWindow(project)
        val content: Content = ContentFactory.getInstance().createContent(assistantWindow.contentPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }

    class AssistantWindow(val project: Project) {

        val contentPanel = JPanel()
        val chatScreen = JBCefBrowser()
        val messageTextField = JTextField()

        val messages = ArrayList<Message>()

        init {
            contentPanel.setLayout(BorderLayout(0, 10))
            contentPanel.border = JBUI.Borders.empty(5)
            contentPanel.add(createChatPanel(), BorderLayout.CENTER)
            contentPanel.add(createMessagePanel(), BorderLayout.SOUTH)
        }

        private fun createChatPanel(): Component {
            if (!JBCefApp.isSupported()) {
                // TODO log warning
            }

            chatScreen.loadHTML(chatHtml())
            return chatScreen.component
        }

        private fun createMessagePanel(): Component {
            val messagePanel = JPanel()
            messagePanel.layout = BorderLayout()
            messagePanel.border = JBUI.Borders.empty(10)
            messageTextField.addKeyListener(MessageTextKeyListener())
            messagePanel.add(messageTextField, BorderLayout.CENTER)

            val sendButton = JButton("Send")
            sendButton.addActionListener(SendActionListener())
            messagePanel.add(sendButton, BorderLayout.EAST)

            return messagePanel
        }

        private fun addMessage(input: String, role: Role) {
            // TODO cap message size
            messages.add(Message(UUID.randomUUID().toString(), input, role, Date()))

            val content = renderMarkdown(input).replace("\"", "\\\"")
            chatScreen.cefBrowser.executeJavaScript("window.append(\"$content\")", "", 0)
        }

        private fun sendMessage() {
            val input = messageTextField.text.trim()

            if (input.isNotEmpty()) {
                messageTextField.text = ""
                addMessage(input, Role.User)

                ApplicationManager.getApplication().executeOnPooledThread {
                    val service: CodeMakerService = project.getService(CodeMakerService::class.java)

                    val fileEditorManager = FileEditorManager.getInstance(project)
                    val file = fileEditorManager.getSelectedEditor()?.file

                    if (file != null) {
                        println("Processing file: " + file.path)
                    }

                    val output = service.assistantCodeCompletion(input, file)

                    addMessage(output, Role.Assistant)
                }
            }
        }

        private fun chatHtml(): String {
            // language=HTML
            return """
                <div id="container" style="width:100%; height:100%;">
                  <div id="chat">
                  </div>
                  <span id="anchor"></span>
                </div>
                <script>
                    window.append = function(body) {
                        let message = document.createElement("div");                                                
                        message.style.cssText = "background-color:#f1f1f1;padding:10px;border:2px solid #dedede;border-radius:5px;margin:10px 0;";
                        message.innerHTML = body;
                        document.getElementById("chat").appendChild(message);
                        document.getElementById("anchor").scrollIntoView({ behavior: "smooth"});
                    }
                </script>
                """.trimIndent()
        }

        private fun renderMarkdown(source: String): String {
            val flavour = CommonMarkFlavourDescriptor()
            val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(source)
            return HtmlGenerator(source, parsedTree, flavour).generateHtml()
        }

        inner class SendActionListener : ActionListener {
            override fun actionPerformed(e: ActionEvent?) {
                sendMessage()
            }
        }

        inner class MessageTextKeyListener : KeyListener {
            override fun keyTyped(e: KeyEvent?) {
            }

            override fun keyPressed(e: KeyEvent?) {
                if (e?.keyCode == KeyEvent.VK_ENTER) {
                    sendMessage()
                }
            }

            override fun keyReleased(e: KeyEvent?) {
            }
        }
    }
}