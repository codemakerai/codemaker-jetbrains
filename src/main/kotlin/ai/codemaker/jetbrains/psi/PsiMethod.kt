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
                "com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptFunctionImpl",
                "com.goide.psi.impl.GoFunctionDeclarationImpl",
                "com.goide.psi.impl.GoMethodDeclarationImpl",
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
        if (psiElement.javaClass.name == "com.goide.psi.impl.GoMethodDeclarationImpl") {
            return ReceiverElement(psiElement)
        }

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

    private class ReceiverElement(psiElement: PsiNameIdentifierOwner) : IdentifiableElement {
        override val fullyQualifiedName = this.elementFullyQualifiedName(psiElement)

        private fun elementFullyQualifiedName(psiElement: PsiNameIdentifierOwner): String? {
            val typeName = getTypeName(psiElement) ?: return null

            val builder = StringBuilder()
            builder.append(typeName)
            builder.append(".")
            builder.append(psiElement.name!!)
            return builder.toString()
        }

        private fun getTypeName(psiElement: PsiNameIdentifierOwner): String? {
            return try {
                val element = DynamicType(psiElement)
                var type = element.flatMap("getReceiverType")

                if (type.isInstanceOf("com.goide.psi.GoPointerType")) {
                    type = type.flatMap("getType")
                }

                if (type.isEmpty()) {
                    return null
                }
                return type.map("getText") as String?
            } catch (e: Exception) {
                null
            }
        }
    }

    private class DynamicType(private val reference: Any?) {

        fun isEmpty(): Boolean {
            return reference == null
        }

        fun map(method: String): Any? {
            reference ?: return null
            val value = try {
                val methodRef = reference.javaClass.getMethod(method)
                methodRef.invoke(reference)
            } catch (e: NoSuchMethodException) {
                null
            }
            return value
        }

        fun flatMap(method: String): DynamicType {
            reference ?: return DynamicType(null)
            val reference = try {
                val methodRef = reference.javaClass.getMethod(method)
                methodRef.invoke(reference)
            } catch (e: NoSuchMethodException) {
                null
            }
            return DynamicType(reference)
        }

        fun isInstanceOf(type: String): Boolean {
            reference ?: return false
            return reference.javaClass.interfaces.any { it.name == type }
        }
    }
}