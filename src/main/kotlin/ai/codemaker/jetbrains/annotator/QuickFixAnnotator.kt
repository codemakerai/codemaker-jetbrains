/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.annotator

import ai.codemaker.jetbrains.psi.PsiMethod
import ai.codemaker.jetbrains.quickfix.EditCodeQuickFix
import ai.codemaker.jetbrains.quickfix.ReplaceCodeQuickFix
import ai.codemaker.jetbrains.quickfix.ReplaceDocumentationQuickFix
import ai.codemaker.jetbrains.settings.AppSettingsState
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement

class QuickFixAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (!AppSettingsState.instance.codeActionsEnabled) {
            return
        }

        if (!PsiMethod.isMethod(element)) {
            return
        }

        registerFix(holder, element, "Edit code with prompt", EditCodeQuickFix())
        registerFix(holder, element, "Replace code", ReplaceCodeQuickFix())
        registerFix(holder, element, "Fix code", ReplaceDocumentationQuickFix())
        registerFix(holder, element, "Replace documentation", ReplaceDocumentationQuickFix())
    }

    private fun registerFix(holder: AnnotationHolder, element: PsiElement, message: String, action: IntentionAction) {
        holder.newAnnotation(HighlightSeverity.INFORMATION, message)
                .range(element.textRange)
                .highlightType(ProblemHighlightType.INFORMATION)
                .withFix(action)
                .create()
    }
}