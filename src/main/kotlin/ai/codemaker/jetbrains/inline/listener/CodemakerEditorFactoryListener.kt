package ai.codemaker.jetbrains.inline.listener

import ai.codemaker.jetbrains.inline.render.CodemakerAutocompleteBlockElementRenderer
import ai.codemaker.jetbrains.inline.render.CodemakerAutocompleteSingleLineRenderer
import ai.codemaker.jetbrains.inline.util.InlayUtil
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.*
import com.intellij.openapi.wm.IdeFocusManager

class CodemakerEditorFactoryListener : EditorFactoryListener {

    private val codemakerCaretListener by lazy { CodemakerCaretListener() }
    private val codemakerDocumentListener by lazy { CodemakerDocumentListener() }

    override fun editorCreated(event: EditorFactoryEvent) {
        event.editor.apply {
            document.addDocumentListener(codemakerDocumentListener)
            caretModel.addCaretListener(codemakerCaretListener)
        }
    }

    override fun editorReleased(event: EditorFactoryEvent) {
        event.editor.apply {
            caretModel.removeCaretListener(codemakerCaretListener)
            document.removeDocumentListener(codemakerDocumentListener)
        }
    }

    private inner class CodemakerCaretListener : CaretListener {

        override fun caretPositionChanged(event: CaretEvent) {
            if (!isSingleOffsetChange(event)) {
                event.editor.let { editor ->
                    InlayUtil.clearAllAutocompleteInlays(editor)
                }
            }
        }

        private fun isSingleOffsetChange(event: CaretEvent): Boolean =
                event.oldPosition.line == event.newPosition.line &&
                        event.oldPosition.column + 1 == event.newPosition.column
    }

    private inner class CodemakerDocumentListener : BulkAwareDocumentListener {

        override fun documentChangedNonBulk(event: DocumentEvent) {
            getActiveEditor(event.document)?.let { editor ->
                val newFragment = event.newFragment.toString()
                val currentTextInlay = InlayUtil.getInlayTextAtCaret(editor)

                InlayUtil.clearAllAutocompleteInlays(editor)
                if (newFragment.isNotEmpty() && currentTextInlay?.startsWith(newFragment) == true) {
                    val newCompletion = currentTextInlay.substring(newFragment.length)
                    displayAutoComplete(editor, event.offset + event.newLength, newCompletion)
                    return
                }
                val changeOffset = event.offset + event.newLength
                displayAutoComplete(editor, changeOffset, InlayUtil.complete())
            }
        }

        private fun getActiveEditor(document: Document): Editor? {
            if (!ApplicationManager.getApplication().isDispatchThread) {
                return null
            }

            val focusOwner = IdeFocusManager.getGlobalInstance().focusOwner
            val dataContext = DataManager.getInstance().getDataContext(focusOwner)
            val activeEditor = CommonDataKeys.EDITOR.getData(dataContext)

            return if (activeEditor?.document == document) activeEditor else null
        }

        private fun displayAutoComplete(editor: Editor, offset: Int, suggestion: String) {
            InlayUtil.clearAllAutocompleteInlays(editor)

            if (suggestion.isEmpty()) {
                return
            }

            var lines = suggestion.split("\n")
            var isMultiLine = lines.size > 1

            var firstLine = lines[0]
            if (firstLine.isNotEmpty()) {
                editor.inlayModel.addInlineElement(offset, true, CodemakerAutocompleteSingleLineRenderer(firstLine, editor))
            }

            if (isMultiLine) {
                var multiLines = lines.subList(1, lines.size).joinToString("\n")
                editor.inlayModel.addBlockElement(offset, true, false, Int.MAX_VALUE, CodemakerAutocompleteBlockElementRenderer(multiLines, editor))
            }
        }
    }
}
