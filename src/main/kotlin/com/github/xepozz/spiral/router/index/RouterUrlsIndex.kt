package com.github.xepozz.spiral.router.index

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID

class RouterUrlsIndex : AbstractRouterIndex() {
    companion object {
        val key = ID.create<String, RouterIndexType>("Spiral.Router.Urls")
    }

    override fun getName() = key

    override fun getIndexer() = DataIndexer<String, RouterIndexType, FileContent> { inputData ->
        // Disambiguate the key by appending the route's positional index so that
        // multiple routes sharing the same URI within a single file (e.g. GET /x
        // and POST /x on different methods) are not collapsed by associateBy.
        // Callers iterate all keys via FileBasedIndex.getAllKeys + getValues, so
        // the literal key shape is not used as a direct lookup.
        parseRoutes(inputData)
            .withIndex()
            .associate { (i, route) -> "${route.uri}#$i" to route }
    }
}
