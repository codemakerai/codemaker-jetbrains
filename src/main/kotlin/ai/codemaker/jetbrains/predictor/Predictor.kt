/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.predictor

import ai.codemaker.jetbrains.file.FileExtensions
import ai.codemaker.jetbrains.service.CodeMakerService
import ai.codemaker.jetbrains.settings.AppSettingsState
import ai.codemaker.sdk.client.model.Modify
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.util.*

@Service(Service.Level.PROJECT)
class Predictor(private val project: Project) {

    private val lastPredictiveGeneration = HashMap<String, Date>()

    private val codeMakerService
        get() = project.getService(CodeMakerService::class.java)

    companion object {
        private const val PREDICTIVE_GENERATION_COOL_DOWN = 1000 * 30;
    }

    fun refresh(file: VirtualFile) {
        if (!AppSettingsState.instance.predictiveGenerationEnabled) {
            return
        }

        if (!FileExtensions.isSupported(file.extension)) {
            return
        }

        if (!canRefresh(file)) {
            return
        }

        codeMakerService.predictiveGenerate(file, Modify.NONE)
    }

    fun delete(file: VirtualFile) {
        lastPredictiveGeneration.remove(file.path)
    }

    private fun canRefresh(file: VirtualFile): Boolean {
        val lastGeneration = lastPredictiveGeneration[file.path]
        return lastGeneration == null
                || (Date().time - lastGeneration.time) >= PREDICTIVE_GENERATION_COOL_DOWN
    }
}