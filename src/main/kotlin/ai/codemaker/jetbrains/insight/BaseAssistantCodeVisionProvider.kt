/*
 * Copyright 2024 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.insight

import ai.codemaker.jetbrains.assistant.publisher.AssistantMessagePublisher
import ai.codemaker.jetbrains.file.FileExtensions
import ai.codemaker.jetbrains.psi.PsiMethod
import ai.codemaker.jetbrains.settings.AppSettingsState
import com.intellij.codeInsight.hints.codeVision.CodeVisionProviderBase
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNameIdentifierOwner
import java.awt.event.MouseEvent

abstract class BaseAssistantCodeVisionProvider(suffix: String, private val hint: String, private val prompt: String) : CodeVisionProviderBase() {

    override val id = "ai.codemaker.codevision.assistant.$suffix"

    override val name = "CodeMaker AI Assistant $hint"

    override fun acceptsFile(file: PsiFile): Boolean {
        return AppSettingsState.instance.assistantCodeVisionEnabled && FileExtensions.isSupported(file.virtualFile?.extension)
    }

    override fun acceptsElement(element: PsiElement): Boolean {
        return AppSettingsState.instance.assistantCodeVisionEnabled && PsiMethod.isMethod(element)
    }

    override fun getHint(element: PsiElement, file: PsiFile): String? {
        return hint
    }

    override fun handleClick(editor: Editor, element: PsiElement, event: MouseEvent?) {
        val method = PsiMethod(element as PsiNameIdentifierOwner)
        val project = element.project

        AssistantMessagePublisher.publishMessage(project, "$prompt ${method.elementName} method.")
    }
}