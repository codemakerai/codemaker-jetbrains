/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.parentOfType

class PsiMethod(psiElement: PsiNameIdentifierOwner) {

    private val element: IdentifiableElement = createIdentifiableElement(psiElement)

    companion object {
        private val TYPES = setOf(
                "com.intellij.psi.impl.source.PsiMethodImpl",
                "org.jetbrains.kotlin.psi.KtNamedFunction",
                "com.intellij.lang.javascript.psi.impl.JSFunctionImpl",
                "com.goide.psi.impl.GoFunctionDeclarationImpl",
        )

        fun isMethod(element: PsiElement): Boolean {
            return TYPES.contains(element.javaClass.name)
        }
    }

    val name: String?
        get() {
            return element.fullyQualifiedName
        }

    val codePath: String?
        get() {
            val name = element.fullyQualifiedName ?: return null
            return "${name}(*)"
        }

    private fun createIdentifiableElement(psiElement: PsiNameIdentifierOwner): IdentifiableElement {
        return NamedElement(psiElement)
    }

    private interface IdentifiableElement {
        val fullyQualifiedName: String?
    }

    private class NamedElement(psiElement: PsiNameIdentifierOwner) : IdentifiableElement {

        override val fullyQualifiedName = this.elementFullyQualifiedName(psiElement)

        private fun elementFullyQualifiedName(psiElement: PsiNameIdentifierOwner): String? {
            val stack = ArrayDeque<String>()

            var element: PsiNameIdentifierOwner? = psiElement
            while (element != null && element.name != null) {
                stack.addLast(element.name!!)
                element = element.parentOfType()
            }

            if (stack.isEmpty()) {
                return null
            }

            val builder = StringBuilder()
            while (!stack.isEmpty()) {
                if (builder.isNotEmpty()) {
                    builder.append('.')
                }
                builder.append(stack.removeLast())
            }
            return builder.toString()
        }
    }
}