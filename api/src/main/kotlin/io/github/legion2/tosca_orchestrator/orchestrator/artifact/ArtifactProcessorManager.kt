package io.github.legion2.tosca_orchestrator.orchestrator.artifact

import io.github.legion2.tosca_orchestrator.orchestrator.model.DeploymentArtifact
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedArtifact
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedArtifactType
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

interface ArtifactProcessorManager {
    suspend fun process(
        artifact: ResolvedArtifact,
        executionEnvironment: ExecutionEnvironment,
        inputs: Map<String, String>,
        outputs: Set<String>,
        dependencies: List<ResolvedArtifact>,
        deploymentArtifacts: List<DeploymentArtifact>
    ): Map<String, String>

    fun canProcess(type: ResolvedArtifactType): Boolean
}

fun copyArtifact(
    artifact: ResolvedArtifact,
    executionEnvironment: ExecutionEnvironment,
    suffix: String
): Path {
    val deployPath = artifact.deployPath
    val artifactPath = if (deployPath == null) {
        Files.createTempFile(executionEnvironment.directory, "artifact-", suffix)
    } else {
        executionEnvironment.directory.resolve(deployPath).also { Files.createDirectories(it.parent) }
    }
    Files.copy(artifact.file.toURL().openStream(), artifactPath, StandardCopyOption.REPLACE_EXISTING)
    return artifactPath
}

suspend fun <T> withDeploymentArtifacts(
    executionEnvironment: ExecutionEnvironment,
    deploymentArtifacts: List<DeploymentArtifact>,
    block: suspend () -> T
): T {
    val deploymentArtifactsPaths = deploymentArtifacts.mapNotNull {
        copyArtifact(it.artifact.copy(deployPath = it.location), executionEnvironment, "").run { if (it.remove) this else null }
    }

    val t = block.invoke()

    deploymentArtifactsPaths.forEach(Files::delete)
    return t
}

suspend fun <T> withDependencies(
    executionEnvironment: ExecutionEnvironment,
    dependencies: List<ResolvedArtifact>,
    block: suspend () -> T
): T {
    val dependencyPaths = dependencies.filter { it.deployPath != null }.map {
        copyArtifact(it, executionEnvironment, "")
    }

    val t = block.invoke()

    dependencyPaths.forEach(Files::delete)
    return t
}

suspend fun withInputOutputEnvironmentVariables(
    executionEnvironment: ExecutionEnvironment,
    inputs: Map<String, String>,
    outputs: Set<String>,
    block: suspend (env: Map<String, String>) -> Unit
): Map<String, String> {
    val outputFiles = outputs.associateWith {
        Files.createTempFile(executionEnvironment.directory, "tosca-output-${it}-", "")
    }

    val outputEnv = outputFiles.map { "TOSCA_OUTPUT_${it.key}" to it.value.toAbsolutePath().toString() }
    block.invoke(inputs + outputEnv)

    return outputFiles.mapValues { (_, path) ->
        Files.readString(path).also { Files.delete(path) }
    }
}

fun isWindows() = System.getProperty("os.name").toLowerCase().contains("win")