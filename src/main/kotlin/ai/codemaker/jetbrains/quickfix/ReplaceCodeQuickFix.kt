/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.quickfix

import ai.codemaker.jetbrains.service.CodeMakerService
import ai.codemaker.sdk.client.model.Modify
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.VirtualFile

class ReplaceCodeQuickFix : BaseGenerationQuickFix("Replace code") {
    override fun doInvoke(service: CodeMakerService, file: VirtualFile, codePath: String?) {
        ApplicationManager.getApplication().invokeLater {
            service.generateCode(file, Modify.REPLACE, codePath)
        }
    }
}