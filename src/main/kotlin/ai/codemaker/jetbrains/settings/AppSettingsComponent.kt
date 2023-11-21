/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.jetbrains.settings

import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JPanel

class AppSettingsComponent {
    val panel: JPanel
    private val apiKeyText = JBTextField()
    private val codeActionsEnabledCheck = JBCheckBox()
    private val autocompletionEnabledCheck = JBCheckBox()
    private val multilineAutocompletionEnabledCheck = JBCheckBox()
    private val predictiveGenerationEnabledCheck = JBCheckBox()
    private val extendedSourceContextEnabledCheck = JBCheckBox()
    private val syntaxAutocorrectionEnabledCheck = JBCheckBox()

    init {
        panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(JBLabel("API Key: "), apiKeyText, 1, false)
                .addLabeledComponent(JBLabel("Enable code actions: "), codeActionsEnabledCheck, 1, false)
                .addLabeledComponent(JBLabel("Enable autocompletion: "), autocompletionEnabledCheck, 1, false)
                .addLabeledComponent(JBLabel("Enable multiline autocompletion: "), multilineAutocompletionEnabledCheck, 1, false)
                .addLabeledComponent(JBLabel("Enable predictive generation: "), predictiveGenerationEnabledCheck, 1, false)
                .addLabeledComponent(JBLabel("Enable extended source context: "), extendedSourceContextEnabledCheck, 1, false)
                .addLabeledComponent(JBLabel("Enable syntax autocorrection: "), syntaxAutocorrectionEnabledCheck, 1, false)
                .addComponentFillVertically(JPanel(), 0)
                .panel
    }

    var apiKey: String?
        get() = apiKeyText.text.trim()
        set(apiKey) {
            apiKeyText.text = apiKey
        }

    var codeActionsEnabled: Boolean
        get() = codeActionsEnabledCheck.isSelected
        set(enabled) {
            codeActionsEnabledCheck.isSelected = enabled
        }

    var autocompletionEnabled: Boolean
        get() = autocompletionEnabledCheck.isSelected
        set(enabled) {
            autocompletionEnabledCheck.isSelected = enabled
        }

    var multilineAutocompletionEnabled: Boolean
        get() = multilineAutocompletionEnabledCheck.isSelected
        set(enabled) {
            multilineAutocompletionEnabledCheck.isSelected = enabled
        }

    var predictiveGenerationEnabled: Boolean
        get() = predictiveGenerationEnabledCheck.isSelected
        set(enabled) {
            predictiveGenerationEnabledCheck.isSelected = enabled
        }

    var extendedSourceContextEnabled: Boolean
        get() = extendedSourceContextEnabledCheck.isSelected
        set(enabled) {
            extendedSourceContextEnabledCheck.isSelected = enabled
        }

    var syntaxAutocorrectionEnabled: Boolean
        get() = syntaxAutocorrectionEnabledCheck.isSelected
        set(enabled) {
            syntaxAutocorrectionEnabledCheck.isSelected = enabled
        }
}