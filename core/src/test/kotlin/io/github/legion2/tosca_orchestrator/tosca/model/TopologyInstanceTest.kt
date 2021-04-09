package io.github.legion2.tosca_orchestrator.tosca.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.legion2.tosca_orchestrator.tosca.model.instance.createInstanceModel
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.net.URI

internal class TopologyInstanceTest {

    @Disabled
    @Test
    fun test() {
        val objectMapper = ObjectMapper()
        objectMapper.registerKotlinModule()
        assertThrows<RuntimeException> {
            objectMapper.readValue<Map<String, String>>(
                """
            {
                "key1": "value",
                "key2": null
            }
            """.trimIndent()
            )
        }
    }

    @ParameterizedTest
    @MethodSource("serviceTemplateFiles")
    fun testCreateInstanceModel(serviceTemplateFile: String) {
        val path = javaClass.getResource(serviceTemplateFile).toURI()
        val instanceModel = createInstanceModel(path, emptyMap())
    }

    @Disabled
    @Test
    fun testLoadFromURL() {
        val instanceModel = createInstanceModel(
            URI("https://raw.githubusercontent.com/xlab-si/xopera-opera/bd1cd5cee95de212b141394123722f9deb6b4126/tests/integration/misc_tosca_types/service-template.yaml"),
            mapOf("host_ip" to "localhost", "slovenian_greeting" to "Zdravo!")
        )
    }

    @Test
    fun testSensorExample() {
        val path = javaClass.getResource("sensor.yaml").toURI()
        val inputs = mapOf("MQTT_URL" to "", "MQTT_User" to "", "MQTT_Password" to "")
        val instanceModel = createInstanceModel(path, inputs)
    }

    companion object {
        @JvmStatic
        fun serviceTemplateFiles() = listOf(
            "serviceTemplateExample.yaml",
            "serviceTemplateHello.yaml",
            //"serviceTemplateDocker.yaml",
            "serviceTemplateIntrinsicFunctions.yaml",
            "serviceTemplateMultipleArtifacts.yaml"
        )
    }

}
