/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.jetbrains.action

import ai.codemaker.jetbrains.service.CodeMakerService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

class GenerateDocumentationAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val path = e.getData(CommonDataKeys.VIRTUAL_FILE)
        if (path != null) {
            val project = e.getData(CommonDataKeys.PROJECT)
            val service: CodeMakerService = project!!.getService(CodeMakerService::class.java)
            service.generateDocumentation(path)
        }
    }
}