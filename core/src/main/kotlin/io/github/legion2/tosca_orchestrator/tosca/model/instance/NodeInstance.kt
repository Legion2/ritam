package io.github.legion2.tosca_orchestrator.tosca.model.instance

import io.github.legion2.tosca_orchestrator.tosca.model.CompletedNode
import io.github.legion2.tosca_orchestrator.tosca.model.TypeInfo
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedArtifact
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedNodeType

data class NodeInstance(
    val name: String,
    val typeInfo: TypeInfo<ResolvedNodeType>,
    val properties: Map<String, PropertyInstance>,
    val attributes: Map<String, AttributeInstance>,
    val interfaces: Map<String, InterfaceInstance>,
    val requirements: Map<String, RequirementInstance>,
    val capabilities: Map<String, CapabilityInstance>,
    val artifacts: Map<String, ResolvedArtifact>,
    val host: String?
) {
    companion object {
        fun from(node: CompletedNode): NodeInstance {
            return try {
                node.nodeTemplate.run {
                    NodeInstance(
                        name,
                        typeInfo,
                        properties,
                        attributes,
                        interfaces,
                        requirements,
                        capabilities,
                        artifacts,
                        node.host
                    )
                }
            } catch (e: RuntimeException) {
                throw IllegalArgumentException("Can not instantiate Node: ${node.nodeTemplate.name}", e)
            }
        }
    }
}
