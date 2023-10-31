/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.extension

import ai.codemaker.jetbrains.corrector.Corrector
import com.intellij.ide.actionsOnSave.impl.ActionsOnSaveFileDocumentManagerListener.ActionOnSave
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager

class CorrectionSaveActionExtension : ActionOnSave() {

    override fun isEnabledForProject(project: Project): Boolean {
        return project.getService(Corrector::class.java) != null
    }

    override fun processDocuments(project: Project, documents: Array<out Document>) {
        try {
            val corrector = project.getService(Corrector::class.java)
            val documentManager = PsiDocumentManager.getInstance(project)
            documents.forEach {
                val psiFile = documentManager.getPsiFile(it) ?: return
                corrector.correct(psiFile.virtualFile)
            }
        } catch (ignore: Exception) {
            // ignores
        }
    }
}