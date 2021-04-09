package io.github.legion2.tosca_orchestrator.orchestrator.model

import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.legion2.tosca_orchestrator.tosca.model.property.EntityReference
import io.github.legion2.tosca_orchestrator.tosca.model.property.ResolvedPropertyNameOrIndex

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.WRAPPER_OBJECT)
sealed class DynamicExpression {
    sealed class Literal : DynamicExpression() {
        data class String(val value: kotlin.String) : Literal()
        data class Boolean(val value: kotlin.Boolean) : Literal()
        data class Float(val value: Double) : Literal()
        data class Integer(val value: Int) : Literal()
        data class Map(val value: kotlin.collections.Map<kotlin.String, DynamicExpression>) : Literal()
        data class List(val value: kotlin.collections.List<DynamicExpression>) : Literal()
    }

    sealed class Function : DynamicExpression() {
        data class Join(val string_value_expressions: List<DynamicExpression>, val delimiter: String) : Function()
        data class Token(
            val string_with_token: DynamicExpression,
            val string_of_token_chars: String,
            val substring_index: Int
        ) : Function()

        data class GetAttribute(
            val entityName: EntityReference,
            val req_or_cap_name: String?,
            val attribute_name: List<ResolvedPropertyNameOrIndex>
        ) : Function()
    }
}
