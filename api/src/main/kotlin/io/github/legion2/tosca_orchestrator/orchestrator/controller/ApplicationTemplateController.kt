package io.github.legion2.tosca_orchestrator.orchestrator.controller

import io.github.legion2.tosca_orchestrator.client.DeviceApplicationClient
import io.github.legion2.tosca_orchestrator.orchestrator.add
import io.github.legion2.tosca_orchestrator.orchestrator.controller.common.*
import io.github.legion2.tosca_orchestrator.orchestrator.model.*
import io.github.legion2.tosca_orchestrator.orchestrator.remove
import io.github.legion2.tosca_orchestrator.orchestrator.storage.ApplicationTemplateStorage
import io.github.legion2.tosca_orchestrator.orchestrator.storage.DeviceStorage
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.apache.commons.text.StringSubstitutor
import org.eclipse.microprofile.rest.client.RestClientBuilder
import java.time.Duration
import java.time.OffsetDateTime
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes
import javax.inject.Inject
import javax.ws.rs.WebApplicationException

@ApplicationScoped
class ApplicationTemplateController {

    private val name = "application-template-controller"

    @Inject
    protected lateinit var deviceStorage: DeviceStorage

    @Inject
    protected lateinit var applicationTemplateStorage: ApplicationTemplateStorage

    private lateinit var baseController: BaseController

    private val scope = CoroutineScope(Dispatchers.Default)

    fun onStart(@Observes ev: StartupEvent?) {
        baseController = BaseController(this::reconcile, name)
        scope.launchWatch(
                applicationTemplateStorage,
                baseController.workQueue,
                enqueueForController(),
                syncPeriod = Duration.ofSeconds(60)
        )
        scope.launch { baseController.run() }
    }

    fun onStop(@Observes ev: ShutdownEvent?) {
        baseController.shutdown()
        scope.cancel()
    }

    private fun getDevices(applicationTemplateResource: ApplicationTemplateResource): List<DeviceResource> {
        return deviceStorage.getResources(applicationTemplateResource.spec.deviceSelector)
    }

    private fun delete(device: DeviceResource, applicationTemplateResource: ApplicationTemplateResource) {
        device.client { it.deleteResource(applicationTemplateResource.metadata.name) }
    }

    private fun <T> DeviceResource.client(block: (DeviceApplicationClient) -> T): T {
        val client = RestClientBuilder.newBuilder()
                .baseUri(spec.url)
                .build(DeviceApplicationClient::class.java)
        return (client as AutoCloseable).use { block.invoke(client) }
    }


    private fun reconcile(request: Request): Result {
        val resource = applicationTemplateStorage.getResource(request.name) ?: return Result(false)
        if (resource.metadata.deletion) {
            return handleDelete(resource)
        }

        if (!resource.metadata.finalizers.contains(name)) {
            applicationTemplateStorage.putResource(ApplicationTemplateResource.metadata.finalizers.add(resource, name))
            applicationTemplateStorage.updateStatus(
                    resource,
                    ApplicationTemplateStatus("Added Finalizer", OffsetDateTime.now(), 0)
            )
            return Result(true)
        }

        return handleApplicationTemplate(resource)
    }

    private fun handleDelete(resource: ApplicationTemplateResource): Result {
        if (!resource.metadata.finalizers.contains(name)) {
            return Result(false)
        }
        val devices = getDevices(resource)
        val runningApplications = devices.mapNotNull { deviceResource ->
            val application = deviceResource.client { it.getResourceOrNull(resource.metadata.name) }

            if (application != null) {
                delete(deviceResource, resource)
            }
            application
        }

        if (runningApplications.isNotEmpty()) {
            applicationTemplateStorage.updateStatus(
                    resource,
                    ApplicationTemplateStatus(
                            "Waiting for child resource deletion: ${devices.map { it.metadata.name }}",
                            OffsetDateTime.now(),
                            runningApplications.size
                    )
            )
            return Result(true, Duration.ofMillis(100))
        }

        applicationTemplateStorage.putResource(ApplicationTemplateResource.metadata.finalizers.remove(resource, name))
        applicationTemplateStorage.updateStatus(
                resource,
                ApplicationTemplateStatus("Removed Finalizer", OffsetDateTime.now(), 0)
        )
        return Result(false)
    }

    private fun handleApplicationTemplate(resource: ApplicationTemplateResource): Result {
        val devices = getDevices(resource)
        val deviceStatuses = devices.associate { device ->
            device.metadata.name to kotlin.runCatching {
                val stringSubstitutor = StringSubstitutor(device.spec.properties.mapKeys { "device.${it.key}" })
                val inputs = resource.spec.inputs.mapValues { stringSubstitutor.replace(it.value) }

                val applicationResource = device.client { it.getResourceOrNull(resource.metadata.name) }
                val application = Application(
                        resource.spec.serviceTemplate,
                        inputs
                )
                if (applicationResource == null) {
                    device.client {
                        it.createResource(
                                applicationName(resource),
                                ApplicationResource(
                                        Metadata(applicationName(resource), mapOf(creatorLabel(name))),
                                        application
                                )
                        )
                    }
                    "created"
                } else {
                    val updatedApplication = device.client {
                        it.updateResource(
                                applicationName(resource),
                                ApplicationResource.spec.set(applicationResource, application)
                        )
                    }
                    val applicationMessage = updatedApplication.status?.message.orEmpty()
                    if (updatedApplication.metadata.resourceVersion == applicationResource.metadata.resourceVersion)
                        "up-to-date - $applicationMessage"
                    else
                        "updated - $applicationMessage"
                }
            }.getOrElse {
                it.message.orEmpty()
            }
        }
        applicationTemplateStorage.updateStatus(
                resource,
                ApplicationTemplateStatus("Successfully reconciled", OffsetDateTime.now(), deviceStatuses.size, deviceStatuses)
        )
        return Result(true, Duration.ofSeconds(5))
    }

    private fun DeviceApplicationClient.getResourceOrNull(name: String): ApplicationResource? {
        return try {
            getResource(name)
        } catch (e: WebApplicationException) {
            if (e.response.status == 404) {
                null
            } else throw e
        }
    }
}

private fun applicationName(resource: ApplicationTemplateResource) = resource.metadata.name

private fun ApplicationTemplateStorage.updateStatus(
        resource: ApplicationTemplateResource,
        status: ApplicationTemplateStatus
) =
        putResourceStatus(resource.metadata.name, status)

