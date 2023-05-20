/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.jetbrains.file

import ai.codemaker.sdk.client.model.Language

class FileExtensions {

    companion object {
        private val languageByExtension = hashMapOf(
                "js" to Language.JAVASCRIPT,
                "jsx" to Language.JAVASCRIPT,
                "java" to Language.JAVA,
                "kt" to Language.KOTLIN,
        )

        fun isSupported(extension: String?): Boolean {
            return languageByExtension.contains(extension)
        }

        fun languageFromExtension(extensions: String?): Language? {
            return languageByExtension[extensions]
        }
    }
}