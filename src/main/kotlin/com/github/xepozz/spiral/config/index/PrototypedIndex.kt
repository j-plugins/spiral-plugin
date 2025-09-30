package com.github.xepozz.spiral.config.index

import com.github.xepozz.spiral.index.AbstractIndex
import com.github.xepozz.spiral.SpiralFrameworkClasses
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.EnumeratorStringDescriptor
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression
import com.jetbrains.php.lang.psi.elements.ClassConstantReference
import com.jetbrains.php.lang.psi.elements.ClassReference
import com.jetbrains.php.lang.psi.elements.PhpAttribute
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import com.jetbrains.php.lang.psi.elements.impl.ClassConstImpl

private typealias PrototypedIndexType = String

class PrototypedIndex : AbstractIndex<PrototypedIndexType>() {
    companion object {
        val key = ID.create<String, PrototypedIndexType>("Spiral.Prototyped")

        fun getAll(project: Project): Map<String, String> {
            val scope = GlobalSearchScope.allScope(project)

            val prototypes = getPrototypes(project)

            val fileBasedIndex = FileBasedIndex.getInstance()

            return prototypes
                .map { it to (fileBasedIndex.getValues(key, it, scope).firstOrNull() ?: "") }
                .associate { it }
//            fileBasedIndex.getValues(key, prototypes, GlobalSearchScope.allScope(project))
        }

        fun getPrototypes(project: Project): Collection<String> {
            val fileBasedIndex = FileBasedIndex.getInstance()

            return fileBasedIndex.getAllKeys(key, project)
        }

        fun getPrototypeClass(prototype: String, project: Project): PrototypedIndexType? {
            val fileBasedIndex = FileBasedIndex.getInstance()

            return fileBasedIndex
                .getValues(key, prototype, GlobalSearchScope.allScope(project))
                .firstOrNull()
        }
    }

    override fun getVersion() = 2

    override fun getName() = key

    override fun getValueExternalizer() = EnumeratorStringDescriptor.INSTANCE

    override fun getInputFilter() = FileBasedIndex.InputFilter { it.fileType == PhpFileType.INSTANCE }

    override fun getIndexer() = DataIndexer<String, PrototypedIndexType, FileContent> { inputData ->
        val psiFile = inputData.psiFile

        val classes = PsiTreeUtil.findChildrenOfType(psiFile, PhpClass::class.java)
        val prototypeBootloaderClass = classes.find { it.fqn == SpiralFrameworkClasses.PROTOTYPE_BOOTLOADER }
        if (prototypeBootloaderClass != null) {
            val predefinedShortcuts = PsiTreeUtil.findChildrenOfType(prototypeBootloaderClass, ClassConstImpl::class.java)
                .firstOrNull { it.name == "DEFAULT_SHORTCUTS" }
            if (predefinedShortcuts != null) {
                val content = predefinedShortcuts.defaultValue as? ArrayCreationExpression
                if (content != null) {
                    val hashElements = content.hashElements

                    val result = mutableMapOf<String, PrototypedIndexType>()

                    for (elem in hashElements) {
                        val key = elem.key as? StringLiteralExpression ?: continue
                        val value = elem.value as? ClassConstantReference ?: continue
                        val classReference = value.classReference as? ClassReference ?: continue

                        result[key.contents] = classReference.fqn.toString()
                    }

//                    println("predefinedShortcuts: $result")
                    return@DataIndexer result
                }
            }
        }

        psiFile
            .let { PsiTreeUtil.findChildrenOfType(it, PhpAttribute::class.java) }
            .filter { it.fqn == SpiralFrameworkClasses.PROTOTYPED }
            .mapNotNull { attribute ->
                attribute.arguments
                    .firstOrNull { it.name == "property" || it.name.isEmpty() }
                    ?.argument
                    ?.value to attribute.owner as? PhpClass
            }
            .filter { it.second != null && !it.first.isNullOrEmpty() }
            .map { StringUtil.unquoteString(it.first!!) to it.second!!.fqn }
            .associate { it }
//            .associate { it.first to it.second }
    }

}