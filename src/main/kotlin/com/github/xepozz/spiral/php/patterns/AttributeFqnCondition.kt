package com.github.xepozz.spiral.php.patterns

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PropertyPatternCondition
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.jetbrains.php.lang.psi.elements.PhpAttribute
import org.jetbrains.annotations.NonNls

class AttributeFqnCondition<T : PhpAttribute>(namePattern: ElementPattern<String>) :
    PropertyPatternCondition<T?, String?>("withFqn", namePattern) {
    override fun getPropertyValue(o: Any): String? {
        return if (o is PhpAttribute) o.fqn else null
    }
}