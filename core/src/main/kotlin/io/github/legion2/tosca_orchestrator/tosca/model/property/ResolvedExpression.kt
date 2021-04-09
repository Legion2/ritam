package io.github.legion2.tosca_orchestrator.tosca.model.property

import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.legion2.tosca_orchestrator.tosca.definitions.EntityName
import io.github.legion2.tosca_orchestrator.tosca.definitions.Expression
import io.github.legion2.tosca_orchestrator.tosca.definitions.Location
import io.github.legion2.tosca_orchestrator.tosca.definitions.PropertyNameOrIndex
import io.github.legion2.tosca_orchestrator.tosca.model.TypeReference
import io.github.legion2.tosca_orchestrator.tosca.model.TypeResolver
import io.github.legion2.tosca_orchestrator.tosca.model.addExceptionContextInfo
import io.github.legion2.tosca_orchestrator.tosca.model.instance.NodeInstance
import io.github.legion2.tosca_orchestrator.tosca.model.instance.RelationshipInstance
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedNodeType

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.WRAPPER_OBJECT)
sealed class ResolvedExpression {
    sealed class Literal : ResolvedExpression() {
        data class String(val value: kotlin.String) : Literal()
        data class Boolean(val value: kotlin.Boolean) : Literal()
        data class Float(val value: Double) : Literal()
        data class Integer(val value: Int) : Literal()
        data class Map(val value: kotlin.collections.Map<kotlin.String, ResolvedExpression>) : Literal()
        data class List(val value: kotlin.collections.List<ResolvedExpression>) : Literal()
    }

    sealed class Function : ResolvedExpression() {
        data class Concat(val string_value_expressions: List<ResolvedExpression>) : Function()
        data class Join(val string_value_expressions: List<ResolvedExpression>, val delimiter: String?) : Function()
        data class Token(
            val string_with_token: ResolvedExpression,
            val string_of_token_chars: String,
            val substring_index: Int
        ) : Function()

        data class GetInput(val propertyName: List<ResolvedPropertyNameOrIndex>) : Function()
        data class GetProperty(
            val entityName: EntityReference,
            val req_or_cap_name: String?,
            val property_name: List<ResolvedPropertyNameOrIndex>
        ) : Function()

        data class GetAttribute(
            val entityName: EntityReference,
            val req_or_cap_name: String?,
            val attribute_name: List<ResolvedPropertyNameOrIndex>
        ) : Function()

        data class GetOperationOutput(
            val entity_name: EntityReference,
            val interface_name: String,
            val operation_name: String,
            val output_variable_name: String
        ) : Function()

        data class GetNodesOfType(val nodeType: TypeReference<ResolvedNodeType>) : Function()
        data class GetArtifact(
            val entityName: EntityReference,
            val artifact_name: String,
            val location: Location?,
            val remove: Boolean?
        ) : Function()
    }

