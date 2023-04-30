/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.jetbrains.action

import ai.codemaker.jetbrains.service.CodeMakerService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiDocumentManager

class GenerateCodeAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(CommonDataKeys.PROJECT) ?: return

        val service: CodeMakerService = project.getService(CodeMakerService::class.java)

        val editor = e.getData(CommonDataKeys.EDITOR)
        if (editor != null) {
            val documentManager = PsiDocumentManager.getInstance(project)
            val file = documentManager.getPsiFile(editor.document) ?: return
            documentManager.commitDocument(editor.document)
            service.generateCode(file.virtualFile)
        } else {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
            service.generateCode(file)
        }
    }
}