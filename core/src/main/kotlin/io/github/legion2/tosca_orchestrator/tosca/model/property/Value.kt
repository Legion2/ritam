package io.github.legion2.tosca_orchestrator.tosca.model.property

sealed class Value {
    data class String(val value: kotlin.String) : Value()
    data class Boolean(val value: kotlin.Boolean) : Value()
    data class Float(val value: Double) : Value()
    data class Integer(val value: Int) : Value()
    data class Map(val value: kotlin.collections.Map<kotlin.String, Value>) : Value()
    data class List(val value: kotlin.collections.List<Value>) : Value()
    object Null : Value()
}

/**
 * The raw value represented by this Value
 */
val Value.rawValue: Any?
    get() = when (this) {
        is Value.String -> value
        is Value.Boolean -> value
        is Value.Float -> value
        is Value.Integer -> value
        is Value.Map -> value.mapValues { it.value.rawValue }
        is Value.List -> value.map { it.rawValue }
        Value.Null -> null
    }

fun fromRawValue(rawValue: Any?): Value {
    return when (rawValue) {
        is List<Any?> -> Value.List(rawValue.map { fromRawValue(it) })
        is Map<*, Any?> -> Value.Map(rawValue.mapValues { fromRawValue(it.value) }.mapKeys { it.key as String })
        is String -> Value.String(rawValue)
        is Boolean -> Value.Boolean(rawValue)
        is Int -> Value.Integer(rawValue)
        is Double -> Value.Float(rawValue)
        null -> Value.Null
        else -> throw IllegalArgumentException("Can not convert raw Value: $rawValue")
    }
}
