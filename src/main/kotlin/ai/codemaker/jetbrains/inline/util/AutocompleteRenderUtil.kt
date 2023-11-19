package ai.codemaker.jetbrains.inline.util

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.FontInfo
import com.intellij.openapi.editor.markup.TextAttributes
import java.awt.Font
import kotlin.math.ceil

object AutocompleteRenderUtil {

    fun fontYOffset(font: Font, editor: Editor): Double {
        val metrics =
                FontInfo.getFontMetrics(font, FontInfo.getFontRenderContext(editor.contentComponent))
        val fontBaseline =
                font.createGlyphVector(metrics.fontRenderContext, "Hello world!").visualBounds.height
        val linePadding = (editor.lineHeight - fontBaseline) / 2
        return ceil(fontBaseline + linePadding)
    }

    fun getTextAttributesForEditor(editor: Editor): TextAttributes =
            try {
                editor.colorsScheme.getAttributes(
                        DefaultLanguageHighlighterColors.INLAY_TEXT_WITHOUT_BACKGROUND)
            } catch (ignored: Exception) {
                editor.colorsScheme.getAttributes(DefaultLanguageHighlighterColors.INLINE_PARAMETER_HINT)
            }

}
