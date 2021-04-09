package io.github.legion2.tosca_orchestrator.tosca.model.resolved.type

import io.github.legion2.tosca_orchestrator.tosca.definitions.NotificationTypeDefinition
import io.github.legion2.tosca_orchestrator.tosca.definitions.type.InterfaceType
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.PropertyResolverContext
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedOperationType
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedOperationType.Companion.refine
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedProperty
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.refine

data class ResolvedInterfaceType(
    val name: String,
    val inputs: Map<String, ResolvedProperty> = emptyMap(),
    val operations: Map<String, ResolvedOperationType> = emptyMap(),
    val notifications: Map<String, NotificationTypeDefinition> = emptyMap()
) {
    companion object {
        fun resolve(
            name: String,
            interfaceType: InterfaceType,
            resolveInterfaceType: (String) -> ResolvedInterfaceType,
            propertyResolverContext: PropertyResolverContext
        ): ResolvedInterfaceType {
            val baseType = resolveInterfaceType(interfaceType.derived_from)
            val inputs = baseType.inputs.refine(interfaceType.inputs.orEmpty(), propertyResolverContext)
            val operations = baseType.operations.refine(interfaceType.operations.orEmpty(), propertyResolverContext)
            return ResolvedInterfaceType(
                name,
                inputs,
                operations,
                baseType.notifications + interfaceType.notifications.orEmpty()
            )
        }
    }
}
