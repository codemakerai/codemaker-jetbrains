/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.action

import ai.codemaker.jetbrains.popup.InputPopup
import ai.codemaker.jetbrains.psi.PsiUtils
import ai.codemaker.jetbrains.service.CodeMakerService
import ai.codemaker.jetbrains.ui.UIUtils
import ai.codemaker.sdkv2.client.model.Modify
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiDocumentManager

class EditMethodCodeAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        val method = PsiUtils.getMethod(psiFile, editor.caretModel.offset)

        presentation.isVisible = method != null
        if (method == null) {
            return
        }

        presentation.text = "Edit '${UIUtils.displayName(method.name)}' code"
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(CommonDataKeys.PROJECT) ?: return

        val service: CodeMakerService = project.getService(CodeMakerService::class.java)

        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val documentManager = PsiDocumentManager.getInstance(project)
        val psiFile = documentManager.getPsiFile(editor.document) ?: return
        val codePath = getCodePath(editor.caretModel.offset) ?: return

        InputPopup.showInputPopup(editor) { prompt: String? ->
            if (!prompt.isNullOrBlank()) {
                ApplicationManager.getApplication().invokeLater {
                    documentManager.commitDocument(editor.document)
                    service.editCode(psiFile.virtualFile, Modify.REPLACE, codePath, prompt)
                }
            }
        }
    }

    private fun getCodePath(offset: Int): String? {
        return "@${offset}"
    }
}