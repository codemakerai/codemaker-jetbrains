/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.inline.completion

import ai.codemaker.jetbrains.inline.listener.CodemakerEditorFactoryListener
import ai.codemaker.jetbrains.service.CodeMakerService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.function.Consumer
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class CompletionProvider {

    companion object {

        private val completionDelay = 300.toDuration(DurationUnit.MILLISECONDS)

        private val logger = Logger.getInstance(CompletionProvider::class.java)

        private var pendingRequest: Job? = null

        fun completion(service: CodeMakerService, file: VirtualFile, offset: Int, isMultiLineAllowed: Boolean, consumer: Consumer<String> ) {
            cancel()
            pendingRequest = GlobalScope.launch {
                delay(completionDelay)
                val completion = service.completion(file, offset, isMultiLineAllowed)
                logger.info("completion: $completion")
                consumer.accept(completion)
            }
        }

        fun cancel() {
            pendingRequest?.cancel()
            pendingRequest = null
        }
    }
}