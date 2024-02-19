/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.jetbrains.file

import ai.codemaker.sdkv2.client.model.Language

class FileExtensions {

    companion object {
        private val languageByExtension = hashMapOf(
                "c" to Language.C,
                "cpp" to Language.CPP,
                "js" to Language.JAVASCRIPT,
                "jsx" to Language.JAVASCRIPT,
                "php" to Language.PHP,
                "java" to Language.JAVA,
                "cs" to Language.CSHARP,
                "go" to Language.GO,
                "kt" to Language.KOTLIN,
                "ts" to Language.TYPESCRIPT,
                "tsx" to Language.TYPESCRIPT,
                "rs" to Language.RUST,
        )

        fun isSupported(extension: String?): Boolean {
            return languageByExtension.contains(extension)
        }

        fun languageFromExtension(extensions: String?): Language? {
            return languageByExtension[extensions]
        }
    }
}