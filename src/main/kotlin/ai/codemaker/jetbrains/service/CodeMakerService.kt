/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.jetbrains.service

import ai.codemaker.jetbrains.file.FileExtensions
import ai.codemaker.jetbrains.settings.AppSettingsState.Companion.instance
import ai.codemaker.sdkv2.client.Client
import ai.codemaker.sdkv2.client.DefaultClient
import ai.codemaker.sdkv2.client.UnauthorizedException
import ai.codemaker.sdkv2.client.model.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProcessCanceledException
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
import java.util.concurrent.Callable

@Service(Service.Level.PROJECT)
class CodeMakerService(private val project: Project) {

    private val logger = Logger.getInstance(CodeMakerService::class.java)

    private val client: Client = DefaultClient {
        instance.apiKey
    }

    fun predictiveGenerate(path: VirtualFile?, modify: Modify) {
        runInBackground("Predictive generation") {
            try {
                walkFiles(path) { file: VirtualFile ->
                    if (file.isDirectory) {
                        return@walkFiles true
                    }

                    try {
                        predictiveProcessFile(client, file, Mode.CODE, modify)
                        return@walkFiles true
                    } catch (e: ProcessCanceledException) {
                        throw e
                    } catch (e: Exception) {
                        logger.error("Failed to generate code in file.", e)
                        return@walkFiles false
                    }
                }
            } catch (e: ProcessCanceledException) {
                throw e
            } catch (e: Exception) {
                logger.error("Failed to generate code in file.", e)
            }
        }
    }

    fun generateCode(path: VirtualFile?, modify: Modify, codePath: String? = null) {
        process(Mode.CODE, "Generating code", path, modify, codePath)
    }

    fun generateInlineCode(path: VirtualFile?, modify: Modify, codePath: String? = null) {
        process(Mode.INLINE_CODE, "Generating inline code", path, modify, codePath)
    }

    fun editCode(path: VirtualFile?, modify: Modify, codePath: String, prompt: String) {
        process(Mode.EDIT_CODE, "Editing code", path, modify, codePath, prompt)
    }

    fun generateDocumentation(path: VirtualFile?, modify: Modify, codePath: String? = null) {
        process(Mode.DOCUMENT, "Generating documentation", path, modify, codePath)
    }

    fun fixSyntax(path: VirtualFile?, modify: Modify, codePath: String? = null) {
        process(Mode.FIX_SYNTAX, "Fixing code", path, modify, codePath)
    }

    private fun process(mode: Mode, title: String, path: VirtualFile?, modify: Modify, codePath: String?, prompt: String? = null) {
        runInBackground(title) {
            try {
                walkFiles(path) { file: VirtualFile ->
                    if (file.isDirectory) {
                        return@walkFiles true
                    }

                    try {
                        processFile(client, file, mode, modify, codePath, prompt)
                        return@walkFiles true
                    } catch (e: ProcessCanceledException) {
                        throw e
                    } catch (e: Exception) {
                        logger.error("Failed to process $mode in file.", e)
                        return@walkFiles false
                    }
                }
            } catch (e: ProcessCanceledException) {
                throw e
            } catch (e: Exception) {
                logger.error("Failed to process $mode task.", e)
            }
        }
    }

    private fun runInBackground(title: String, runnable: Runnable) {
        val task = object : Task.Backgroundable(project, title, true) {
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
    private fun process(client: Client, mode: Mode, language: Language, source: String, modify: Modify, codePath: String?, prompt: String?): String {
        val response = client.process(createProcessRequest(mode, language, source, modify, codePath, prompt))
        return response.output.source
    }

    @Throws(InterruptedException::class)
    private fun predictiveProcess(client: Client, mode: Mode, language: Language, source: String, modify: Modify) {
        // TODO async requests
        client.process(createProcessRequest(mode, language, source, modify))
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

    private fun predictiveProcessFile(client: Client, file: VirtualFile, mode: Mode, modify: Modify) {
        try {
            val source = readFile(file) ?: return

            val language = FileExtensions.languageFromExtension(file.extension)
            predictiveProcess(client, mode, language!!, source, modify)
        } catch (e: ProcessCanceledException) {
            throw e
        } catch (e: UnauthorizedException) {
            logger.error("Unauthorized request. Configure the the API Key in the Preferences > Tools > CodeMaker AI menu.", e)
            throw e
        } catch (e: Exception) {
            logger.error("Failed to process file.", e)
        }
    }

    private fun processFile(client: Client, file: VirtualFile, mode: Mode, modify: Modify, codePath: String? = null, prompt: String? = null) {
        try {
            val source = readFile(file) ?: return

            val language = FileExtensions.languageFromExtension(file.extension)
            val output = process(client, mode, language!!, source, modify, codePath, prompt)

            writeFile(file, output)
        } catch (e: ProcessCanceledException) {
            throw e
        } catch (e: UnauthorizedException) {
            logger.error("Unauthorized request. Configure the the API Key in the Preferences > Tools > CodeMaker AI menu.", e)
            throw e
        } catch (e: Exception) {
            logger.error("Failed to process file.", e)
        }
    }

    private fun readFile(file: VirtualFile): String? {
        return ReadAction.nonBlocking(Callable<String> {
            val documentManager = PsiDocumentManager.getInstance(project)
            val psiFile = PsiManager.getInstance(project).findFile(file) ?: return@Callable null
            val document = documentManager.getDocument(psiFile) ?: return@Callable null
            return@Callable document.text
        }).executeSynchronously()
    }

    private fun writeFile(file: VirtualFile, output: String) {
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
    }

    private fun createProcessRequest(mode: Mode, language: Language, source: String, modify: Modify, codePath: String? = null, prompt: String? = null): ProcessRequest {
        return ProcessRequest(
                mode,
                language,
                Input(source),
                Options(modify, codePath, prompt, true)
        )
    }
}