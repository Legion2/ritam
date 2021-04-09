package io.github.legion2.tosca_orchestrator.tosca.model.resolved

import io.github.legion2.tosca_orchestrator.tosca.definitions.InterfaceDefinition
import io.github.legion2.tosca_orchestrator.tosca.definitions.NotificationDefinition
import io.github.legion2.tosca_orchestrator.tosca.model.TypeResolver
import io.github.legion2.tosca_orchestrator.tosca.model.refineOrAdd
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedInterfaceType

data class ResolvedInterface(
    val type: ResolvedInterfaceType,
    val inputs: Map<String, ResolvedProperty> = emptyMap(),
    val operations: Map<String, ResolvedOperation> = emptyMap(),
    val notifications: Map<String, NotificationDefinition> = emptyMap()
) {
    companion object {
        fun from(
            interfaceDefinition: InterfaceDefinition,
            propertyResolverContext: PropertyResolverContext,
            interfaceTypeResolver: TypeResolver<ResolvedInterfaceType>,
            operationResolverContext: OperationResolverContext
        ): ResolvedInterface {
            interfaceDefinition.type
                ?: throw IllegalArgumentException("Interface must declare a type if they do not refine an existing Interface")
            val interfaceType = interfaceTypeResolver(interfaceDefinition.type)
            if (interfaceDefinition.deprecatedOperationsSyntax.isNotEmpty()) {
                throw IllegalArgumentException("deprecated Interface definition without 'operations' keyname is not supported: $interfaceDefinition")
            }

            val inputs = interfaceType.inputs.refine(interfaceDefinition.inputs.orEmpty(), propertyResolverContext)
            val operations = interfaceType.operations.addImplementation(
                interfaceDefinition.operations.orEmpty(),
                propertyResolverContext,
                operationResolverContext
            )
            val notifications = interfaceType.notifications + interfaceDefinition.notifications.orEmpty()
            if (notifications.isNotEmpty()) {
                TODO("notifications are not implemented")
            }
            return ResolvedInterface(interfaceType, inputs, operations, emptyMap())
        }
    }
}

private fun ResolvedInterface.refine(
    interfaceDefinition: InterfaceDefinition,
    expressionResolverContext: PropertyResolverContext,
    interfaceTypeResolver: TypeResolver<ResolvedInterfaceType>,
    operationResolverContext: OperationResolverContext
): ResolvedInterface {
    if (interfaceDefinition.type != null && interfaceTypeResolver(interfaceDefinition.type) != type) {
        throw IllegalArgumentException("The type of an Interface can not be changed by redefining an Interface")
    }
    val inputs = inputs.refine(interfaceDefinition.inputs.orEmpty(), expressionResolverContext)
    val operations =
        operations.refine(interfaceDefinition.operations.orEmpty(), expressionResolverContext, operationResolverContext)
    val notifications = notifications + interfaceDefinition.notifications.orEmpty()
    return ResolvedInterface(type, inputs, operations, notifications)
}

fun Map<String, ResolvedInterface>.refine(
    interfaceDefinitions: Map<String, InterfaceDefinition>,
    interfaceTypeResolver: TypeResolver<ResolvedInterfaceType>,
    propertyResolverContext: PropertyResolverContext,
    operationResolverContext: OperationResolverContext
): Map<String, ResolvedInterface> {
    return refineOrAdd(
        interfaceDefinitions,
        { refine(it, propertyResolverContext, interfaceTypeResolver, operationResolverContext) },
        { ResolvedInterface.from(it, propertyResolverContext, interfaceTypeResolver, operationResolverContext) })
}
