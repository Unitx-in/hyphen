package com.unitx.hyphen_android.extension

import android.util.Log

inline fun <reified T : Any> T.logState(
    tag: String = (T::class.simpleName ?: "Unknown") + "State",
    noinline filter: (T) -> Boolean = { true }
) {
    if (!filter(this)) return
    val sb = StringBuilder()
    this.javaClass.declaredFields.forEach { field ->
        field.isAccessible = true
        val line = when (val value = field.get(this)) {
            is Collection<*> -> "${field.name}.size = ${value.size}"
            is Map<*, *>     -> "${field.name}.size = ${value.size}"
            is Array<*>      -> "${field.name}.size = ${value.size}"
            else             -> "${field.name} = $value"
        }
        sb.appendLine("    $line")
    }
    Log.d(tag, sb.toString().trimEnd())
}

inline fun <reified T : Any> T.logLists(
    tag: String = (T::class.simpleName ?: "Unknown") + "Lists",
    noinline filter: (T) -> Boolean = { true }
) {
    if (!filter(this)) return
    val sb = StringBuilder()
    this.javaClass.declaredFields.forEach { field ->
        field.isAccessible = true
        when (val value = field.get(this)) {
            is Collection<*> -> sb.appendLine("    ${field.name}.size = ${value.size} | hash=${value.hashCode()}")
            is Map<*, *>     -> sb.appendLine("    ${field.name}.size = ${value.size} | hash=${value.hashCode()}")
            is Array<*>      -> sb.appendLine("    ${field.name}.size = ${value.size} | hash=${value.hashCode()}")
            else             -> { /* skip non-collections */ }
        }
    }
    if (sb.isEmpty()) {
        Log.d(tag, "(no collection fields)")
    } else {
        Log.d(tag, sb.toString().trimEnd())
    }
}

val previousStates = mutableMapOf<Class<*>, Any>()

inline fun <reified T : Any> T.logDiff(
    tag: String = (T::class.simpleName ?: "Unknown") + "Diff",
    noinline filter: (T) -> Boolean = { true }
) {
    if (!filter(this)) return

    val previous = previousStates[T::class.java]
    if (previous == this) return
    previousStates[T::class.java] = this

    if (previous == null) return

    val sb = StringBuilder()
    this.javaClass.declaredFields.forEach { field ->
        field.isAccessible = true
        val newValue = field.get(this)
        val oldValue = field.get(previous)
        if (newValue != oldValue) {
            val line = when (newValue) {
                is Collection<*> -> "${field.name}.size = ${(oldValue as? Collection<*>)?.size} -> ${newValue.size}"
                is Map<*, *>     -> "${field.name}.size = ${(oldValue as? Map<*, *>)?.size} -> ${newValue.size}"
                is Array<*>      -> "${field.name}.size = ${(oldValue as? Array<*>)?.size} -> ${newValue.size}"
                else             -> "${field.name} = $oldValue -> $newValue"
            }
            sb.appendLine("    $line")
        }
    }

    if (sb.isEmpty()) return
    Log.d(tag, sb.toString().trimEnd())
}

fun clearStateLog() {
    previousStates.clear()
}

fun clearStateLog(clazz: Class<*>) {
    previousStates.remove(clazz)
}

data class DiffEntry(val fieldName: String, val oldValue: Any?, val newValue: Any?)
data class ItemDiff<T>(val index: Int, val item: T, val changes: List<DiffEntry>)

val previousListStates = mutableMapOf<Class<*>, List<*>>()

