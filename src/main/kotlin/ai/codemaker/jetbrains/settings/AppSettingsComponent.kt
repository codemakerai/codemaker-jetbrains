/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.jetbrains.settings

import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JPanel

class AppSettingsComponent {
    val panel: JPanel
    private val apiKeyText = JBTextField()

    init {
        panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(JBLabel("API Key: "), apiKeyText, 1, false)
                .addComponentFillVertically(JPanel(), 0)
                .panel
    }

    var apiKey: String?
        get() = apiKeyText.text
        set(apiKey) {
            apiKeyText.text = apiKey
        }
}