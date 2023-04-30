/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.jetbrains.service

import ai.codemaker.jetbrains.file.FileExtensions
import ai.codemaker.jetbrains.settings.AppSettingsState.Companion.instance
import ai.codemaker.sdk.client.Client
import ai.codemaker.sdk.client.DefaultClient
import ai.codemaker.sdk.client.model.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
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
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.intellij.util.ThrowableRunnable
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.Callable
import java.util.concurrent.FutureTask
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
        try {
            val task = ReadAction.nonBlocking(Callable {
                val documentManager = PsiDocumentManager.getInstance(project)
                val psiFile = PsiManager.getInstance(project).findFile(file) ?: return@Callable null
                val document = documentManager.getDocument(psiFile) ?: return@Callable null
                val source = document.text

                FutureTask<String> {
                    val language = FileExtensions.languageFromExtension(file.extension)
                    return@FutureTask process(client, mode, language!!, source)
                }
            }).executeSynchronously() ?: return

            task.run()
            val output = task.get() ?: return

            ApplicationManager.getApplication().invokeAndWait {
                WriteCommandAction.writeCommandAction(project)
                        .run(ThrowableRunnable<java.lang.RuntimeException> {
                            val documentManager = PsiDocumentManager.getInstance(project)
                            val psiFile = PsiManager.getInstance(project).findFile(file) ?: return@ThrowableRunnable
                            val document = documentManager.getDocument(psiFile) ?: return@ThrowableRunnable

                            document.setText(output)
                            documentManager.commitDocument(document)
                        })
            }
        } catch (e: Exception) {
            logger.error("Failed to process file.", e)
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