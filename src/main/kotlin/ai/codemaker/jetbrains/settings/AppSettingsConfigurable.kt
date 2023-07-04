/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.jetbrains.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

class AppSettingsConfigurable : Configurable {

    private var settingsComponent: AppSettingsComponent? = null

    override fun getDisplayName(): String {
        return "CodeMaker AI"
    }

    override fun createComponent(): JComponent? {
        settingsComponent = AppSettingsComponent()
        return settingsComponent!!.panel
    }

    override fun disposeUIResources() {
        settingsComponent = null
    }

    override fun isModified(): Boolean {
        val settings = AppSettingsState.instance
        return settingsComponent!!.apiKey != settings.apiKey
                && settingsComponent!!.predictiveGenerationEnabled != settings.predictiveGenerationEnabled
    }

    override fun apply() {
        val settings = AppSettingsState.instance
        settings.apiKey = settingsComponent!!.apiKey
        settings.predictiveGenerationEnabled = settingsComponent!!.predictiveGenerationEnabled
    }

    override fun reset() {
        val settings = AppSettingsState.instance
        settingsComponent!!.apiKey = settings.apiKey
        settingsComponent!!.predictiveGenerationEnabled = settings.predictiveGenerationEnabled
    }
}