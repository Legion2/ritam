package io.github.legion2.tosca_orchestrator.tosca.state

import io.github.legion2.tosca_orchestrator.tosca.definitions.ConstraintClause
import io.github.legion2.tosca_orchestrator.tosca.model.property.*
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedProperty

interface EvaluationContext {
    fun evaluate(resolvedExpression: ResolvedExpression): Value
}

internal fun EvaluationContext.validateAndPopulatePropertyValue(value: Value, validationInfo: ValidationInfo): Value {
    val populatedValue = validateAndPopulateValueBasedOnPrimitiveType(value, validationInfo)
    validateValueBasedOnConstraints(value, validationInfo.constraints)
    return populatedValue
}

private fun validateValueBasedOnConstraints(value: Value, constraints: List<ConstraintClause>) {
    constraints.forEach {
        //println(it)
        //TODO
    }
}

private fun EvaluationContext.validateAndPopulateValueBasedOnPrimitiveType(
    value: Value,
    validationInfo: ValidationInfo
): Value {
    return when (validationInfo.primitiveType) {
        PrimitiveType.COMPLEX -> {
            if (value !is Value.Map) {
                throw IllegalArgumentException()
            }
            Value.Map(getValues(value.value, validationInfo.properties))
        }
        PrimitiveType.STRING -> {
            if (value !is Value.String) {
                throw IllegalArgumentException()
            }
            value
        }
        PrimitiveType.INTEGER -> {
            if (value !is Value.Integer) {
                throw IllegalArgumentException()
            }
            value
        }
        PrimitiveType.FLOAT -> {
            if (value !is Value.Float) {
                throw IllegalArgumentException()
            }
            value
        }
        PrimitiveType.BOOLEAN -> {
            if (value !is Value.Boolean) {
                throw IllegalArgumentException()
            }
            value
        }
        PrimitiveType.TIMESTAMP -> {
            if (value !is Value.String) {
                throw IllegalArgumentException()
            }
            value
        }
        PrimitiveType.NULL -> {
            if (value != Value.Null) {
                throw IllegalArgumentException()
            }
            value
        }
        PrimitiveType.VERSION -> {
            if (value !is Value.String) {
                throw IllegalArgumentException()
            }
            value
        }
        PrimitiveType.RANGE -> {
            if (value !is Value.String) {
                throw IllegalArgumentException()
            }
            value
        }
        PrimitiveType.LIST -> {
            validationInfo.entry_schema ?: throw IllegalArgumentException()
            if (value !is Value.List) {
                throw IllegalArgumentException()
            }
            val entryValidationInfo = validationInfo.entry_schema.toValidationInfo()
            Value.List(value.value.map {
                validateAndPopulatePropertyValue(it, entryValidationInfo)
            })
        }
        PrimitiveType.MAP -> {
            validationInfo.entry_schema ?: throw IllegalArgumentException()
            if (value !is Value.Map) {
                throw IllegalArgumentException("The value '$value' is not a Map")
            }
            val entryValidationInfo = validationInfo.entry_schema.toValidationInfo()
            Value.Map(value.value.mapValues {
                validateAndPopulatePropertyValue(it.value, entryValidationInfo)
            })
        }
        PrimitiveType.SCALAR_UNIT_SIZE -> {
            if (value !is Value.String) {
                throw IllegalArgumentException()
            }
            val (scalar, unit) = parseUnit(value.value)
            if (!sizeUnits.contains(unit)) {
                throw IllegalArgumentException()
            }
            value
        }
        PrimitiveType.SCALAR_UNIT_TIME -> {
            if (value !is Value.String) {
                throw IllegalArgumentException()
            }
            val (scalar, unit) = parseUnit(value.value)
            if (!timeUnits.contains(unit)) {
                throw IllegalArgumentException()
            }
            value
        }
        PrimitiveType.SCALAR_UNIT_FREQUENCY -> {
            if (value !is Value.String) {
                throw IllegalArgumentException()
            }
            val (scalar, unit) = parseUnit(value.value)
            if (!frequencyUnits.contains(unit)) {
                throw IllegalArgumentException()
            }
            value
        }
        PrimitiveType.SCALAR_UNIT_BITRATE -> {
            if (value !is Value.String) {
                throw IllegalArgumentException()
            }
            val (scalar, unit) = parseUnit(value.value)
            if (!bitrateUnits.contains(unit)) {
                throw IllegalArgumentException()
            }
            value
        }
    }
}

private fun EvaluationContext.getValues(
    values: Map<String, Value>,
    properties: Map<String, ResolvedProperty>
): Map<String, Value> {
    val populatedValues = properties.mapValues { (propertyName, resolvedProperty) ->
        validateAndPopulatePropertyValue(values[propertyName], resolvedProperty)
    }
    val unknownProperties = values - populatedValues.keys
    if (unknownProperties.isNotEmpty()) {
        throw IllegalArgumentException("Property values were provided but not defined: $unknownProperties")
    }

    return populatedValues.filterValues { it != null } as Map<String, Value>
}

private fun EvaluationContext.validateAndPopulatePropertyValue(
    providedValue: Value?,
    resolvedProperty: ResolvedProperty
): Value? {
    val value = providedValue ?: resolvedProperty.default?.let { evaluate(it) }
    return if (value == null) {
        if (resolvedProperty.required) {
            throw IllegalArgumentException("No value provided for required property")
        }
        null
    } else {
        validateAndPopulatePropertyValue(value, resolvedProperty.toValidationInfo())
    }
}

private data class UnitValue(val value: Double, val unit: String)

private fun parseUnit(value: String): UnitValue {
    val result = Regex("([-0-9.]+)\\s*([a-zA-Z]+)").matchEntire(value) ?: throw IllegalArgumentException()
    return UnitValue(result.groupValues[1].toDouble(), result.groupValues[2])
}
