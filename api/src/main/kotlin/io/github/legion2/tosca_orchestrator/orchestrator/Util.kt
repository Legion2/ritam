package io.github.legion2.tosca_orchestrator.orchestrator

import arrow.optics.Lens
import kotlinx.coroutines.delay

suspend fun Process.await(): Int {
    try {
        while (isAlive) {
            delay(100)
        }
    } finally {
        if (isAlive) {
            destroyForcibly()
        }
    }
    return exitValue()
}

fun <K, V> Map<K, V?>.filterNullValues(): Map<K, V> {
    return this.filterValues { it != null } as Map<K, V>
}

fun <S, T> Lens<S, Set<T>>.add(source: S, element: T) =
        modify(source) { it + element }

fun <S, T> Lens<S, Set<T>>.remove(source: S, element: T) =
        modify(source) { it - element }

fun <S, K, V> Lens<S, Map<K, V>>.put(source: S, key: K, value: V) = modify(source) { it + (key to value) }

fun <S, K, V> Lens<S, Map<K, V>>.removeKey(source: S, key: K) = modify(source) { it - key }

