/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.listener

import ai.codemaker.jetbrains.corrector.Corrector
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class CorrectionFileManagerListener(private val project: Project) : FileEditorManagerListener {

    private val corrector
        get() = project.getService(Corrector::class.java)

    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        try {
            corrector.correct(file)
        } catch (ignore: Exception) {
            // ignores
        }
    }

    override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
        try {
            corrector.delete(file)
        } catch (ignore: Exception) {
            // ignores
        }
    }
}