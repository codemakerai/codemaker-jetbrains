/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.jetbrains.file

import ai.codemaker.sdkv2.client.model.Language

class FileExtensions {

    companion object {
        private val languageByExtension = hashMapOf(
                "js" to Language.JAVASCRIPT,
                "jsx" to Language.JAVASCRIPT,
                "ts" to Language.TYPESCRIPT,
                "tsx" to Language.TYPESCRIPT,
                "java" to Language.JAVA,
                "kt" to Language.KOTLIN,
                "cs" to Language.CSHARP,
                "go" to Language.GO,
        )

        fun isSupported(extension: String?): Boolean {
            return languageByExtension.contains(extension)
        }

        fun languageFromExtension(extensions: String?): Language? {
            return languageByExtension[extensions]
        }
    }
}