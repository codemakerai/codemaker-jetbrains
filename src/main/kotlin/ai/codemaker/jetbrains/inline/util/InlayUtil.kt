package ai.codemaker.jetbrains.inline.util

import ai.codemaker.jetbrains.inline.render.CodemakerAutocompleteBlockElementRenderer
import ai.codemaker.jetbrains.inline.render.CodemakerAutocompleteElementRenderer
import ai.codemaker.jetbrains.inline.render.CodemakerAutocompleteSingleLineRenderer
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.util.Disposer
import kotlin.random.Random

class InlayUtil {

    companion object {

        /**
         * Get all autocomplete inlays in the editor.
         */
        fun getAllAutocompleteInlays(editor: Editor): List<Inlay<*>> {
            with(editor.inlayModel) {
                return listOf(
                        getInlineElementsInRange(0, editor.document.textLength, CodemakerAutocompleteElementRenderer::class.java),
                        getBlockElementsInRange(0, editor.document.textLength, CodemakerAutocompleteElementRenderer::class.java),
                        getAfterLineEndElementsInRange(0, editor.document.textLength, CodemakerAutocompleteElementRenderer::class.java)
                ).flatten()
            }
        }

        /**
         * Get the text of the inlay at the caret position.
         * We suppose there are at most one inline inlay and one block inlay at the caret position.
         * If it's a multi-line inlay, we return the text of the inline inlay and the block inlay.
         */
        fun getInlayTextAtCaret(editor: Editor): String? {
            val inlineText = editor.inlayModel.getInlineElementsInRange(maxOf(0, editor.caretModel.offset - 1), editor.caretModel.offset + 1)
                    .firstOrNull { it.renderer is CodemakerAutocompleteSingleLineRenderer }
                    ?.renderer?.let { (it as CodemakerAutocompleteSingleLineRenderer).text }
            val blockText = editor.inlayModel.getBlockElementsInRange(maxOf(0, editor.caretModel.offset - 1), editor.caretModel.offset + 1)
                    .firstOrNull { it.renderer is CodemakerAutocompleteBlockElementRenderer }
                    ?.renderer?.let { (it as CodemakerAutocompleteBlockElementRenderer).text }

            return when {
                inlineText != null && blockText != null -> "$inlineText\n$blockText"
                else -> inlineText ?: blockText
            }
        }

        fun clearAllAutocompleteInlays(editor: Editor) {
            getAllAutocompleteInlays(editor).forEach(Disposer::dispose)
        }
    }
}