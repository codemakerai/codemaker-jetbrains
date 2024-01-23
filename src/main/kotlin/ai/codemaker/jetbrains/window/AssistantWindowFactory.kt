/*
 * Copyright 2024 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.window

import ai.codemaker.jetbrains.assistant.Message
import ai.codemaker.jetbrains.assistant.Role
import ai.codemaker.jetbrains.file.FileExtensions
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
            val message = Message(UUID.randomUUID().toString(), input, role, Date())
            messages.add(message)
            appendMessage(message)
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

                    if (file != null && FileExtensions.isSupported(file.extension)) {
                        val output = service.assistantCodeCompletion(input, file)
                        addMessage(output, Role.Assistant)
                    } else {
                        val output = service.assistantCompletion(input)
                        addMessage(output, Role.Assistant)
                    }
                }
            }
        }

        private fun appendMessage(message: Message) {
            val content = renderMarkdown(message.content).replace("\"", "\\\"")
            val assistant = if (message.role == Role.Assistant)  "true" else "false"
            chatScreen.cefBrowser.executeJavaScript("window.append(\"$content\", ${assistant})", "", 0)
        }

        private fun chatHtml(): String {
            // language=HTML
            return """
                <div id="container" style="width:100%; height:100%;background-color:#323232">
                  <div id="chat">
                  </div>
                  <span id="anchor"></span>
                </div>
                <script>
                    window.append = function(body, assistant) {
                        let container = document.createElement("div");
                        let color = assistant ? "#464646" : "#323232";
                        container.style.cssText = "padding:10px;border:0px;border-radius:5px;margin:10px;color:#f8f8f2;background-color:" + color + ";";
                        container.innerHTML = "<div>" + (assistant ? "Assistant" : "User")   + "</div>"
                        
                        let message = document.createElement("div");                        
                        message.innerHTML = body;
                        container.appendChild(message);
                        
                        document.getElementById("chat").appendChild(container);
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