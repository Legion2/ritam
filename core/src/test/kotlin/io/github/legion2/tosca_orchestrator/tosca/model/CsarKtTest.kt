package io.github.legion2.tosca_orchestrator.tosca.model

import io.github.legion2.tosca_orchestrator.tosca.model.instance.createInstanceModel
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.io.path.deleteExisting

internal class CsarKtTest {

    @Test
    fun testLoadMetadata() {
        val inputStream = this.javaClass.getResource("serviceTemplateExample.csar").openStream()
        val storedCsar = storeCsar(inputStream)
        val metaFile = loadMetadata(storedCsar)
        val uri = storedCsar.resolve("serviceTemplateExample.yaml").toUri()

        MatcherAssert.assertThat(metaFile, `is`(ToscaMetaFile("1.1", "1.1", "Someone", uri)))
        storedCsar.toFile().deleteRecursively()
    }

    @Test
    fun testStoreCsar() {
        val inputStream = this.javaClass.getResource("serviceTemplateExample.csar").openStream()
        val storedCsar = storeCsar(inputStream)

        val path = storedCsar.resolve("serviceTemplateExample.yaml")
        MatcherAssert.assertThat(Files.exists(path), `is`(true))
        storedCsar.toFile().deleteRecursively()
    }

    @Test
    fun testCreateInstanceModelFromCsar() {
        val inputStream = this.javaClass.getResource("serviceTemplateExample.csar").openStream()
        val storedCsar = storeCsar(inputStream)
        val metaFile = loadMetadata(storedCsar)
        createInstanceModel(metaFile.entryDefinitions, emptyMap())
        storedCsar.toFile().deleteRecursively()
    }
}