package com.github.xepozz.spiral.views

import com.github.xepozz.spiral.SpiralIcons
import com.github.xepozz.spiral.SpiralViewUtil
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlTagNameProvider

class ComponentTagNameProvider : XmlTagNameProvider {
    override fun addTagNameVariants(
        elements: MutableList<LookupElement>,
        tag: XmlTag,
        prefix: String?
    ) {
        val project = tag.project

        val result = mutableListOf<String>()
        FilenameIndex.processAllFileNames(
            {
                if (it.startsWith("x-") && it.endsWith(SpiralViewUtil.VIEW_SUFFIX)) {
                    result.add(it.removeSuffix(SpiralViewUtil.VIEW_SUFFIX))
                }

                true
            },
            GlobalSearchScope.projectScope(project),
            null,
        )

        result
            .distinct()
            .map {
                LookupElementBuilder.create(it)
                    .withIcon(SpiralIcons.SPIRAL)
                    .withTypeText("Spiral Component")
            }
            .apply { elements.addAll(this) }
    }
}