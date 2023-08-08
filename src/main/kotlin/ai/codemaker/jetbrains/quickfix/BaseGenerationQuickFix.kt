/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.quickfix

import ai.codemaker.jetbrains.psi.PsiUtils
import ai.codemaker.jetbrains.service.CodeMakerService
import com.intellij.codeInsight.intention.HighPriorityAction
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

abstract class BaseGenerationQuickFix(private val text: String) : BaseIntentionAction(), HighPriorityAction {

    override fun getText(): String {
        return text
    }

    override fun getFamilyName(): String {
        return "CodeMaker AI"
    }

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        return true
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null) {
            return
        }
        if (file == null) {
            return
        }

        val service: CodeMakerService = project.getService(CodeMakerService::class.java) ?: return
        val method = PsiUtils.getMethod(file, editor.caretModel.offset) ?: return
        doInvoke(service, project, editor.document, method.codePath)
    }

    abstract fun doInvoke(service: CodeMakerService, project: Project, document: Document, codePath: String?)
}