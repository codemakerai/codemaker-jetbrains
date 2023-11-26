package ai.codemaker.jetbrains.action

import ai.codemaker.jetbrains.inline.render.CodemakerAutocompleteBlockElementRenderer
import ai.codemaker.jetbrains.inline.render.CodemakerAutocompleteSingleLineRenderer
import ai.codemaker.jetbrains.inline.util.InlayUtil
import com.intellij.codeInsight.hint.HintManagerImpl
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorAction
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler

object AcceptInlineCompletionAction :
        EditorAction(AcceptInlineCompletionHandler()),
        HintManagerImpl.ActionToIgnore {

    class AcceptInlineCompletionHandler : EditorWriteActionHandler() {

        private val logger = Logger.getInstance(AcceptInlineCompletionHandler::class.java)

        // Fall back to the default implementation if there is no inline completion
        override fun isEnabledForCaret(editor: Editor, caret: Caret, dataContext: DataContext?): Boolean {
            return InlayUtil.getAllAutocompleteInlays(editor).isNotEmpty()
        }

        override fun executeWriteAction(editor: Editor, caret: Caret?, dataContext: DataContext) {
            if (InlayUtil.getAllAutocompleteInlays(editor).isNotEmpty()) {
                acceptSuggestion(editor)
            }
        }

        private fun acceptSuggestion(editor: Editor) {
            ApplicationManager.getApplication().runWriteAction {
                try {
                    val document = editor.document
                    val offset = editor.caretModel.offset
                    val inlays = InlayUtil.getAllAutocompleteInlays(editor)

                    val completion = buildString {
                        val singleLineInlay = inlays.firstOrNull { it.renderer is CodemakerAutocompleteSingleLineRenderer }
                        val blockInlay = inlays.firstOrNull { it.renderer is CodemakerAutocompleteBlockElementRenderer }

                        singleLineInlay?.let {
                            append((it.renderer as CodemakerAutocompleteSingleLineRenderer).text)
                        }
                        blockInlay?.let {
                            append("\n")
                            append((it.renderer as CodemakerAutocompleteBlockElementRenderer).text)
                        }
                    }

                    InlayUtil.clearAllAutocompleteInlays(editor)
                    document.insertString(offset, completion)
                    editor.caretModel.moveToOffset(offset + completion.length)
                } catch (e: Exception) {
                    logger.error("Failed to accept inline completion", e)
                }
            }
        }
    }
}
