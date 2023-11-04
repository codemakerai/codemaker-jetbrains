/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.action

import ai.codemaker.jetbrains.service.CodeMakerService
import ai.codemaker.sdkv2.client.model.Modify
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiDocumentManager
import java.util.regex.Pattern

class GenerateInlineCodeAction : AnAction() {

    companion object {
        private val COMMENT_PATTERN = Pattern.compile("^\\s*(//|#).*")
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        e.presentation.isVisible = hasCommentAt(editor.document, editor.caretModel.offset)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val documentManager = PsiDocumentManager.getInstance(project)
        val psiFile = documentManager.getPsiFile(editor.document) ?: return

        if (hasCommentAt(editor.document, editor.caretModel.offset)) {
            documentManager.commitDocument(editor.document)

            val service: CodeMakerService = project.getService(CodeMakerService::class.java)
            val codePath = getCodePath(editor.caretModel.offset)

            service.generateInlineCode(psiFile.virtualFile, Modify.NONE, codePath)
        }
    }

    private fun hasCommentAt(document: Document, offset: Int): Boolean {
        var i = offset;
        while (i - 1 >= 0) {
            if (document.charsSequence[i - 1] == '\n') {
                break;
            }
            i--
        }

        val line = document.charsSequence.subSequence(i, offset)
        val matcher = COMMENT_PATTERN.matcher(line)
        return matcher.matches()
    }

    private fun getCodePath(offset: Int): String? {
        return "@$offset"
    }
}