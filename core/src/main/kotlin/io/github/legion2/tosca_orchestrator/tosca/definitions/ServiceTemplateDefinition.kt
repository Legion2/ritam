package io.github.legion2.tosca_orchestrator.tosca.definitions

import io.github.legion2.tosca_orchestrator.tosca.definitions.type.*
import java.net.URI

data class ServiceTemplateDefinition(
    val tosca_definitions_version: String,
    val namespace: URI? = null,
    val metadata: Map<String, String>? = null,
    val description: String? = null,
    val dsl_definitions: Any? = null,
    val repositories: Map<String, RepositoryDefinition>? = null,
    val imports: List<ImportDefinition>? = null,
    val artifact_types: Map<String, ArtifactType>? = null,
    val data_types: Map<String, DataType>? = null,
    val capability_types: Map<String, CapabilityType>? = null,
    val interface_types: Map<String, InterfaceType>? = null,
    val relationship_types: Map<String, RelationshipType>? = null,
    val node_types: Map<String, NodeType>? = null,
    val group_types: Map<String, GroupType>? = null,
    val policy_types: Map<String, PolicyType>? = null,
    val topology_template: TopologyTemplateDefinition? = null
)