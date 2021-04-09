package io.github.legion2.tosca_orchestrator.tosca.state

import io.github.legion2.tosca_orchestrator.tosca.model.property.*
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedProperty
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedSchema
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedDataType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class PropertyInstanceKtTest {

    private val testContext = object : EvaluationContext {
        override fun evaluate(resolvedExpression: ResolvedExpression): Value {
            TODO("Not yet implemented")
        }
    }

    @ParameterizedTest
    @MethodSource("validValues")
    fun testValidate(value: Value, resolvedDataType: ResolvedDataType, expected: Value) {
        Assertions.assertEquals(
            expected,
            testContext.validateAndPopulatePropertyValue(value, resolvedDataType.toValidationInfo())
        )
    }

    @ParameterizedTest
    @MethodSource("invalidValues")
    fun testNotValidate(value: Value, resolvedDataType: ResolvedDataType) {
        assertThrows<IllegalArgumentException> {
            testContext.validateAndPopulatePropertyValue(
                value,
                resolvedDataType.toValidationInfo()
            )
        }
    }

    companion object {
        @JvmStatic
        fun validValues() = listOf(
            Arguments.of(Value.String("string"), PrimitiveType.STRING.toResolvedType(), Value.String("string")),
            Arguments.of(Value.Integer(17), PrimitiveType.INTEGER.toResolvedType(), Value.Integer(17)),
            Arguments.of(Value.Boolean(true), PrimitiveType.BOOLEAN.toResolvedType(), Value.Boolean(true)),
            Arguments.of(Value.Float(17.5), PrimitiveType.FLOAT.toResolvedType(), Value.Float(17.5)),
            Arguments.of(Value.String("1 h"), PrimitiveType.SCALAR_UNIT_TIME.toResolvedType(), Value.String("1 h")),
            Arguments.of(
                Value.String("2020-10-26T14:55:05+01:00"),
                PrimitiveType.TIMESTAMP.toResolvedType(),
                Value.String("2020-10-26T14:55:05+01:00")
            ),
            Arguments.of(
                Value.Map(
                    mapOf(
                        "key" to Value.Integer(1),
                        "test" to Value.Integer(2)
                    )
                ),
                ResolvedDataType(
                    "",
                    PrimitiveType.MAP,
                    entry_schema = ResolvedSchema(PrimitiveType.INTEGER.toResolvedType())
                ), Value.Map(
                    mapOf(
                        "key" to Value.Integer(1),
                        "test" to Value.Integer(2)
                    )
                )
            ),
            /*Arguments.of(//TODO
                Value.Map(
                    mapOf(
                        "key" to Value.String("test"),
                        "test" to Value.Integer(2)
                    )
                ),
                ResolvedDataType(
                    "", PrimitiveType.COMPLEX, properties = mapOf(
                        "key" to ResolvedProperty(PrimitiveType.STRING.toResolvedType(), true),
                        "test" to ResolvedProperty(PrimitiveType.INTEGER.toResolvedType(), false),
                        "default" to ResolvedProperty(
                            PrimitiveType.BOOLEAN.toResolvedType(), true,
                            ResolvedExpression.Literal.Boolean(true)
                        )
                    )
                ),
                Value.Map(
                    mapOf(
                        "key" to Value.String("test"),
                        "test" to Value.Integer(2),
                        "default" to Value.Boolean(true)
                    )
                )
            )*/
        )

        @JvmStatic
        fun invalidValues() = listOf(
            Arguments.of(Value.String("string"), (PrimitiveType.INTEGER.toResolvedType())),
            Arguments.of(Value.Integer(17), (PrimitiveType.BOOLEAN.toResolvedType())),
            Arguments.of(Value.Boolean(true), (PrimitiveType.COMPLEX.toResolvedType())),
            Arguments.of(Value.Float(17.5), (PrimitiveType.INTEGER.toResolvedType())),
            Arguments.of(Value.String("1 h"), (PrimitiveType.SCALAR_UNIT_BITRATE.toResolvedType())),
            Arguments.of(Value.String("2020-10-26T14:55:05+01:00"), (PrimitiveType.INTEGER.toResolvedType())),
            Arguments.of(
                Value.Map(mapOf("key" to Value.Integer(1), "test" to Value.Integer(2))),
                ResolvedDataType(
                    "",
                    PrimitiveType.MAP,
                    entry_schema = ResolvedSchema(PrimitiveType.STRING.toResolvedType())
                )
            )
        )
    }
}