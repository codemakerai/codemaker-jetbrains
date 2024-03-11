/*
 * Copyright 2024 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.window

import ai.codemaker.jetbrains.assistant.Message
import ai.codemaker.jetbrains.assistant.Role
import ai.codemaker.jetbrains.file.FileExtensions
import ai.codemaker.jetbrains.service.CodeMakerService
import ai.codemaker.jetbrains.settings.AppSettingsState
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefBrowserBase
import com.intellij.ui.jcef.JBCefBrowserBuilder
import com.intellij.ui.jcef.JBCefClient
import com.intellij.ui.jcef.JBCefJSQuery
import com.intellij.util.ui.JBUI
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefMessageRouter
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.network.CefRequest
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

    object AssistantWindowFactory {
        const val ASSISTANT_VIEW = "/webview/assistant.html"
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val assistantWindow = AssistantWindow(project)
        val content: Content = ContentFactory.getInstance().createContent(assistantWindow.contentPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }

    class AssistantWindow(val project: Project) : Disposable {

        val contentPanel = JPanel()
        private val chatScreen = JBCefBrowser.create(JBCefBrowserBuilder().setCreateImmediately(false))
        private val jsQuery = JBCefJSQuery.create(chatScreen as JBCefBrowserBase)
        private val messageTextField = JTextField()

        val messages = ArrayList<Message>()

        init {
            contentPanel.setLayout(BorderLayout(0, 10))
            contentPanel.border = JBUI.Borders.empty(5)
            contentPanel.add(createChatPanel(), BorderLayout.CENTER)
            contentPanel.add(createMessagePanel(), BorderLayout.SOUTH)

            Disposer.register(this, jsQuery)
        }

        override fun dispose() {
        }

        private fun createChatPanel(): Component {
            chatScreen.setProperty(JBCefBrowserBase.Properties.NO_CONTEXT_MENU, true)
            chatScreen.jbCefClient.addLoadHandler(LoadHandler(), chatScreen.cefBrowser)
            chatScreen.loadHTML(assistantView())
            return chatScreen.component
        }

        private fun createMessagePanel(): Component {
            val messagePanel = JPanel()
            messagePanel.layout = BorderLayout()
            messagePanel.border = JBUI.Borders.empty(10)
            messageTextField.addKeyListener(MessageTextKeyListener())
            messagePanel.add(messageTextField, BorderLayout.CENTER)

            val sendButton = JButton("Send")
            sendButton.addActionListener {
                sendMessage()
            }
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

                if (AppSettingsState.instance.apiKey.isNullOrEmpty()) {
                    addMessage(
                            "To use Assistant features, please first set the API Key in the Extension Settings." +
                                    "\nYou can create free account [here](https://portal.codemaker.ai/#/register).", Role.Assistant)
                    return
                }

                ApplicationManager.getApplication().executeOnPooledThread {
                    try {
                        val service: CodeMakerService = project.getService(CodeMakerService::class.java)

                        val fileEditorManager = FileEditorManager.getInstance(project)
                        val file = fileEditorManager.getSelectedEditor()?.file

                        val isAssistantActionsEnabled = AppSettingsState.instance.assistantActionsEnabled

                        if (isAssistantActionsEnabled && file != null && FileExtensions.isSupported(file.extension)) {
                            val output = service.assistantCodeCompletion(input, file)
                            addMessage(output, Role.Assistant)
                        } else {
                            val output = service.assistantCompletion(input)
                            addMessage(output, Role.Assistant)
                        }
                    } catch (e: Exception) {
                        addMessage("Assistant could not complete this request. Please try again.", Role.Assistant)
                    }
                }
            }
        }

        private fun appendMessage(message: Message) {
            val content = renderMarkdown(message.content).replace("\n", "\\n").replace("\"", "\\\"")
            val assistant = (message.role == Role.Assistant).toString()
            chatScreen.cefBrowser.executeJavaScript("window.append(\"$content\", ${assistant})", "", 0)
        }

        private fun assistantView(): String {
            return AssistantWindowFactory::class.java
                    .getResource(AssistantWindowFactory.ASSISTANT_VIEW)!!
                    .readText()
        }

        private fun renderMarkdown(source: String): String {
            val flavour = CommonMarkFlavourDescriptor()
            val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(source)
            return HtmlGenerator(source, parsedTree, flavour).generateHtml()
        }

        private fun registerEventHandler() {
            chatScreen.cefBrowser.executeJavaScript(
                    "window.openLink = function(link) { " + jsQuery.inject("link") + "};",
                    chatScreen.cefBrowser.url, 0)

            chatScreen.cefBrowser.executeJavaScript("""
                        document.addEventListener('click', function(e) {
                            e.preventDefault();
                            const link = e.target.closest('a').href;
                            if (link) {
                                window.openLink(link);
                            }
                        });
                    """.trimIndent(), chatScreen.cefBrowser.url, 0)

            jsQuery.addHandler {
                BrowserUtil.browse(it)
                return@addHandler null
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

        inner class LoadHandler : CefLoadHandlerAdapter() {
            override fun onLoadingStateChange(browser: CefBrowser?, isLoading: Boolean, canGoBack: Boolean, canGoForward: Boolean) {
                registerEventHandler()
            }
        }
    }
}