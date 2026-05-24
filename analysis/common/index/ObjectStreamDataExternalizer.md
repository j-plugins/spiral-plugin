# Analysis: common/index/ObjectStreamDataExternalizer.kt

## Summary
A custom DataExternalizer that uses Java object serialization (ObjectOutputStream/ObjectInputStream) to persist arbitrary objects to the index. The implementation wraps serialized bytes with a length prefix for correct deserialization.

## Issues

### 1. [INDEX] Silent swallowing of ClassNotFoundException and ClassCastException
- Location: ObjectStreamDataExternalizer.kt:36-40
- Problem: The `read()` method catches `ClassNotFoundException` and `ClassCastException` in an empty catch block, returning `null` silently. This masks structural mismatches between index versions and can hide bugs if deserialization fails due to class schema changes.
- Why it matters: Silent null return on deserialization failure may cause downstream code to process incomplete or missing data without warning. Index value class structural changes should be detected and cause an explicit version bump, but this swallowing makes such issues invisible.
- Suggested fix: At minimum, log the exception at WARN level before returning null. Alternatively, consider throwing an exception to force developers to bump the index version. Example: `Logger.getInstance(ObjectStreamDataExternalizer::class.java).warn("Failed to deserialize index value", ignored)`.

### 2. [INDEX] Fragile Java serialization across IDE/plugin version updates
- Location: ObjectStreamDataExternalizer.kt:14-43
- Problem: Using `ObjectOutputStream` for index values is inherently fragile. Per the CLAUDE.md checklist (line 32), "Value externalizer using ObjectStreamDataExternalizer (Java serialization) is fragile across IDE/plugin versions — prefer DataExternalizer<T> with explicit save/read." This class implements exactly the pattern warned against.
- Why it matters: Any change to a serialized class's structure (field addition, removal, or reordering) will break deserialization in previously-indexed files. Unlike explicit binary serialization, there is no forward/backward compatibility story.
- Suggested fix: Review each index that uses this externalizer and consider switching to explicit `DataExternalizer<T>` with hand-written `save`/`read` methods. This gives fine-grained control and makes version compatibility intentions explicit.

### 3. [API-MISUSE] Unchecked cast to T after deserialization
- Location: ObjectStreamDataExternalizer.kt:37
- Problem: `input.readObject() as T` assumes the deserialized object is of type T. If the persisted data is stale or corrupted, the cast may succeed but return a wrong type, leading to ClassCastException at use-site instead of at deserialization.
- Why it matters: Errors are deferred and harder to diagnose. The catch block at line 39 swallows such exceptions, compounding the problem.
- Suggested fix: Explicitly check the type before casting: `val obj = input.readObject(); if (obj is T) { object = obj } else { /* log and return null */ }`.

## Severity summary
Java serialization in indexes is a known architectural debt. These issues aren't new bugs but codify patterns that CLAUDE.md explicitly discourages.
