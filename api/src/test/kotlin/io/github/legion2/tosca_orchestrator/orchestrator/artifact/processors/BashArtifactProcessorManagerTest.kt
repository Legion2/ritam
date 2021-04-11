package io.github.legion2.tosca_orchestrator.orchestrator.artifact.processors

import io.github.legion2.tosca_orchestrator.orchestrator.artifact.ExecutionEnvironment
import io.github.legion2.tosca_orchestrator.tosca.model.TypeInfo
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedArtifact
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedArtifactType
import io.github.legion2.tosca_orchestrator.tosca.normative.NormativeArtifactTypes
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Files

internal class BashArtifactProcessorManagerTest {

    @Test
    fun process() = runBlocking {
        val uri = this@BashArtifactProcessorManagerTest.javaClass.getResource("script.sh").toURI()
        val artifact = ResolvedArtifact(ResolvedArtifactType(TypeInfo(NormativeArtifactTypes.bash)), uri)
        val executionEnvironment = ExecutionEnvironment.getExecutionEnvironment()

        val output = BashArtifactProcessorManager().process(
            artifact,
            executionEnvironment,
            mapOf("input" to "foo"),
            setOf("status", "input"),
            emptyList(),
            emptyList()
        )
        assertEquals("example status", output["status"]?.trim())
        assertEquals("input: foo", output["input"]?.trim())
        executionEnvironment.cleanup()
    }

    @ParameterizedTest
    @MethodSource("fileNames")
    fun testFileName(deployPath: String?, fileName: Matcher<in String?>) = runBlocking {
        val uri = this@BashArtifactProcessorManagerTest.javaClass.getResource("fileNameTest.sh").toURI()
        val artifact = ResolvedArtifact(ResolvedArtifactType(TypeInfo(NormativeArtifactTypes.bash)), uri, deployPath)
        val executionEnvironment = ExecutionEnvironment.getExecutionEnvironment()

        val output = BashArtifactProcessorManager().process(
            artifact,
            executionEnvironment,
            emptyMap(),
            setOf("filename"),
            emptyList(),
            emptyList()
        )
        assertThat(output["filename"]?.trim(), fileName)
        executionEnvironment.cleanup()
    }

    companion object {
        @JvmStatic
        fun fileNames() = listOf(
            Arguments.of(null, Matchers.startsWith("artifact-")),
            Arguments.of("myScript.sh", Matchers.`is`("myScript.sh")),
            Arguments.of("./myScript.sh", Matchers.`is`("myScript.sh")),
            Arguments.of("dir/myScript.sh", Matchers.`is`("myScript.sh")),
            Arguments.of(Files.createTempFile("absolutePath", ".sh").toAbsolutePath().toString(), Matchers.startsWith("absolutePath")),
        )
    }
}