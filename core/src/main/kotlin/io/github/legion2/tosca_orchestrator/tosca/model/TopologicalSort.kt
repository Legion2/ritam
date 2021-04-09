package io.github.legion2.tosca_orchestrator.tosca.model

import java.util.*

private enum class Mark {
    NO_MARK,
    TEMPORARY_MARK,
    PERMANENT_MARK
}

private class DependencyLoopException(val loop: List<String> = listOf()) :
    IllegalArgumentException("Loop in the dependency graph detected: $loop")

fun <K, T> topologicalSort(map: Map<K, T>, dependencies: (T) -> Iterable<K>): LinkedHashMap<K, T> {
    val marks = map.mapValues { Mark.NO_MARK }.toMutableMap()
    val result = linkedMapOf<K, T>()
    fun visit(key: K, value: T) {
        when (marks.getValue(key)) {
            Mark.PERMANENT_MARK -> return
            Mark.TEMPORARY_MARK -> throw DependencyLoopException()
            Mark.NO_MARK -> {
                marks[key] = Mark.TEMPORARY_MARK
                dependencies(value).forEach {
                    val dep = map[it]
                    if (dep != null) {
                        try {
                            visit(it, dep)
                        } catch (e: DependencyLoopException) {
                            throw DependencyLoopException(e.loop + it.toString())
                        }
                    }
                }
                marks[key] = Mark.PERMANENT_MARK
                result[key] = value
            }
        }
    }
    for (node in map.entries) {
        visit(node.key, node.value)
    }
    return result
}
