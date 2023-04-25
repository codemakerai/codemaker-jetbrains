/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.jetbrains.service

import ai.codemaker.jetbrains.file.FileExtensions
import ai.codemaker.jetbrains.settings.AppSettingsState.Companion.instance
import ai.codemaker.sdk.client.Client
import ai.codemaker.sdk.client.DefaultClient
import ai.codemaker.sdk.client.model.*
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ContentIterator
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@Service(Service.Level.PROJECT)
class CodeMakerService(private val project: Project) {

    private val logger = Logger.getInstance(CodeMakerService::class.java)

    fun generateCode(path: VirtualFile?) {
        runInBackground("Generating code") {
            try {
                val client = createClient()
                walkFiles(path) { file: VirtualFile ->
                    if (file.isDirectory) {
                        return@walkFiles true
                    }

                    try {
                        processFile(client, file, Mode.CODE)
                        return@walkFiles true
                    } catch (e: Exception) {
                        logger.error("Failed to generate code in file.", e)
                        return@walkFiles false
                    }
                }
            } catch (e: Exception) {
                logger.error("Failed to generate code in file.", e)
            }
        }
    }

    fun generateDocumentation(path: VirtualFile?) {
        runInBackground("Generating documentation") {
            try {
                val client = createClient()
                walkFiles(path) { file: VirtualFile ->
                    if (file.isDirectory) {
                        return@walkFiles true
                    }

                    try {
                        processFile(client, file, Mode.DOCUMENT)
                        return@walkFiles true
                    } catch (e: Exception) {
                        logger.error("Failed to generate documentation in file.", e)
                        return@walkFiles false
                    }
                }
            } catch (e: Exception) {
                logger.error("Failed to generate documentation in file.", e)
            }
        }
    }

    private fun runInBackground(title: String, runnable: Runnable) {
        val task = object : Task.Backgroundable(project, title, false) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Processing"
                indicator.isIndeterminate = false
                indicator.fraction = 0.0
                runnable.run()
                indicator.fraction = 1.0
            }
        }.setCancelText("Stop Generating")
        ProgressManager.getInstance().run(task)
    }

    @Throws(InterruptedException::class)
    private fun process(client: Client, mode: Mode, language: Language, source: String): String {
        val processResponse = client.createProcess(createProcessRequest(mode, language, source))
        var success = false
        val timeout = Instant.now().plus(10, ChronoUnit.MINUTES)
        while (timeout.isAfter(Instant.now())) {
            val processStatus = client.getProcessStatus(
                    createProcessStatusRequest(processResponse.id)
            )
            if (isCompleted(processStatus.status)) {
                success = true
                break
            }
            TimeUnit.MILLISECONDS.sleep(1000)
        }
        if (!success) {
            throw RuntimeException("Processing task had failed")
        }
        val processOutput = client.getProcessOutput(
                createProcessOutputRequest(processResponse.id)
        )
        return processOutput.output.source
    }

    private fun walkFiles(path: VirtualFile?, iterator: ContentIterator) {
        VfsUtilCore.iterateChildrenRecursively(
                path!!,
                ::filterFile,
                iterator
        )
    }

    private fun filterFile(file: VirtualFile): Boolean {
        return file.isDirectory || FileExtensions.isSupported(file.extension)
    }

    private fun processFile(client: Client, file: VirtualFile, mode: Mode) {
        val language = FileExtensions.languageFromExtension(file.extension)
        val source = String(file.contentsToByteArray(), file.charset)
        val output = process(client, mode, language!!, source)
        writeToFile(file, output)
    }

    private fun writeToFile(file: VirtualFile, output: String) {
        WriteCommandAction.runWriteCommandAction(project) {
            try {
                val newTimestamp = Instant.now().epochSecond
                file.setBinaryContent(
                        output.toByteArray(file.charset), newTimestamp, newTimestamp, this@CodeMakerService
                )
            } catch (e: Exception) {
                logger.error("Failed to write file.", e)
            }
        }
    }

    private fun createClient(): Client {
        val appSettings = instance
        return DefaultClient(appSettings.apiKey)
    }

    private fun isCompleted(status: Status): Boolean {
        return status == Status.COMPLETED
    }

    private fun createProcessRequest(mode: Mode, language: Language, source: String): CreateProcessRequest {
        return CreateProcessRequest(
                Process(
                        mode,
                        language,
                        Input(source)
                )
        )
    }

    private fun createProcessStatusRequest(id: String): GetProcessStatusRequest {
        return GetProcessStatusRequest(id)
    }

    private fun createProcessOutputRequest(id: String): GetProcessOutputRequest {
        return GetProcessOutputRequest(id)
    }
}