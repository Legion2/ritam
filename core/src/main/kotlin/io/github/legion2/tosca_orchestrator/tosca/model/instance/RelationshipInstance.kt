package io.github.legion2.tosca_orchestrator.tosca.model.instance

import io.github.legion2.tosca_orchestrator.tosca.model.CompletedRelationship

data class RelationshipInstance(
    val name: String,
    val properties: Map<String, PropertyInstance>,
    val attributes: Map<String, AttributeInstance>,
    val interfaces: Map<String, InterfaceInstance>,
    val source: String,
    val target: String
) {
    companion object {
        fun from(relationship: CompletedRelationship): RelationshipInstance {
            try {
                relationship.relationshipTemplate.run {
                    return RelationshipInstance(
                        name,
                        properties,
                        attributes,
                        interfaces,
                        relationship.source,
                        relationship.target
                    )
                }

            } catch (e: RuntimeException) {
                throw IllegalArgumentException("Can not instantiate Relationship: $relationship", e)
            }
        }
    }
}
