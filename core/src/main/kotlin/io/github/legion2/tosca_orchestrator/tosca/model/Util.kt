package io.github.legion2.tosca_orchestrator.tosca.model

fun <K, V, V2> Map<K, V>.refineExisting(map: Map<K, V2>, action: V.(V2) -> V): Map<K, V> {
    val notExisting = map - this.keys
    if (notExisting.isNotEmpty()) {
        throw IllegalArgumentException("Only existing elements can be refined, but additional refinements for non existing elements where provided: $notExisting")
    }

    return mapValues {
        val refinement = map[it.key]
        if (refinement != null) {
            it.value.action(refinement)
        } else it.value
    }
}

fun <K, V, V2> Map<K, V>.refineOrAdd(map: Map<K, V2>, refine: V.(V2) -> V, create: (V2) -> V): Map<K, V> {
    val refined = this.toMutableMap()
    map.forEach { (key, refinement) ->
        refined.compute(key) { _, element ->
            element?.refine(refinement) ?: create(refinement)
        }
    }
    return refined
}

fun <K, V, V2> Map<K, V>.addNew(map: Map<K, V2>, create: (V2) -> V): Map<K, V> {
    val redefined = keys.intersect(map.keys)
    if (redefined.isNotEmpty()) {
        throw IllegalArgumentException("Redefinition not allowed: $redefined")
    }

    return this + map.mapValues { create(it.value) }
}

fun <K, V, V2, V3> Map<K, V>.combine(map: Map<K, V2>, combine: (K, V?, V2?) -> V3): Map<K, V3> {
    return (this.keys + map.keys).associateWith {
        combine(it, this[it], map[it])
    }
}

inline fun <R, T : R> Result<T>.addExceptionContextInfo(onFailure: (exception: Throwable) -> String): R {
    return getOrElse { throw RuntimeException(onFailure(it), it) }
}
