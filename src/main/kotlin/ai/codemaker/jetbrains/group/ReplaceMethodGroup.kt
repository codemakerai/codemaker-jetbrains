/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.group

import ai.codemaker.jetbrains.psi.PsiUtils
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DefaultActionGroup

class ReplaceMethodGroup : DefaultActionGroup() {

    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        val method = PsiUtils.getMethod(psiFile, editor.caretModel.offset)

        presentation.isVisible = method != null
        if (method == null) {
            return
        }
        presentation.text = "Replace '${method.name}'"
    }
}