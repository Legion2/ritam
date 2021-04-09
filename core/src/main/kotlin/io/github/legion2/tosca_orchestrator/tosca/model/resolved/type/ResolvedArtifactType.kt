package io.github.legion2.tosca_orchestrator.tosca.model.resolved.type

import io.github.legion2.tosca_orchestrator.tosca.definitions.type.ArtifactType
import io.github.legion2.tosca_orchestrator.tosca.model.Type
import io.github.legion2.tosca_orchestrator.tosca.model.TypeInfo
import io.github.legion2.tosca_orchestrator.tosca.model.TypeReference
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.PropertyResolverContext
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedProperty
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.refine

data class ResolvedArtifactType(
    override val typeInfo: TypeInfo<ResolvedArtifactType>,
    val mime_type: String? = null,
    val file_ext: List<String> = emptyList(),
    val properties: Map<String, ResolvedProperty> = emptyMap()
) : Type<ResolvedArtifactType> {
    companion object {
        fun resolve(
            name: TypeReference<ResolvedArtifactType>,
            artifactType: ArtifactType,
            resolveArtifactType: (String) -> ResolvedArtifactType,
            propertyResolverContext: PropertyResolverContext
        ): ResolvedArtifactType {
            val baseType = resolveArtifactType(artifactType.derived_from)
            val typeInfo = TypeInfo(name, baseType.typeInfo)
            val properties = baseType.properties.refine(artifactType.properties.orEmpty(), propertyResolverContext)
            return ResolvedArtifactType(typeInfo, artifactType.mime_type, artifactType.file_ext.orEmpty(), properties)
        }
    }
}
