/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.jetbrains.psi

import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.PsiTreeUtil

class PsiUtils {
    companion object {
        fun getMethod(psiFile: PsiFile, offset: Int): PsiMethod? {
            val element = psiFile.findElementAt(offset)
            val namedElement = PsiTreeUtil.getParentOfType(element, PsiNameIdentifierOwner::class.java, false) ?: return null

            if (!PsiMethod.isMethod(namedElement)) {
                return null
            }

            return PsiMethod(namedElement)
        }
    }
}