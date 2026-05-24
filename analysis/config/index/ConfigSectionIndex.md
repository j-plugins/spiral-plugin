# Analysis: src/main/kotlin/com/github/xepozz/spiral/config/index/ConfigSectionIndex.kt

## Summary
File-based index of all PHP classes extending `InjectableConfig`, mapping them by FQN.

## Issues

### 1. [INDEX] Value type duplicates key
- Location: ConfigSectionIndex.kt:14, 35
- Problem: Value type `ConfigSectionIndexType = String` and indexer stores FQN as both key and value.
- Why it matters: Wasted disk and parsing overhead.
- Suggested fix: Either use `Void` index (FileBasedIndex stores only keys) or change value type to a meaningful payload (e.g. class declaration offset).

### 2. [DUMB-MODE] No dumb-mode guard documented for callers
- Location: ConfigSectionIndex.kt companion / util
- Problem: Index access methods do not document the dumb-mode contract for callers.
- Why it matters: `FileBasedIndex.getValues/getAllKeys` throws during indexing. Callers from UI/reference contributors must guard with `DumbService` or `runReadActionInSmartMode`.
- Suggested fix: Add KDoc or wrap queries in a smart-mode helper.
