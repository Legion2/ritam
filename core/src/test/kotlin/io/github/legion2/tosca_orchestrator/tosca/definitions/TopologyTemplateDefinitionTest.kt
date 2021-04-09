package io.github.legion2.tosca_orchestrator.tosca.definitions

import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class TopologyTemplateDefinitionTest {
    @ParameterizedTest
    @MethodSource("topologyTemplateFiles")
    fun testParsing(topologyTemplateFile: String) {
        val topologyTemplate =
            toscaYamlMapper.readValue<TopologyTemplateDefinition>(javaClass.getResource(topologyTemplateFile))
    }

    companion object {
        @JvmStatic
        fun topologyTemplateFiles() = listOf(
            "topologyTemplate1.yaml",
            "topologyTemplate2.yaml",
            "topologyTemplate3.yaml",
            "topologyTemplate4.yaml"
        )
    }
}