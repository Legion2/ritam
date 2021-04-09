package io.github.legion2.tosca_orchestrator.orchestrator.storage.common

import io.github.legion2.tosca_orchestrator.orchestrator.model.*
import io.github.legion2.tosca_orchestrator.orchestrator.storage.ApplicationTemplateStorage
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.OffsetDateTime

internal class BaseResourceStorageTest {

    val name = "test-resource"

    @Test
    fun postResource() {
        val storage = ApplicationTemplateStorage()
        val resource = storage.postResource(
            ApplicationTemplateResource(
                Metadata(
                    name,
                    emptyMap()
                ),
                ApplicationTemplate(emptyMap(), emptyMap(), URI.create(""))
            )
        )
        Assertions.assertEquals(name, resource.metadata.name)
        Assertions.assertEquals(false, resource.metadata.deletion)
        Assertions.assertEquals(true, resource.metadata.finalizers.isEmpty())
        Assertions.assertEquals(false, resource.metadata.resourceVersion.isNullOrBlank())
    }

    @Test
    fun putResource() {
        val storage = ApplicationTemplateStorage()
        val resource = storage.postResource(
            ApplicationTemplateResource(
                Metadata(
                    name,
                    emptyMap()
                ),
                ApplicationTemplate(emptyMap(), emptyMap(), URI.create(""))
            )
        )

        val updatedResource =
            storage.putResource(ApplicationTemplateResource.spec.serviceTemplate.set(resource, URI.create("foo")))
        Assertions.assertEquals(false, resource.metadata.resourceVersion == updatedResource.metadata.resourceVersion)
    }

    @Test
    fun putResourceWithoutChange() {
        val storage = ApplicationTemplateStorage()
        val resource = storage.postResource(
            ApplicationTemplateResource(
                Metadata(
                    name,
                    emptyMap()
                ),
                ApplicationTemplate(emptyMap(), emptyMap(), URI.create(""))
            )
        )

        val unchangedResource = storage.putResource(
            ApplicationTemplateResource.spec.set(resource, ApplicationTemplate(emptyMap(), emptyMap(), URI.create("")))
        )
        Assertions.assertEquals(
            true, resource.metadata.resourceVersion == unchangedResource.metadata.resourceVersion
        )
    }

    @Test
    fun putResourceWithoutStatusChange() {
        val storage = ApplicationTemplateStorage()
        val lastReconciled = OffsetDateTime.now()
        val resource = storage.postResource(
            ApplicationTemplateResource(
                Metadata(
                    name,
                    emptyMap()
                ),
                ApplicationTemplate(emptyMap(), emptyMap(), URI.create("")),
                ApplicationTemplateStatus("message", lastReconciled,10, mapOf("foo" to "bar"))
            )
        )

        val unchangedResource = storage.putResource(
            ApplicationTemplateResource.status.set(
                resource,
                ApplicationTemplateStatus("message", lastReconciled,10, mapOf("foo" to "bar"))
            )
        )
        Assertions.assertEquals(
            true, resource.metadata.resourceVersion == unchangedResource.metadata.resourceVersion
        )
    }
}