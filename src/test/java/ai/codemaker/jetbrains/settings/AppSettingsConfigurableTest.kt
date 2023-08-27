package ai.codemaker.jetbrains.settings

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock

class AppSettingsConfigurableTest {

    private val parentApplication = mock<Disposable>()

    private val application = mock<Application>()

    private val settings = AppSettingsState()

    private var instance: AppSettingsConfigurable = AppSettingsConfigurable()

    @BeforeEach
    fun setUp() {
        ApplicationManager.setApplication(application, parentApplication)
        Mockito.`when`(application.getService(Mockito.eq(AppSettingsState::class.java))).thenReturn(settings)
        instance.createComponent()
        instance.apply()
    }

    @Test
    fun testDisplayName() {

        // when
        assertEquals("CodeMaker AI", instance.displayName)
    }

    @Test
    fun testConfigurableIsNotModifiedByDefault() {

        // when
        assertFalse(instance.isModified)
    }

    @Test
    fun testConfigurableIsModifiedOnApiKeyChange() {

        // when
        settings.apiKey = "api-key"

        // the
        assertTrue(instance.isModified)
    }

    @Test
    fun testConfigurableIsModifiedOnCodeActionEnabled() {

        // when
        settings.codeActionsEnabled = true

        // the
        assertTrue(instance.isModified)
    }

    @Test
    fun testConfigurableIsModifiedOnPredictiveCodeGenerationEnabled() {

        // when
        settings.predictiveGenerationEnabled = true

        // the
        assertTrue(instance.isModified)
    }

    @Test
    fun testConfigurableIsModifiedOnAllSettingChanged() {

        // when
        settings.apiKey = "api-key"
        settings.codeActionsEnabled = true
        settings.predictiveGenerationEnabled = true

        // the
        assertTrue(instance.isModified)
    }
}