inline fun <reified T : Any> List<T>.logDiffList(
    tag: String = (T::class.simpleName ?: "Unknown") + "ListDiff",
    noinline primaryKey: ((T) -> Any?)? = null,
    noinline filter: (T) -> Boolean = { true }
): List<ItemDiff<T>> {
    @Suppress("UNCHECKED_CAST")
    val previous = previousListStates[T::class.java] as? List<T>
    if (previous == this) {
        Log.d(tag, "Same list reference, no changes.")
        return emptyList()
    }
    previousListStates[T::class.java] = this
    if (previous == null) return emptyList()

    val diffs = if (primaryKey != null) {
        val prevMap = previous.associateBy { primaryKey(it) }
        val sb = StringBuilder()
        var hasAdded = false

        val result = this.mapIndexedNotNull { index, current ->
            if (!filter(current)) return@mapIndexedNotNull null
            val key = primaryKey(current)
            val prev = prevMap[key]

            if (prev == null) {
                sb.appendLine("  Added[$index]: key=$key")
                hasAdded = true
                return@mapIndexedNotNull null
            }

            val changes = T::class.java.declaredFields.mapNotNull { field ->
                field.isAccessible = true
                val newValue = field.get(current)
                val oldValue = field.get(prev)
                if (newValue != oldValue) {
                    when (newValue) {
                        is Collection<*> -> DiffEntry("${field.name}.size", (oldValue as? Collection<*>)?.size, newValue.size)
                        is Map<*, *>     -> DiffEntry("${field.name}.size", (oldValue as? Map<*, *>)?.size, newValue.size)
                        is Array<*>      -> DiffEntry("${field.name}.size", (oldValue as? Array<*>)?.size, newValue.size)
                        else             -> DiffEntry(field.name, oldValue, newValue)
                    }
                } else null
            }
            if (changes.isEmpty()) null else ItemDiff(index, current, changes)
        }

        // detect removals
        val currentKeys = this.map { primaryKey(it) }.toHashSet()
        previous.forEachIndexed { index, item ->
            if (primaryKey(item) !in currentKeys) {
                sb.appendLine("  Removed[$index]: key=${primaryKey(item)}")
                hasAdded = true
            }
        }

        result.also { itemDiffs ->
            itemDiffs.forEach { itemDiff ->
                sb.appendLine("  Item[${itemDiff.index}] (key=${primaryKey(itemDiff.item)}):")
                itemDiff.changes.forEach { sb.appendLine("    ${it.fieldName} = ${it.oldValue} -> ${it.newValue}") }
            }
            if (hasAdded || itemDiffs.isNotEmpty()) {
                Log.d(tag, sb.toString().trimEnd())
            } else {
                Log.d(tag, "Same content, no changes.")
            }
        }
    } else {
        // original index-based diffing
        this.mapIndexedNotNull { index, current ->
            val prev = previous.getOrNull(index) ?: return@mapIndexedNotNull null
            if (!filter(current)) return@mapIndexedNotNull null

            val changes = T::class.java.declaredFields.mapNotNull { field ->
                field.isAccessible = true
                val newValue = field.get(current)
                val oldValue = field.get(prev)
                if (newValue != oldValue) {
                    when (newValue) {
                        is Collection<*> -> DiffEntry("${field.name}.size", (oldValue as? Collection<*>)?.size, newValue.size)
                        is Map<*, *>     -> DiffEntry("${field.name}.size", (oldValue as? Map<*, *>)?.size, newValue.size)
                        is Array<*>      -> DiffEntry("${field.name}.size", (oldValue as? Array<*>)?.size, newValue.size)
                        else             -> DiffEntry(field.name, oldValue, newValue)
                    }
                } else null
            }.also { if (it.isEmpty()) return@mapIndexedNotNull null }

            ItemDiff(index, current, changes)
        }.also { diffs ->
            if (diffs.isNotEmpty()) {
                val sb = StringBuilder()
                diffs.forEach { itemDiff ->
                    sb.appendLine("  Item[${itemDiff.index}]:")
                    itemDiff.changes.forEach { sb.appendLine("    ${it.fieldName} = ${it.oldValue} -> ${it.newValue}") }
                }
                Log.d(tag, sb.toString().trimEnd())
            } else {
                Log.d(tag, "Same content, no changes.")
            }
        }
    }

    return diffs
}