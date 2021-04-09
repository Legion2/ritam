package io.github.legion2.tosca_orchestrator.api

import io.github.legion2.tosca_orchestrator.orchestrator.model.*
import io.github.legion2.tosca_orchestrator.orchestrator.storage.DeviceStorage
import io.quarkus.test.common.http.TestHTTPResource
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import java.net.URI
import java.nio.file.Path
import javax.inject.Inject
import javax.ws.rs.core.MediaType

@QuarkusTest
internal class ApplicationTemplateResourceTest {

    @Inject
    protected lateinit var deviceStorage: DeviceStorage

    @TestHTTPResource
    protected lateinit var deviceUri: URI

    @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Does not work, reason unknown")
    @Test
    fun testCreateApplicationTemplate() = runBlocking {
        val labels = mapOf("type" to "test-device")
        val properties = mapOf("location" to "test-location")
        deviceStorage.postResource(DeviceResource(Metadata("test-device", labels), Device(properties, deviceUri)))

        val name = "testCreateApplicationTemplate"
        val uri =
            Path.of("src/test/resources/io/github/legion2/tosca_orchestrator/tosca/model/sensor.yaml")
                .toUri()

        val inputs =
            mapOf("MQTT_URL" to "", "MQTT_User" to "", "MQTT_Password" to "", "location" to "\${device.location}")

        val application = ApplicationTemplate(labels, inputs, uri)
        val applicationTemplateResource = given().contentType(MediaType.APPLICATION_JSON)
            .body(ApplicationTemplateResource(Metadata(name), application))
            .`when`()
            .post("/api/resources/application-templates/{name}", name)
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .extract().`as`(ApplicationTemplateResource::class.java)

        assertThat(applicationTemplateResource.metadata.name, `is`(name))

        withTimeout(20_000) {
            do {
                delay(100)
                val applicationTemplateResourceEnd = given().contentType(MediaType.APPLICATION_JSON)
                    .`when`()
                    .get("/api/resources/application-templates/{name}", name)
                    .then()
                    .statusCode(200)
                    .contentType(MediaType.APPLICATION_JSON)
                    .extract().`as`(ApplicationTemplateResource::class.java)
                val status = applicationTemplateResourceEnd.status
            } while (status == null || status.instances != 1 || !status.devices.all { it.value == "up-to-date - Successfully Reconciled" })

            val applicationResourceEnd = given().contentType(MediaType.APPLICATION_JSON)
                .`when`()
                .get("/api/resources/applications/{name}", name)
                .then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON)
                .extract().`as`(ApplicationResource::class.java)
            assertThat(applicationResourceEnd.spec.inputs["location"], `is`("test-location"))
        }
    }
}
