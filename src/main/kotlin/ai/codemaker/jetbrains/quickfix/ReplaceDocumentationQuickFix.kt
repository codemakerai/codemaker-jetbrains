/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.quickfix

import ai.codemaker.jetbrains.service.CodeMakerService
import ai.codemaker.sdk.client.model.Modify
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.VirtualFile

class ReplaceDocumentationQuickFix : BaseGenerationQuickFix("Replace documentation") {

    override fun doInvoke(service: CodeMakerService, file: VirtualFile, codePath: String?) {
        ApplicationManager.getApplication().invokeLater {
            service.generateDocumentation(file, Modify.REPLACE, codePath)
        }
    }
}