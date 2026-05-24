package com.github.xepozz.spiral.router.index

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID

class RouterNamesIndex : AbstractRouterIndex() {
    companion object {
        val key = ID.create<String, RouterIndexType>("Spiral.Router.Names")
    }

    override fun getName() = key

    override fun getIndexer() = DataIndexer<String, RouterIndexType, FileContent> { inputData ->
        parseRoutes(inputData)
            .filter { it.name != null }
            .associateBy { it.name!! }
    }
}
