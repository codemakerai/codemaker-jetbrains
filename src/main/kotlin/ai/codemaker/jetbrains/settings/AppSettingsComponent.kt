/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.jetbrains.settings

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.*
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
    private val extendedSourceContextDepthCombo = ComboBox(arrayOf(1, 2, 3))
    private val assistantActionsEnabledCheck = JBCheckBox()
    private val syntaxAutocorrectionEnabledCheck = JBCheckBox()

    init {
        val createAccountLabel = ActionLink("Create account for free.") {
            BrowserUtil.browse("https://portal.codemaker.ai/#/register");
        }
        createAccountLabel.setExternalLinkIcon()

        panel = FormBuilder.createFormBuilder()
                .addComponent(createAccountLabel)
                .addSeparator()
                .addLabeledComponent(JBLabel("API Key: "), apiKeyText, 1, false)
                .addSeparator()
                .addLabeledComponent(JBLabel("Enable autocompletion: "), autocompletionEnabledCheck, 1, false)
                .addLabeledComponent(JBLabel("Enable multiline autocompletion: "), multilineAutocompletionEnabledCheck, 1, false)
                .addSeparator()
                .addLabeledComponent(JBLabel("Enable code actions: "), codeActionsEnabledCheck, 1, false)
                .addLabeledComponent(JBLabel("Enable predictive generation: "), predictiveGenerationEnabledCheck, 1, false)
                .addSeparator()
                .addLabeledComponent(JBLabel("Enable extended source context: "), extendedSourceContextEnabledCheck, 1, false)
                .addLabeledComponent(JBLabel("Extended source context depth: "), extendedSourceContextDepthCombo, 1, false)
                .addSeparator()
                .addLabeledComponent(JBLabel("Enable assistant contextual operations: "), assistantActionsEnabledCheck, 1, false)
                .addSeparator()
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

    var extendedSourceContextDepth: Int
        get() = extendedSourceContextDepthCombo.item
        set(item) {
            extendedSourceContextDepthCombo.item = item
        }

    var assistantActionsEnabled: Boolean
        get() = assistantActionsEnabledCheck.isSelected
        set(enabled) {
            assistantActionsEnabledCheck.isSelected = enabled
        }

    var syntaxAutocorrectionEnabled: Boolean
        get() = syntaxAutocorrectionEnabledCheck.isSelected
        set(enabled) {
            syntaxAutocorrectionEnabledCheck.isSelected = enabled
        }
}