package io.github.legion2.tosca_orchestrator.tosca.normative

import io.github.legion2.tosca_orchestrator.tosca.model.Namespace
import io.github.legion2.tosca_orchestrator.tosca.model.TypeReference
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedArtifactType

object NormativeArtifactTypes {
    val root = TypeReference<ResolvedArtifactType>(Namespace.tosca, "tosca.artifacts.Root")
    val file = TypeReference<ResolvedArtifactType>(Namespace.tosca, "tosca.artifacts.File")
    val deployment = TypeReference<ResolvedArtifactType>(Namespace.tosca, "tosca.artifacts.Deployment")
    val image = TypeReference<ResolvedArtifactType>(Namespace.tosca, "tosca.artifacts.Deployment.Image")
    val imageVM = TypeReference<ResolvedArtifactType>(Namespace.tosca, "tosca.artifacts.Deployment.Image.VM")
    val implementation = TypeReference<ResolvedArtifactType>(Namespace.tosca, "tosca.artifacts.Implementation")
    val bash = TypeReference<ResolvedArtifactType>(Namespace.tosca, "tosca.artifacts.Implementation.Bash")
    val python = TypeReference<ResolvedArtifactType>(Namespace.tosca, "tosca.artifacts.Implementation.Python")
    val template = TypeReference<ResolvedArtifactType>(Namespace.tosca, "tosca.artifacts.template")
}

val artifactTypeShortNameAliases = mapOf(
    "File" to NormativeArtifactTypes.file.name,
    "Deployment.Image" to NormativeArtifactTypes.image.name,
    "Bash" to NormativeArtifactTypes.bash.name,
    "Python" to NormativeArtifactTypes.python.name
)
