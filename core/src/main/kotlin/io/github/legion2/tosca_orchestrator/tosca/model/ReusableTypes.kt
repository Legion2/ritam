package io.github.legion2.tosca_orchestrator.tosca.model

import io.github.legion2.tosca_orchestrator.tosca.model.property.primitiveTypes
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.*

data class ReusableTypes(
    val dataTypes: Map<Namespace, Map<String, ResolvedDataType>>,
    val nodeTypes: Map<Namespace, Map<String, ResolvedNodeType>>,
    val relationshipTypes: Map<Namespace, Map<String, ResolvedRelationshipType>>,
    val interfaceTypes: Map<Namespace, Map<String, ResolvedInterfaceType>>,
    val capabilityTypes: Map<Namespace, Map<String, ResolvedCapabilityType>>,
    val artifactTypes: Map<Namespace, Map<String, ResolvedArtifactType>>,
    val policyTypes: Map<Namespace, Map<String, ResolvedPolicyType>>
)

private fun MutableReusableTypes.asImmutable(): ReusableTypes {
    return ReusableTypes(
        dataTypes,
        nodeTypes,
        relationshipTypes,
        interfaceTypes,
        capabilityTypes,
        artifactTypes,
        policyTypes
    )
}

private data class MutableReusableTypes(
    val dataTypes: MutableMap<Namespace, Map<String, ResolvedDataType>> = mutableMapOf(),
    val nodeTypes: MutableMap<Namespace, Map<String, ResolvedNodeType>> = mutableMapOf(),
    val relationshipTypes: MutableMap<Namespace, Map<String, ResolvedRelationshipType>> = mutableMapOf(),
    val interfaceTypes: MutableMap<Namespace, Map<String, ResolvedInterfaceType>> = mutableMapOf(),
    val capabilityTypes: MutableMap<Namespace, Map<String, ResolvedCapabilityType>> = mutableMapOf(),
    val artifactTypes: MutableMap<Namespace, Map<String, ResolvedArtifactType>> = mutableMapOf(),
    val policyTypes: MutableMap<Namespace, Map<String, ResolvedPolicyType>> = mutableMapOf()
) {
    fun add(reusableNamespace: ReusableNamespace) {
        dataTypes.merge(reusableNamespace.namespace, reusableNamespace.dataTypes)
        nodeTypes.merge(reusableNamespace.namespace, reusableNamespace.nodeTypes)
        relationshipTypes.merge(reusableNamespace.namespace, reusableNamespace.relationshipTypes)
        interfaceTypes.merge(reusableNamespace.namespace, reusableNamespace.interfaceTypes)
        capabilityTypes.merge(reusableNamespace.namespace, reusableNamespace.capabilityTypes)
        artifactTypes.merge(reusableNamespace.namespace, reusableNamespace.artifactTypes)
        policyTypes.merge(reusableNamespace.namespace, reusableNamespace.policyTypes)
    }
}

private fun <K, Y, V> MutableMap<K, Map<Y, V>>.merge(key: K, value: Map<Y, V>) {
    this.merge(key, value) { t, u ->
        t + u
    }
}

fun getAllTypes(importedServiceTemplates: Map<Namespace, ImportedServiceTemplate>): ReusableTypes {
    val reusableTypes = MutableReusableTypes()

    reusableTypes.add(internalTypes)

    topologicalSort(importedServiceTemplates) { it.importedNamespaces }
        .entries.forEach { (_, importedServiceTemplate) ->
            reusableTypes.add(ReusableNamespace.from(importedServiceTemplate, reusableTypes.asImmutable()))
        }

    return reusableTypes.asImmutable()
}

const val toscaEntityRoot = "tosca.entity.Root"

val internalTypes = ReusableNamespace(
    Namespace.tosca,
    primitiveTypes,
    mapOf(toscaEntityRoot to ResolvedNodeType(TypeInfo(TypeReference(Namespace.tosca, toscaEntityRoot)))),
    mapOf(toscaEntityRoot to ResolvedRelationshipType(toscaEntityRoot)),
    mapOf(toscaEntityRoot to ResolvedInterfaceType(toscaEntityRoot)),
    mapOf(toscaEntityRoot to ResolvedCapabilityType(toscaEntityRoot)),
    mapOf(toscaEntityRoot to ResolvedArtifactType(TypeInfo(TypeReference(Namespace.tosca, toscaEntityRoot)))),
    mapOf(toscaEntityRoot to ResolvedPolicyType(toscaEntityRoot)),
    emptyMap()
)

