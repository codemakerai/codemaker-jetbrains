/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.quickfix

import ai.codemaker.jetbrains.service.CodeMakerService
import com.intellij.codeInsight.intention.HighPriorityAction
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
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

    override fun generatePreview(project: Project, editor: Editor, file: PsiFile): IntentionPreviewInfo {
        return IntentionPreviewInfo.EMPTY
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null) {
            return
        }
        if (file == null) {
            return
        }

        val service: CodeMakerService = project.getService(CodeMakerService::class.java) ?: return
        val codePath = getCodePath(file, editor.caretModel.offset) ?: return
        doInvoke(project, editor, service, file.virtualFile, codePath)
    }

    abstract fun getCodePath(file: PsiFile, offset: Int): String?

    abstract fun doInvoke(project: Project, editor: Editor, service: CodeMakerService, file: VirtualFile, codePath: String?)
}