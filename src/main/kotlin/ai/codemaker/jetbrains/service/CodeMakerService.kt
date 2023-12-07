/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.jetbrains.service

import ai.codemaker.jetbrains.file.FileExtensions
import ai.codemaker.jetbrains.settings.AppSettingsState
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
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.intellij.util.ThrowableRunnable
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.Callable

@Service(Service.Level.PROJECT)
class CodeMakerService(private val project: Project) {

    private val maximumSourceGraphDepth = 16

    private val maximumSourceContextSize = 10

    private val logger = Logger.getInstance(CodeMakerService::class.java)

    private val client: Client = DefaultClient {
        instance.apiKey
    }

    fun generateCode(path: VirtualFile?, modify: Modify, codePath: String? = null) {
        process(Mode.CODE, "Generating code", path, modify, codePath)
    }

    fun generateSourceGraphCode(path: VirtualFile?) {
        processSourceGraph(Mode.CODE, "Generating source graph code", path)
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

    fun completion(path: VirtualFile, offset: Int, isMultilineAutocompletion: Boolean): String {
        try {
            val source = readFile(path) ?: return ""
            val language = FileExtensions.languageFromExtension(path.extension)

            val contextId = registerContext(client, language!!, source, path.path)

            val response = client.completion(createCompletionRequest(language!!, source, offset, isMultilineAutocompletion, contextId))

            return response.output.source;
        } catch (e: ProcessCanceledException) {
            throw e
        } catch (e: UnauthorizedException) {
            logger.error("Unauthorized request. Configure the the API Key in the Preferences > Tools > CodeMaker AI menu.", e)
            throw e
        } catch (e: Exception) {
            logger.error("Failed to complete code in file.", e)
            return ""
        }
    }

    fun predict(path: VirtualFile?) {
        runInBackground("Predictive generation") {
            try {
                walkFiles(path) { file: VirtualFile ->
                    if (file.isDirectory) {
                        return@walkFiles true
                    }

                    try {
                        predictFile(client, file)
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

    private fun processSourceGraph(mode: Mode, title: String, path: VirtualFile?) {
        runInBackground(title) {
            try {
                walkFiles(path) { file: VirtualFile ->
                    if (file.isDirectory) {
                        return@walkFiles true
                    }

                    try {
                        processSourceGraphFile(client, file, mode)
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
    private fun process(client: Client, mode: Mode, language: Language, source: String, modify: Modify, codePath: String?, prompt: String?, contextId: String?): String {
        val response = client.process(createProcessRequest(mode, language, source, modify, codePath, prompt, contextId))
        return response.output.source
    }

    @Throws(InterruptedException::class)
    private fun predictiveProcess(client: Client, language: Language, source: String, contextId: String?) {
        client.predict(createPredictRequest(language, source, contextId))
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

    private fun predictFile(client: Client, file: VirtualFile) {
        try {
            val source = readFile(file) ?: return
            val language = FileExtensions.languageFromExtension(file.extension)

            val contextId = registerContext(client, language!!, source, file.path)

            predictiveProcess(client, language!!, source, contextId)
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

            val contextId = registerContext(client, mode, language!!, source, file.path)

            val output = process(client, mode, language!!, source, modify, codePath, prompt, contextId)

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

    private fun processSourceGraphFile(client: Client, file: VirtualFile, mode: Mode, depth: Int = 0) {
        try {
            val language = FileExtensions.languageFromExtension(file.extension)
            val source = readFile(file) ?: return

            if (depth < maximumSourceGraphDepth) {
                val response = discoverContext(client, language!!, source, file.path)
                if (response.isRequiresProcessing) {
                    val paths = resolveContextPaths(response, file.path)
                    paths.forEach {
                        val dependantFile = VirtualFileManager.getInstance().findFileByNioPath(it) ?: return@forEach
                        processSourceGraphFile(client, dependantFile, Mode.CODE, depth + 1)
                    }
                }
            }

            val contextId = registerContext(client, mode, language!!, source, file.path)
            val output = process(client, mode, language!!, source, Modify.NONE, null, null, contextId)
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

    private fun registerContext(client: Client, mode: Mode, language: Language, source: String, path: String): String? {
        if (!isExtendedContextSupported(mode)) {
            return null
        }

        return registerContext(client, language, source, path)
    }

    private fun registerContext(client: Client, language: Language, source: String, path: String): String? {
        try {
            if (!AppSettingsState.instance.extendedSourceContextEnabled) {
                return null
            }

            val sourceContexts = resolveContextWithDepth(client, language, source, path, AppSettingsState.instance.extendedSourceContextDepth)

            val createContextResponse = client.createContext(CreateContextRequest())
            val contextId = createContextResponse.id

            client.registerContext(RegisterContextRequest(contextId, sourceContexts))
            return contextId
        } catch (e: Exception) {
            logger.warn("Failed to process file context.", e)
            return null
        }
    }

    private fun discoverContext(client: Client, language: Language, source: String, path: String): DiscoverContextResponse {
        return client.discoverContext(DiscoverContextRequest(Context(language, Input(source), path)))
    }

    private fun resolveContextPaths(discoverContextResponse: DiscoverContextResponse, path: String): List<Path> {
        val paths = discoverContextResponse.requiredContexts.map {
            Path.of(path).parent.resolve(it.path).normalize()
        }

        return paths.filter {
            Files.exists(it)
        }
    }

    private fun discoverContextPaths(client: Client, language: Language, source: String, path: String): List<Path> {
        val discoverContextResponse = discoverContext(client, language, source, path)
        return resolveContextPaths(discoverContextResponse, path)
    }

    private fun resolveContextWithDepth(client: Client, language: Language, source: String, path: String, maximumDepth: Int): List<Context> {
        val resolvedSourceContexts = ArrayList<Path>()

        val queue = LinkedList<Path>()
        queue.addAll(discoverContextPaths(client, language, source, path))
        var depth = 1
        var count = queue.size

        while (!queue.isEmpty() && resolvedSourceContexts.size < maximumSourceContextSize) {
            val child = queue.removeFirst()
            resolvedSourceContexts.add(child)

            if (depth + 1 <= maximumDepth) {
                queue.addAll(discoverContextPaths(client, language, source, child.toString()))
            }

            if (--count == 0) {
                count = queue.size
                depth++
            }
        }

        return resolvedSourceContexts.map {
            val file = VirtualFileManager.getInstance().findFileByNioPath(it) ?: return@map null
            val contextSource = readFile(file) ?: return@map null
            return@map Context(
                    language,
                    Input(contextSource),
                    it.toString(),
            )
        }.filterNotNull()
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

    private fun createProcessRequest(mode: Mode, language: Language, source: String, modify: Modify, codePath: String? = null, prompt: String? = null, contextId: String? = null): ProcessRequest {
        return ProcessRequest(
                mode,
                language,
                Input(source),
                Options(modify, codePath, prompt, true, false, contextId)
        )
    }

    private fun createPredictRequest(language: Language, source: String, contextId: String?): PredictRequest {
        return PredictRequest(
                language,
                Input(source),
                Options(null, null, null, false, false, contextId)
        )
    }

    private fun createCompletionRequest(language: Language, source: String, offset: Int, isMultilineAutocompletion: Boolean, contextId: String?): CompletionRequest {
        return CompletionRequest(
                language,
                Input(source),
                Options(null, "@$offset", null, false, isMultilineAutocompletion, contextId)
        )
    }

    private fun isExtendedContextSupported(mode: Mode): Boolean {
        return mode == Mode.CODE
                || mode == Mode.EDIT_CODE
                || mode == Mode.INLINE_CODE
    }
}