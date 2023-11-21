package ai.codemaker.jetbrains.inline.listener

import ai.codemaker.jetbrains.inline.render.CodemakerAutocompleteBlockElementRenderer
import ai.codemaker.jetbrains.inline.render.CodemakerAutocompleteSingleLineRenderer
import ai.codemaker.jetbrains.inline.util.InlayUtil
import ai.codemaker.jetbrains.service.CodeMakerService
import ai.codemaker.jetbrains.settings.AppSettingsState
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.*
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.psi.PsiDocumentManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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

        private val logger = Logger.getInstance(CodemakerDocumentListener::class.java)

        override fun documentChangedNonBulk(event: DocumentEvent) {
            if (!AppSettingsState.instance.autocompletionEnabled) {
                return
            }

            getActiveEditor(event.document)?.let { editor ->
                val newFragment = event.newFragment.toString()
                val currentTextInlay = InlayUtil.getInlayTextAtCaret(editor)

                InlayUtil.clearAllAutocompleteInlays(editor)
                // If what user types is matched with the completion, update the completion with remaining text
                if (newFragment.isNotEmpty() && currentTextInlay?.startsWith(newFragment) == true) {
                    val newCompletion = currentTextInlay.substring(newFragment.length)
                    displayAutoComplete(editor, event.offset + event.newLength, newCompletion)
                    return
                }
                val changeOffset = event.offset + event.newLength

                val project = editor.project ?: return
                val service: CodeMakerService = project.getService(CodeMakerService::class.java)
                val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(event.document)
                val virtualFile = psiFile?.virtualFile ?: return

                // TODO: Add logics to check if auto complete should be triggered like:
                // 1. If the file is supported
                // 2. If the cursor is not in the middle of a line

                val multilineAutocompletionEnabled = AppSettingsState.instance.multilineAutocompletionEnabled

                // TODO: Add cancellation token(debounce) like vscode extension, if user types too fast, cancel the previous request
                // Using Coroutines to avoid blocking the UI thread
                GlobalScope.launch {
                    // TODO: response is always empty, need to fix
                    val completion = service.completion(virtualFile, event.offset, multilineAutocompletionEnabled)
                    logger.info("completion: $completion")
                    ApplicationManager.getApplication().invokeLater {
                        displayAutoComplete(editor, changeOffset, completion)
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
