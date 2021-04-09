package io.github.legion2.tosca_orchestrator.api

import io.github.legion2.tosca_orchestrator.orchestrator.model.Application
import io.github.legion2.tosca_orchestrator.orchestrator.model.ApplicationResource
import io.github.legion2.tosca_orchestrator.orchestrator.model.Metadata
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import java.nio.file.Path
import javax.ws.rs.core.MediaType

@QuarkusTest
internal class ApplicationResourceTest {

    @Test
    fun testValidation() {
        given().contentType("application/yaml")
            .body(javaClass.getResourceAsStream("../tosca/model/sensor.yaml"))
            .`when`()
            .post("/api/orchestrator/validate")
            .then()
            .statusCode(200)
            .contentType("application/yaml")
    }

    @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Does not work, reason unknown")
    @Test
    fun testOrchestration() = runBlocking {
        val name = "testOrchestration"
        val uri =
            Path.of("src/test/resources/io/github/legion2/tosca_orchestrator/tosca/model/sensor.yaml")
                .toUri()

        val inputs = mapOf("MQTT_URL" to "", "MQTT_User" to "", "MQTT_Password" to "", "location" to  "")
        val application = Application(uri, inputs)
        val applicationResource = given().contentType(MediaType.APPLICATION_JSON)
            .body(ApplicationResource(Metadata(name), application))
            .`when`()
            .post("/api/resources/applications/{name}", name)
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .extract().`as`(ApplicationResource::class.java)

        assertThat(applicationResource.metadata.name, `is`(name))

        withTimeout(10_000) {
            do {
                delay(100)
                val applicationResourceEnd = given().contentType(MediaType.APPLICATION_JSON)
                    .`when`()
                    .get("/api/resources/applications/{name}", name)
                    .then()
                    .statusCode(200)
                    .contentType(MediaType.APPLICATION_JSON)
                    .extract().`as`(ApplicationResource::class.java)
                val status = applicationResourceEnd.status
            } while (status == null || status.nodes.size != 3 || !status.nodes.all { it.value == "Successfully reconciled" })
        }
    }

    @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Does not work, reason unknown")
    @Test
    fun testCreateDelete() = runBlocking {
        val name = "example2"
        val uri =
            Path.of("src/test/resources/io/github/legion2/tosca_orchestrator/tosca/model/sensor.yaml")
                .toUri()

        val inputs = mapOf("MQTT_URL" to "", "MQTT_User" to "", "MQTT_Password" to "", "location" to "")
        val application = Application(uri, inputs)
        given().contentType(MediaType.APPLICATION_JSON)
            .body(ApplicationResource(Metadata(name), application))
            .`when`()
            .post("/api/resources/applications/{name}", name)
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .extract().`as`(ApplicationResource::class.java)


        delay(500)

        given().contentType(MediaType.APPLICATION_JSON)
            .`when`()
            .delete("/api/resources/applications/{name}", name)
            .then()
            .statusCode(204)

        withTimeout(10_000) {
            do {
                delay(100)
                val result = kotlin.runCatching {
                    given().contentType(MediaType.APPLICATION_JSON)
                        .`when`()
                        .get("/api/resources/applications/{name}", name)
                        .then()
                        .statusCode(404)
                }
            } while (result.isFailure)
        }
    }
}
