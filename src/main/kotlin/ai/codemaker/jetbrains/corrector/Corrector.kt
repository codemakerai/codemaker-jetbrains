/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.corrector

import ai.codemaker.jetbrains.file.FileExtensions
import ai.codemaker.jetbrains.service.CodeMakerService
import ai.codemaker.jetbrains.settings.AppSettingsState
import ai.codemaker.sdkv2.client.model.Modify
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

@Service(Service.Level.PROJECT)
class Corrector(private val project: Project) {

    private val correctedFiles = HashSet<String>()

    private val codeMakerService
        get() = project.getService(CodeMakerService::class.java)

    companion object {
        private const val PREDICTIVE_GENERATION_COOL_DOWN = 1000 * 30;
    }

    fun correct(file: VirtualFile) {
        if (!AppSettingsState.instance.syntaxAutocorrectionEnabled) {
            return
        }

        if (!FileExtensions.isSupported(file.extension)) {
            return
        }

        if (!canCorrect(file)) {
            return
        }

        try {
            correctedFiles.add(file.path)
            codeMakerService.fixSyntax(file, Modify.REPLACE)
        } finally {
            correctedFiles.remove(file.path)
        }
    }

    fun delete(file: VirtualFile) {
        correctedFiles.remove(file.path)
    }

    private fun canCorrect(file: VirtualFile): Boolean {
        return !correctedFiles.contains(file.path)
    }
}