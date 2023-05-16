/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.jetbrains.action

import ai.codemaker.jetbrains.service.CodeMakerService
import ai.codemaker.sdk.client.model.Modify
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiDocumentManager

class ReplaceDocumentationAction : BaseDocumentationAction(Modify.REPLACE) {

}