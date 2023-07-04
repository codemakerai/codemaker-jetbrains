/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.extension

import ai.codemaker.jetbrains.predictor.Predictor
import com.intellij.ide.actionsOnSave.impl.ActionsOnSaveFileDocumentManagerListener.ActionOnSave
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.utils.editor.getVirtualFile

class PredictiveSaveActionExtension : ActionOnSave() {

    override fun isEnabledForProject(project: Project): Boolean {
        return project.getService(Predictor::class.java) != null
    }

    override fun processDocuments(project: Project, documents: Array<out Document>) {
        try {
            val predictor = project.getService(Predictor::class.java)
            val documentManager = PsiDocumentManager.getInstance(project)
            documents.forEach {
                val psiFile = documentManager.getPsiFile(it) ?: return
                predictor.refresh(psiFile.virtualFile)
            }
        } catch (ignore: Exception) {
            // ignores
        }
    }
}