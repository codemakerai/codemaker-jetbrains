/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.psi

import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.parentOfType

class PsiMethod(private val psiElement: PsiNameIdentifierOwner?) {

    companion object {
        val types = setOf(
                "com.intellij.psi.impl.source.PsiMethodImpl",
                "org.jetbrains.kotlin.psi.KtNamedFunction",
        )

        fun isMethod(typeName: String): Boolean {
            return types.contains(typeName)
        }
    }

    val name: String?
        get() {
            return getFullQualifiedName()
        }

    val codePath: String?
        get() {
            val name = getFullQualifiedName() ?: return null
            return "${name}(*)"
        }

    private fun getFullQualifiedName(): String? {
        val stack = ArrayDeque<String>()

        var element = psiElement
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