/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.popup

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupListener
import com.intellij.openapi.ui.popup.LightweightWindowEvent
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JTextField

object InputPopup {

    fun interface InputPopupListener {

        fun onClose(input: String?)
    }

    fun showInputPopup(editor: Editor, listener: InputPopupListener) {
        val promptField = JTextField(30)
        val popup = JBPopupFactory.getInstance().createComponentPopupBuilder(promptField, promptField)
                .setRequestFocus(true)
                .createPopup()
        promptField.addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent?) {

            }

            override fun keyPressed(e: KeyEvent?) {
                if (e?.keyCode == KeyEvent.VK_ENTER) {
                    popup.closeOk(e)
                } else if (e?.keyCode == KeyEvent.VK_ESCAPE) {
                    popup.cancel(e)
                }
            }

            override fun keyReleased(e: KeyEvent?) {

            }

        })
        popup.addListener(object : JBPopupListener {
            override fun onClosed(event: LightweightWindowEvent) {
                if (event.isOk) {
                    listener.onClose(promptField.text)
                }
            }

        })
        popup.showInBestPositionFor(editor)
    }
}