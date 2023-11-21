package ai.codemaker.jetbrains.inline.render

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.markup.TextAttributes

class CodemakerAutocompleteSingleLineRenderer(
    text: String,
    editor: Editor
) : CodemakerAutocompleteElementRenderer(text, editor) {

    override fun paint(
            inlay: Inlay<*>, g:
            java.awt.Graphics,
            r: java.awt.Rectangle,
            textAttributes: TextAttributes) {
        val font = font
        g.font = font
        g.color = themeAttributes.foregroundColor
        g.drawString(text, r.x, r.y + fontYOffset())
    }
}