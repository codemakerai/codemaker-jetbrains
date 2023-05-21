/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.ui

class UIUtils {

    companion object {
        private const val MAXIMUM_LENGTH = 20

        fun displayName(name: String?): String? {
            if (name == null) {
                return null
            } else if (name.length <= MAXIMUM_LENGTH) {
                return name
            }

            return "${name.substring(0, MAXIMUM_LENGTH)}..."
        }
    }
}