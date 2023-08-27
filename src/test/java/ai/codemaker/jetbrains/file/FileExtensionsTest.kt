package ai.codemaker.jetbrains.file

import ai.codemaker.sdk.client.model.Language
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FileExtensionsTest {

    @Test
    fun testJavaFileExtensionIsSupported() {

        // when
        assertTrue(FileExtensions.isSupported("java"))
    }

    @Test
    fun testKotlinFileExtensionIsSupported() {

        // when
        assertTrue(FileExtensions.isSupported("kt"))
    }

    @Test
    fun testCSharpFileExtensionIsSupported() {

        // when
        assertTrue(FileExtensions.isSupported("cs"))
    }

    @Test
    fun testJavaScriptFileExtensionIsSupported() {

        // when
        assertTrue(FileExtensions.isSupported("js"))
        assertTrue(FileExtensions.isSupported("jsx"))
    }

    @Test
    fun testTypeScriptFileExtensionIsSupported() {

        // when
        assertTrue(FileExtensions.isSupported("ts"))
        assertTrue(FileExtensions.isSupported("tsx"))
    }

    @Test
    fun testGoFileExtensionIsSupported() {

        // when
        assertTrue(FileExtensions.isSupported("go"))
    }

    @Test
    fun testJavaLanguageFromExtension() {

        // when
        assertEquals(Language.JAVA, FileExtensions.languageFromExtension("java"));
    }

    @Test
    fun testKotlinLanguageFromExtension() {

        // when
        assertEquals(Language.KOTLIN, FileExtensions.languageFromExtension("kt"));
    }

    @Test
    fun testCSharpLanguageFromExtension() {

        // when
        assertEquals(Language.CSHARP, FileExtensions.languageFromExtension("cs"));
    }

    @Test
    fun testJavaScriptLanguageFromExtension() {

        // when
        assertEquals(Language.JAVASCRIPT, FileExtensions.languageFromExtension("js"));
        assertEquals(Language.JAVASCRIPT, FileExtensions.languageFromExtension("jsx"));
    }

    @Test
    fun testTypeScriptLanguageFromExtension() {

        // when
        assertEquals(Language.TYPESCRIPT, FileExtensions.languageFromExtension("ts"));
        assertEquals(Language.TYPESCRIPT, FileExtensions.languageFromExtension("tsx"));
    }

    @Test
    fun testGoLanguageFromExtension() {

        // when
        assertEquals(Language.GO, FileExtensions.languageFromExtension("go"));
    }
}