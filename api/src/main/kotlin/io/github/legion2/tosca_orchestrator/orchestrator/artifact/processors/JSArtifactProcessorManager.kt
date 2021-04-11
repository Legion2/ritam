package io.github.legion2.tosca_orchestrator.orchestrator.artifact.processors

import io.github.legion2.tosca_orchestrator.orchestrator.artifact.*
import io.github.legion2.tosca_orchestrator.orchestrator.await
import io.github.legion2.tosca_orchestrator.orchestrator.javascript
import io.github.legion2.tosca_orchestrator.orchestrator.model.DeploymentArtifact
import io.github.legion2.tosca_orchestrator.tosca.model.isSubtypeOf
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedArtifact
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedArtifactType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files

class JSArtifactProcessorManager : ArtifactProcessorManager {
    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun process(
        artifact: ResolvedArtifact,
        executionEnvironment: ExecutionEnvironment,
        inputs: Map<String, String>,
        outputs: Set<String>,
        dependencies: List<ResolvedArtifact>,
        deploymentArtifacts: List<DeploymentArtifact>,
    ): Map<String, String> {
        return withContext(Dispatchers.IO) {
            val artifactPath = copyArtifact(artifact, executionEnvironment, ".mjs")
            withDeploymentArtifacts(executionEnvironment, deploymentArtifacts) {
                withDependencies(executionEnvironment, dependencies) {
                    if (Files.exists(executionEnvironment.directory.resolve("package.json")) && Files.exists(
                            executionEnvironment.directory.resolve("package-lock.json")
                        )
                    ) {
                        val processBuilder = ProcessBuilder(if (isWindows()) "npm.cmd" else "npm", "ci")
                            .directory(executionEnvironment.directory.toFile())
                            .redirectErrorStream(true)
                        val process = processBuilder.start()
                        val exitCode = process.await()
                        if (exitCode != 0) {
                            val log = String(process.inputStream.readAllBytes())
                            throw IllegalStateException("Failed to installed dependencies of Operation: $exitCode\n$log")
                        }
                    }

                    withInputOutputEnvironmentVariables(executionEnvironment, inputs, outputs) { env ->
                        val processBuilder = ProcessBuilder("node", artifactPath.toString())
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
        }
    }

    override fun canProcess(type: ResolvedArtifactType): Boolean {
        return type.isSubtypeOf(javascript)
    }
}
