package ai.codemaker.jetbrains.inline.render

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.editor.markup.TextAttributes
import ai.codemaker.jetbrains.inline.util.AutocompleteRenderUtil
import java.awt.Font

abstract class CodemakerAutocompleteElementRenderer(
        val text: String,
        val editor: Editor,
) : EditorCustomElementRenderer {

    protected val themeAttributes: TextAttributes

    init {
        themeAttributes = AutocompleteRenderUtil.getTextAttributesForEditor(editor)
    }

    override fun calcWidthInPixels(inlay: Inlay<*>): Int {
        val editor = inlay.editor as EditorImpl
        return editor.getFontMetrics(Font.PLAIN).stringWidth(text)
    }

    protected val font: Font
        get() {
            val editorFont = editor.colorsScheme.getFont(EditorFontType.PLAIN)
            return editorFont.deriveFont(Font.ITALIC) ?: editorFont
        }

    protected fun fontYOffset(): Int = AutocompleteRenderUtil.fontYOffset(font, editor).toInt()

}