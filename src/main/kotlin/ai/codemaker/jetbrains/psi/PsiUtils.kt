/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.psi

import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiQualifiedNamedElement
import com.intellij.psi.util.PsiTreeUtil

class PsiUtils {
    companion object {
        fun getNamedElement(psiFile: PsiFile, offset: Int): PsiNamedElement? {
            val element = psiFile.findElementAt(offset)
            val namedElement = PsiTreeUtil.getParentOfType(element, PsiNameIdentifierOwner::class.java) ?: return null

            if (namedElement is PsiQualifiedNamedElement) {
                return null
            }

            return PsiNamedElement(namedElement)
        }
    }
}