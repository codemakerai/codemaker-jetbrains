package ai.codemaker.jetbrains.settings

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito

class AppSettingsStateTest {

    @Mock
    private val parentApplication = Mockito.mock<Disposable>()

    @Mock
    private val application = Mockito.mock<Application>()

    private val settings = AppSettingsState()

    @BeforeEach
    fun setUp() {
        ApplicationManager.setApplication(application, parentApplication)
        Mockito.`when`(application.getService(Mockito.eq(AppSettingsState::class.java))).thenReturn(settings)
    }

    @Test
    fun testInstance() {

        // when
        val instance = AppSettingsState.instance

        // then
        assertNull(instance.apiKey)
        assertTrue(instance.codeActionsEnabled)
        assertFalse(instance.predictiveGenerationEnabled)
    }
}