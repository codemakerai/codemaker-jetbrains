package ai.codemaker.jetbrains.inline.listener

import ai.codemaker.jetbrains.file.FileExtensions
import ai.codemaker.jetbrains.inline.completion.CompletionProvider
import ai.codemaker.jetbrains.inline.render.CodemakerAutocompleteBlockElementRenderer
import ai.codemaker.jetbrains.inline.render.CodemakerAutocompleteSingleLineRenderer
import ai.codemaker.jetbrains.inline.util.InlayUtil
import ai.codemaker.jetbrains.service.CodeMakerService
import ai.codemaker.jetbrains.settings.AppSettingsState
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.*
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.psi.PsiDocumentManager

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

    /**
     * Clear all autocomplete inlays when cursor moves
     */
    private inner class CodemakerCaretListener : CaretListener {

        override fun caretPositionChanged(event: CaretEvent) {
            if (!isSingleOffsetChange(event)) {
                event.editor.let { editor ->
                    CompletionProvider.cancel()
                    InlayUtil.clearAllAutocompleteInlays(editor)
                }
            }
        }

        // Typing a character will trigger CaretEvent, we don't want to clear inlays in this case, because what user types might be matched with the completion
        private fun isSingleOffsetChange(event: CaretEvent): Boolean =
                event.oldPosition.line == event.newPosition.line &&
                        event.oldPosition.column + 1 == event.newPosition.column
    }

    /**
     * Display autocomplete inlays when user types
     */
    private inner class CodemakerDocumentListener : BulkAwareDocumentListener {

        override fun documentChangedNonBulk(event: DocumentEvent) {
            if (!AppSettingsState.instance.autocompletionEnabled) {
                return
            }

            if (event.isWholeTextReplaced) {
                return
            }

            val newFragment = event.newFragment.toString()
            if (newFragment.isBlank()) {
                return
            }

            getActiveEditor(event.document)?.let { editor ->
                val currentTextInlay = InlayUtil.getInlayTextAtCaret(editor)

                val offset = event.offset
                val insertOffset = offset + event.newLength

                InlayUtil.clearAllAutocompleteInlays(editor)
                // If what user types is matched with the completion, update the completion with remaining text
                if (newFragment.isNotEmpty() && currentTextInlay?.startsWith(newFragment) == true) {
                    val newCompletion = currentTextInlay.substring(newFragment.length)
                    displayAutoComplete(editor, insertOffset, newCompletion)
                    return
                }


                val project = editor.project ?: return
                val service: CodeMakerService = project.getService(CodeMakerService::class.java)
                val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(event.document)
                val virtualFile = psiFile?.virtualFile ?: return

                if (!FileExtensions.isSupported(virtualFile.extension)) {
                    return
                }

                val isMultiLineAllowed = AppSettingsState.instance.multilineAutocompletionEnabled
                CompletionProvider.completion(service, virtualFile, offset, isMultiLineAllowed) { completion ->
                    ApplicationManager.getApplication().invokeLater {
                        displayAutoComplete(editor, insertOffset , completion)
                    }
                }
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

        /**
         * For multi-line suggestion, due to the way the editor paint the inlay, we need to use one single line inlay and one block inlay.
         * The single line inlay will be displayed first with the first line of the completion.
         * The block inlay will be displayed after the single line inlay with the rest of the completion.
         */
        private fun displayAutoComplete(editor: Editor, offset: Int, suggestion: String) {
            InlayUtil.clearAllAutocompleteInlays(editor)

            if (suggestion.isEmpty()) {
                return
            }

            val lines = suggestion.split("\n")
            val isMultiLine = lines.size > 1

            val firstLine = lines[0]
            if (firstLine.isNotEmpty()) {
                editor.inlayModel.addInlineElement(offset, true, CodemakerAutocompleteSingleLineRenderer(firstLine, editor))
            }

            if (isMultiLine) {
                val multiLines = lines.subList(1, lines.size).joinToString("\n")
                editor.inlayModel.addBlockElement(offset, true, false, Int.MAX_VALUE, CodemakerAutocompleteBlockElementRenderer(multiLines, editor))
            }
        }
    }
}
