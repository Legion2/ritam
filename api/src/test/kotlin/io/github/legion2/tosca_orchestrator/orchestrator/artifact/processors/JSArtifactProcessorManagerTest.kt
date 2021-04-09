package io.github.legion2.tosca_orchestrator.orchestrator.artifact.processors

import io.github.legion2.tosca_orchestrator.orchestrator.artifact.ExecutionEnvironment
import io.github.legion2.tosca_orchestrator.orchestrator.javascript
import io.github.legion2.tosca_orchestrator.tosca.model.TypeInfo
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedArtifact
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedArtifactType
import io.github.legion2.tosca_orchestrator.tosca.normative.NormativeArtifactTypes
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class JSArtifactProcessorManagerTest {
    @Test
    fun process() = runBlocking {
        val uri = this@JSArtifactProcessorManagerTest.javaClass.getResource("script.mjs").toURI()
        val artifact = ResolvedArtifact(ResolvedArtifactType(TypeInfo(javascript)), uri)
        val executionEnvironment = ExecutionEnvironment.getExecutionEnvironment()

        val output = JSArtifactProcessorManager().process(
                artifact,
                executionEnvironment,
                mapOf("input" to "foo"),
                setOf("status", "input"),
                emptyList()
        )
        assertEquals("example status", output["status"]?.trim())
        assertEquals("input: foo", output["input"]?.trim())
        executionEnvironment.cleanup()
    }

    @Test
    fun installDependencies() = runBlocking {
        val uri = this@JSArtifactProcessorManagerTest.javaClass.getResource("script-with-dependency.mjs").toURI()
        val packageJsonUri = this@JSArtifactProcessorManagerTest.javaClass.getResource("package.json").toURI()
        val packageLockUri = this@JSArtifactProcessorManagerTest.javaClass.getResource("package-lock.json").toURI()
        val artifact = ResolvedArtifact(ResolvedArtifactType(TypeInfo(javascript)), uri)
        val packageJsonArtifact = ResolvedArtifact(ResolvedArtifactType(TypeInfo(NormativeArtifactTypes.file)), packageJsonUri, "package.json")
        val packageLockArtifact = ResolvedArtifact(ResolvedArtifactType(TypeInfo(NormativeArtifactTypes.file)), packageLockUri, "package-lock.json")
        val executionEnvironment = ExecutionEnvironment.getExecutionEnvironment()

        val output = JSArtifactProcessorManager().process(
                artifact,
                executionEnvironment,
                mapOf("duration" to "10s"),
                setOf("duration"),
                listOf(packageJsonArtifact, packageLockArtifact)
        )
        assertEquals("10000", output["duration"]?.trim())
        executionEnvironment.cleanup()
    }
}