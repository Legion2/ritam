package io.github.legion2.tosca_orchestrator.orchestrator.artifact.processors

import io.github.legion2.tosca_orchestrator.orchestrator.artifact.*
import io.github.legion2.tosca_orchestrator.orchestrator.await
import io.github.legion2.tosca_orchestrator.tosca.model.isSubtypeOf
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedArtifact
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedArtifactType
import io.github.legion2.tosca_orchestrator.tosca.normative.NormativeArtifactTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files

class BashArtifactProcessorManager : ArtifactProcessorManager {
    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun process(
            artifact: ResolvedArtifact,
            executionEnvironment: ExecutionEnvironment,
            inputs: Map<String, String>,
            outputs: Set<String>,
            dependencies: List<ResolvedArtifact>,
    ): Map<String, String> {
        return withContext(Dispatchers.IO) {
            val artifactPath = copyArtifact(artifact, executionEnvironment, ".sh")

            withInputOutputEnvironmentVariables(executionEnvironment, inputs, outputs) { env ->
                val processBuilder = if (isWindows()) {
                    ProcessBuilder("C:\\Program Files\\Git\\usr\\bin\\bash.exe", artifactPath.toString())
                } else {
                    ProcessBuilder("bash", artifactPath.toString())
                }
                        .directory(executionEnvironment.directory.toFile())
                        .redirectErrorStream(true)
                val environment = processBuilder.environment()
                environment.clear()
                environment.putAll(env)

                val process = processBuilder.start()

                val exitCode = process.await()
                Files.delete(artifactPath)
                if (exitCode != 0) {
                    val log = String(process.inputStream.readAllBytes())
                    throw IllegalStateException("Operation failed with exit code: $exitCode\n$log")
                }
            }
        }
    }

    override fun canProcess(type: ResolvedArtifactType): Boolean {
        return type.isSubtypeOf(NormativeArtifactTypes.bash)
    }
}

