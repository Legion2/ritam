package io.github.legion2.tosca_orchestrator.tosca.model.resolved

import io.github.legion2.tosca_orchestrator.tosca.definitions.ArtifactDefinition
import io.github.legion2.tosca_orchestrator.tosca.definitions.ArtifactDefinitionOrName
import io.github.legion2.tosca_orchestrator.tosca.model.TypeResolver
import io.github.legion2.tosca_orchestrator.tosca.model.combine
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedArtifactType
import io.github.legion2.tosca_orchestrator.tosca.normative.NormativeArtifactTypes
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.toPath

data class ResolvedArtifact(
    val type: ResolvedArtifactType,
    val file: URI,
    val deployPath: String? = null,
) {
    //TODO
    companion object {
        private fun from(
            artifactDefinition: ArtifactDefinition.Definition,
            artifactResolverContext: ArtifactResolverContext
        ): ResolvedArtifact {
            val artifactType = artifactResolverContext.artifactTypeResolver(artifactDefinition.type)
            val uri = artifactResolverContext.contextPath.resolve(artifactDefinition.file)
            return ResolvedArtifact(artifactType, uri, artifactDefinition.deploy_path)
        }

        private fun from(
            fileURI: ArtifactDefinition.FileURI,
            artifactResolverContext: ArtifactResolverContext
        ): ResolvedArtifact {
            val uri = artifactResolverContext.contextPath.resolve(fileURI.file)
            val artifactType = artifactResolverContext.fileExtensionResolver(Paths.get(uri).fileName.toString())
            return ResolvedArtifact(artifactType, uri)
        }

        /**
         * for operation implementation
         */
        fun from(
            artifactDefinitionOrName: ArtifactDefinitionOrName,
            artifactResolverContext: ArtifactResolverContext,
            artifactResolver: (String) -> ResolvedArtifact
        ): ResolvedArtifact {
            return when (artifactDefinitionOrName) {
                is ArtifactDefinitionOrName.Definition -> from(
                    artifactDefinitionOrName.definition,
                    artifactResolverContext
                )
                is ArtifactDefinitionOrName.ArtifactNameOrFileURI -> {
                    kotlin.runCatching { artifactResolver(artifactDefinitionOrName.name) }.getOrElse {
                        from(ArtifactDefinition.FileURI(artifactDefinitionOrName.name), artifactResolverContext)
                    }
                }
            }
        }

        /**
         * for Node type
         */
        fun from(
            artifactDefinition: ArtifactDefinition,
            artifactResolverContext: ArtifactResolverContext
        ): ResolvedArtifact {
            return when (artifactDefinition) {
                is ArtifactDefinition.Definition -> from(artifactDefinition, artifactResolverContext)
                is ArtifactDefinition.FileURI -> from(artifactDefinition, artifactResolverContext)
            }
        }

        /**
         * For node templates
         */
        fun validate(
            artifactAssignments: Map<String, ArtifactDefinition>,
            artifacts: Map<String, ResolvedArtifact>,
            artifactResolverContext: ArtifactResolverContext
        ): Map<String, ResolvedArtifact> {
            return artifacts.combine(artifactAssignments) { name, artifact, artifactAssignment ->
                if (artifactAssignment != null) {
                    from(artifactAssignment, artifactResolverContext)
                } else {
                    artifact!!
                }
                //TODO
            }
        }
    }
}

data class ArtifactResolverContext(
    val artifactTypeResolver: TypeResolver<ResolvedArtifactType>,
    val contextPath: URI,
    val fileExtensionResolver: (String) -> ResolvedArtifactType
)