# Analysis: src/main/kotlin/com/github/xepozz/spiral/router/endpoints/SpiralGroup.kt

## Summary
A data class representing a group of route endpoints, holding a reference to the project, the group name, and a collection of endpoints.

## Issues

### 1. [LEAK] Project reference in data class
- Location: SpiralGroup.kt:5-9
- Problem: Holding a direct `Project` reference in a data class that may be retained across dynamic plugin reloads or project close events. This can prevent proper garbage collection.
- Why it matters: Per IntelliJ Platform best practices, project references should be held only in service/component contexts with proper lifecycle management. Long-lived data objects shouldn't hold project references.
- Suggested fix: Remove `project` from the data class and pass it as a parameter when needed in the provider methods, or use a lazy-initialized `ProjectManager` lookup.
