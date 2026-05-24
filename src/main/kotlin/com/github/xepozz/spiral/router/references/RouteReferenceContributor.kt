package com.github.xepozz.spiral.router.references

import com.github.xepozz.spiral.SpiralFrameworkClasses
import com.github.xepozz.spiral.common.references.AttributesUtil.getPsiArgument
import com.github.xepozz.spiral.php.patterns.AttributeFqnCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression
import com.jetbrains.php.lang.psi.elements.ParameterList
import com.jetbrains.php.lang.psi.elements.PhpAttribute
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

/**
 * Registers PSI references for string literals inside `#[Route(...)]` attribute
 * parameter lists.
 *
 * Two patterns are required because the `methods:` argument may be passed either
 * as a plain string or as an array literal — the PSI parent depth differs:
 *   - Direct string:   StringLiteral -> ParameterList -> PhpAttribute   (super-parent depth 2)
 *   - Array element:   StringLiteral -> ArrayCreationExpression -> ParameterList -> PhpAttribute (depth 4)
 *
 * Expected `@Route` signature (see [SpiralFrameworkClasses.ROUTE]):
 *   `Route(string $uri, ?string $name = null, array|string $methods = '*', ?string $defaults = null, ?string $group = null)`
 * The positional indices used below match this signature.
 */
class RouteReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        // Direct string argument: StringLiteral -> ParameterList -> PhpAttribute
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression::class.java)
                .withParent(PlatformPatterns.psiElement(ParameterList::class.java))
                .withSuperParent(
                    2,
                    PlatformPatterns.psiElement(PhpAttribute::class.java)
                        .with(AttributeFqnCondition(StandardPatterns.string().oneOf(SpiralFrameworkClasses.ROUTE)))
                ),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<out PsiReference> {
                    val literal = element as? StringLiteralExpression ?: return PsiReference.EMPTY_ARRAY
                    val attribute = literal.parent?.parent as? PhpAttribute ?: return PsiReference.EMPTY_ARRAY
                    return referenceFor(literal, attribute)
                }
            }
        )

        // String inside an array (currently only `methods: ['GET', 'POST']`):
        //   StringLiteral -> ArrayHashElement(?) -> ArrayCreationExpression -> ParameterList -> PhpAttribute
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression::class.java)
                .withSuperParent(2, ArrayCreationExpression::class.java)
                .withSuperParent(3, ParameterList::class.java)
                .withSuperParent(
                    4,
                    PlatformPatterns.psiElement(PhpAttribute::class.java)
                        .with(AttributeFqnCondition(StandardPatterns.string().oneOf(SpiralFrameworkClasses.ROUTE)))
                ),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<out PsiReference> {
                    val literal = element as? StringLiteralExpression ?: return PsiReference.EMPTY_ARRAY
                    val arrayCreation = literal.parent?.parent as? ArrayCreationExpression
                        ?: return PsiReference.EMPTY_ARRAY
                    val attribute = arrayCreation.parent?.parent as? PhpAttribute
                        ?: return PsiReference.EMPTY_ARRAY

                    return if (arrayCreation == attribute.getPsiArgument("methods", 2)) {
                        arrayOf(RouterMethodsReference(literal))
                    } else {
                        PsiReference.EMPTY_ARRAY
                    }
                }
            }
        )
    }

    private fun referenceFor(literal: StringLiteralExpression, attribute: PhpAttribute): Array<out PsiReference> {
        return when (literal) {
            attribute.getPsiArgument("uri", 0) -> arrayOf(RouterUriReference(literal))
            attribute.getPsiArgument("name", 1) -> arrayOf(RouterNameReference(literal))
            attribute.getPsiArgument("methods", 2) -> arrayOf(RouterMethodsReference(literal))
            attribute.getPsiArgument("group", 4) -> arrayOf(RouterGroupReference(literal))
            else -> PsiReference.EMPTY_ARRAY
        }
    }
}