    companion object {
        fun Expression.resolve(expressionResolverContext: ExpressionResolverContext): ResolvedExpression = runCatching {
            when (this) {
                is Expression.Literal.String -> Literal.String(value)
                is Expression.Literal.Boolean -> Literal.Boolean(value)
                is Expression.Literal.Float -> Literal.Float(value)
                is Expression.Literal.Integer -> Literal.Integer(value)
                is Expression.Literal.Map -> Literal.Map(value.mapValues { it.value.resolve(expressionResolverContext) })
                is Expression.Literal.List -> Literal.List(value.map { it.resolve(expressionResolverContext) })
                is Expression.Function.Concat -> Function.Concat(string_value_expressions.map {
                    it.resolve(
                        expressionResolverContext
                    )
                })
                is Expression.Function.Join -> Function.Join(string_value_expressions.map {
                    it.resolve(
                        expressionResolverContext
                    )
                }, delimiter)
                is Expression.Function.Token -> Function.Token(
                    string_with_token.resolve(expressionResolverContext),
                    string_of_token_chars,
                    substring_index
                )
                is Expression.Function.GetInput -> Function.GetInput(ResolvedPropertyNameOrIndex.from(property_name))
                is Expression.Function.GetProperty -> Function.GetProperty(
                    EntityReference.resolve(
                        entity_name,
                        expressionResolverContext.contextReferences
                    ), req_or_cap_name, ResolvedPropertyNameOrIndex.from(property_name)
                )
                is Expression.Function.GetAttribute -> Function.GetAttribute(
                    EntityReference.resolve(
                        entity_name,
                        expressionResolverContext.contextReferences
                    ), req_or_cap_name, ResolvedPropertyNameOrIndex.from(attribute_name)
                )
                is Expression.Function.GetOperationOutput -> Function.GetOperationOutput(
                    EntityReference.resolve(
                        entity_name,
                        expressionResolverContext.contextReferences
                    ), interface_name, operation_name, output_variable_name
                )
                is Expression.Function.GetNodesOfType -> Function.GetNodesOfType(
                    expressionResolverContext.typeReferenceResolver(
                        node_type_name
                    )
                )
                is Expression.Function.GetArtifact -> Function.GetArtifact(
                    EntityReference.resolve(
                        entity_name,
                        expressionResolverContext.contextReferences
                    ), artifact_name, location, remove
                )
            }
        }.addExceptionContextInfo { "Can not resolve Expression: $this" }
    }
}

/**
 * Simple container to pass all required information around to resolve Expressions
 */
data class ExpressionResolverContext(
    val contextReferences: ContextReferences,
    val typeReferenceResolver: TypeResolver<TypeReference<ResolvedNodeType>>
)

data class ContextReferences(
    val self: EntityReference? = null,
    val source: EntityReference.NodeReference? = null,
    val target: EntityReference.NodeReference? = null,
    val host: EntityReference.NodeReference? = null
) {
    companion object {
        fun from(nodeInstance: NodeInstance): ContextReferences {
            return ContextReferences(
                EntityReference.NodeReference(nodeInstance.name),
                null,
                null,
                nodeInstance.host?.let { EntityReference.NodeReference(it) })//TODO HOST
        }

        fun from(relationshipInstance: RelationshipInstance): ContextReferences {
            return ContextReferences(
                EntityReference.RelationshipReference(relationshipInstance.name),
                EntityReference.NodeReference(relationshipInstance.source),
                EntityReference.NodeReference(relationshipInstance.target),
                null
            )
        }
    }
}

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY)
sealed class EntityReference {
    data class NodeReference(val name: String) : EntityReference()
    object NodeTypeSelfReference : EntityReference()
    data class RelationshipReference(val name: String) : EntityReference()
    object RelationshipTypeSelfReference : EntityReference()
    companion object {
        /**
         * Resolve context dependent entity names to simple string name
         */
        fun resolve(entityName: EntityName, contextReferences: ContextReferences): EntityReference {
            return when (entityName) {
                is EntityName.ModelableEntityName -> NodeReference(entityName.name)//TODO relationship
                EntityName.SELF -> contextReferences.self
                    ?: throw IllegalArgumentException("SELF is not defined in this context")
                EntityName.SOURCE -> contextReferences.source
                    ?: throw IllegalArgumentException("SOURCE is not defined in this context")
                EntityName.TARGET -> contextReferences.target
                    ?: throw IllegalArgumentException("TARGET is not defined in this context")
                EntityName.HOST -> contextReferences.host
                    ?: throw IllegalArgumentException("HOST is not defined in this context")
            }
        }
    }
}

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY)
sealed class ResolvedPropertyNameOrIndex {
    data class PropertyName(val name: String) : ResolvedPropertyNameOrIndex()
    data class PropertyIndex(val index: Int) : ResolvedPropertyNameOrIndex()
    companion object {
        fun from(propertyName: List<PropertyNameOrIndex>): List<ResolvedPropertyNameOrIndex> {
            return propertyName.map {
                when (it) {
                    is PropertyNameOrIndex.PropertyName -> PropertyName(it.name)
                    is PropertyNameOrIndex.PropertyIndex -> PropertyIndex(it.index)
                }
            }
        }
    }
}
