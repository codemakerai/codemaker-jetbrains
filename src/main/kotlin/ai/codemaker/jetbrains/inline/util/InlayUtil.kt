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
        fun getAllAutocompleteInlays(editor: Editor): List<Inlay<*>> {
            with(editor.inlayModel) {
                return listOf(
                        getInlineElementsInRange(0, editor.document.textLength, CodemakerAutocompleteElementRenderer::class.java),
                        getBlockElementsInRange(0, editor.document.textLength, CodemakerAutocompleteElementRenderer::class.java),
                        getAfterLineEndElementsInRange(0, editor.document.textLength, CodemakerAutocompleteElementRenderer::class.java)
                ).flatten()
            }
        }

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

        // Test only
        fun complete(): String {
            val codeSnippets = listOf(
                    "int a = 5;", // Single-line
                    "String name = \"John Doe\";", // Single-line
                    """
    for (int i = 0; i < 10; i++) {
        System.out.println(i);
    }""", // Multi-line

                    """    if (x > 0) {
        return x;
    } else {
        return -x;
    }""", // Multi-line
                    "double radius = 2.5;\ndouble area = Math.PI * radius * radius;", // Multi-line
                    "List<String> names = new ArrayList<>();\n        names.add(\"Alice\");\n        names.add(\"Bob\");" // Multi-line
            )
            return codeSnippets[Random.nextInt(codeSnippets.size)]
        }
    }
}