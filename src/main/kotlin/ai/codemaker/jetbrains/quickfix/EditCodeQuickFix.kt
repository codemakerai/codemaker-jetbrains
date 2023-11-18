/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.quickfix

import ai.codemaker.jetbrains.popup.InputPopup
import ai.codemaker.jetbrains.service.CodeMakerService
import ai.codemaker.sdkv2.client.model.Modify
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JTextField

class EditCodeQuickFix : BaseGenerationQuickFix("Edit code with prompt") {
    override fun getCodePath(file: PsiFile, offset: Int): String? {
        return "@$offset"
    }

    override fun doInvoke(project: Project, editor: Editor, service: CodeMakerService, file: VirtualFile, codePath: String?) {
        if (codePath == null) {
            return
        }

        InputPopup.showInputPopup(editor) { prompt: String? ->
            if (!prompt.isNullOrBlank()) {
                ApplicationManager.getApplication().invokeLater {
                    service.editCode(file, Modify.REPLACE, codePath, prompt)
                }
            }
        }
    }
}