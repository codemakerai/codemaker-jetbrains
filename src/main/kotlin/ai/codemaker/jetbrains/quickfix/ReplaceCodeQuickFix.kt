/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.quickfix

import ai.codemaker.jetbrains.service.CodeMakerService
import ai.codemaker.sdk.client.model.Modify
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager

class ReplaceCodeQuickFix : BaseGenerationQuickFix("Replace code") {
    override fun doInvoke(service: CodeMakerService, project: Project, document: Document, codePath: String?) {
        ApplicationManager.getApplication().invokeLater {
            val documentManager = PsiDocumentManager.getInstance(project)
            val psiFile = documentManager.getPsiFile(document) ?: return@invokeLater
            service.generateCode(psiFile.virtualFile, Modify.REPLACE, codePath)
        }
    }
}