/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.jetbrains.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "ai.codemaker.jetbrains.settings.AppSettingsState", storages = [Storage("SdkSettingsPlugin.xml")])
class AppSettingsState : PersistentStateComponent<AppSettingsState> {

    @JvmField
    var apiKey: String? = null

    @JvmField
    var model: String? = null

    @JvmField
    var codeActionsEnabled: Boolean = true

    @JvmField
    var autocompletionEnabled: Boolean = false

    @JvmField
    var multilineAutocompletionEnabled: Boolean = false

    @JvmField
    var predictiveGenerationEnabled: Boolean = false

    @JvmField
    var extendedSourceContextEnabled: Boolean = false

    @JvmField
    var extendedSourceContextDepth: Int = 1

    @JvmField
    var assistantActionsEnabled: Boolean = true

    @JvmField
    var syntaxAutocorrectionEnabled: Boolean = false

    override fun getState(): AppSettingsState {
        return this
    }

    override fun loadState(state: AppSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        @JvmStatic
        val instance: AppSettingsState
            get() = ApplicationManager.getApplication().getService(AppSettingsState::class.java)
    }
}