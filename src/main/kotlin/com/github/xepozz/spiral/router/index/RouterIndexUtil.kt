package com.github.xepozz.spiral.router.index

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.php.PhpIndexImpl
import com.jetbrains.php.lang.PhpLangUtil
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

object RouterIndexUtil {
    private val LOG = Logger.getInstance(RouterIndexUtil::class.java)

    /**
     * HTTP methods Spiral's router recognises. Spiral exposes the standard
     * RFC 7231 set plus PATCH; TRACE/CONNECT are intentionally omitted as they
     * are not part of the framework's documented routing surface.
     */
    val ALL_VERBS = listOf("GET", "POST", "PUT", "PATCH", "OPTIONS", "HEAD", "DELETE")

    // TODO: Handle method names provided as class constants, e.g. `UserController::DETAIL_ACTION`.
    fun parseMethods(element: PsiElement?): Collection<String> = when (element) {
        is ArrayCreationExpression -> element.children.flatMap { parseMethods(it) }
        is StringLiteralExpression -> listOf(parseContent(element))
        null -> ALL_VERBS
        else -> listOf(parseContent(element))
    }

    fun parseContent(element: PsiElement?) = when (element) {
        null -> ""
        is StringLiteralExpression -> StringUtil.unquoteString(element.contents)
        else -> StringUtil.unquoteString(element.text)
    }

    fun getAllRoutes(project: Project): Collection<Route> {
        if (DumbService.isDumb(project)) return emptyList()

        val fileBasedIndex = FileBasedIndex.getInstance()
        val allScope = GlobalSearchScope.allScope(project)

        return fileBasedIndex
            .getAllKeys(RouterUrlsIndex.key, project)
            .flatMap {
                fileBasedIndex
                    .getValues(RouterUrlsIndex.key, it, allScope)
            }
    }

    /**
     * Resolve a controller method given the FQN string stored on a [Route].
     *
     * The indexer captures `classMethod.fqn`, which uses '.' as the separator
     * between the containing class FQN and the method name (PhpStorm convention),
     * e.g. `\App\Controller\UserController.detail`.
     */
    fun getControllerMethodByFqn(project: Project, fqn: String): Collection<Method> {
        if (DumbService.isDumb(project)) return emptyList()

        val parts = fqn.split('.')
        if (parts.size != 2) {
            if (LOG.isDebugEnabled) {
                LOG.debug("Malformed controller FQN: '$fqn' (expected 'class.method')")
            }
            return emptyList()
        }
        val (className, methodName) = parts
        val phpIndex = PhpIndexImpl.getInstance(project)

        return phpIndex
            .getAnyByFQN(className)
            .mapNotNull { it.findMethodByName(methodName) }
    }

    /**
     * Convert an indexed method FQN (`\Foo\Bar.method`) into the canonical
     * presentation form used by PhpStorm UI (`Foo\Bar::method`).
     */
    fun toPresentableFqn(fqn: String): String = PhpLangUtil.toPresentableFQN(fqn.replace(".", "::"))
}
