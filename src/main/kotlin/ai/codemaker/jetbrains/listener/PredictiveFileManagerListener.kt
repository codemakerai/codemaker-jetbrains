/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.listener

import ai.codemaker.jetbrains.predictor.Predictor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class PredictiveFileManagerListener(private val project: Project) : FileEditorManagerListener {

    private val predictor
        get() = project.getService(Predictor::class.java)

    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        try {
            predictor.refresh(file)
        } catch (ignore: Exception) {
            // ignores
        }
    }

    override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
        try {
            predictor.delete(file)
        } catch (ignore: Exception) {
            // ignores
        }
    }
}