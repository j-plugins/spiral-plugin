package com.github.xepozz.spiral.router.index

import com.github.xepozz.spiral.SpiralFrameworkClasses
import com.github.xepozz.spiral.SpiralViewUtil
import com.github.xepozz.spiral.common.index.ObjectStreamDataExternalizer
import com.github.xepozz.spiral.common.references.AttributesUtil.getPsiArgument
import com.github.xepozz.spiral.index.AbstractIndex
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileContent
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.PhpAttribute

typealias RouterIndexType = Route

abstract class AbstractRouterIndex : AbstractIndex<RouterIndexType>() {
    /**
     * Version history:
     * - 1: initial.
     * - 2: added support for group parameter parsing.
     * - 3: split methods array into individual entries.
     * - 4: bump after switching the URLs index to per-route disambiguated keys
     *   so multiple routes on the same URI within a single file are preserved.
     */
    override fun getVersion() = 4

    override fun getValueExternalizer() = ObjectStreamDataExternalizer<RouterIndexType>()

    override fun getInputFilter() = FileBasedIndex.InputFilter {
        it.fileType == PhpFileType.INSTANCE && !it.name.endsWith(SpiralViewUtil.VIEW_SUFFIX)
    }

    /**
     * Parses every `#[Route(...)]` attribute on a `Method` in the file.
     *
     * Spiral's `@Route` signature is:
     *   `Route(string $uri, ?string $name = null, array|string $methods = '*', ?string $defaults = null, ?string $group = null)`
     * Hence the positional indices used below: `uri = 0`, `name = 1`, `methods = 2`, `group = 4`.
     * Named-argument resolution is handled by `AttributesUtil.getPsiArgument`.
     */
    protected fun parseRoutes(inputData: FileContent): List<Route> = inputData
        .psiFile
        .let { PsiTreeUtil.findChildrenOfType(it, PhpAttribute::class.java) }
        .filter { it.fqn == SpiralFrameworkClasses.ROUTE }
        .filter { it.arguments.isNotEmpty() }
        .mapNotNull { attribute ->
            val classMethod = attribute.owner as? Method
            if (classMethod == null) {
                if (LOG.isDebugEnabled) {
                    LOG.debug("@Route attribute on non-method owner in ${inputData.file.path}; skipping")
                }
                return@mapNotNull null
            }

            val uri = RouterIndexUtil.parseContent(attribute.getPsiArgument("uri", 0))
            if (uri.isEmpty()) {
                if (LOG.isDebugEnabled) {
                    LOG.debug("@Route with empty uri at ${classMethod.fqn}; skipping")
                }
                return@mapNotNull null
            }
            val name = RouterIndexUtil.parseContent(attribute.getPsiArgument("name", 1))
            val methods = RouterIndexUtil.parseMethods(attribute.getPsiArgument("methods", 2))
            val group = RouterIndexUtil.parseContent(attribute.getPsiArgument("group", 4))

            Route(
                uri = uri,
                name = name.takeIf { it.isNotEmpty() },
                methods = methods,
                fqn = classMethod.fqn,
                group = group.takeIf { it.isNotEmpty() },
            )
        }

    private companion object {
        val LOG = Logger.getInstance(AbstractRouterIndex::class.java)
    }
}
