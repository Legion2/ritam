package io.github.legion2.tosca_orchestrator.orchestrator.controller

import io.github.legion2.tosca_orchestrator.orchestrator.add
import io.github.legion2.tosca_orchestrator.orchestrator.controller.common.*
import io.github.legion2.tosca_orchestrator.orchestrator.model.DeviceResource
import io.github.legion2.tosca_orchestrator.orchestrator.model.DeviceStatus
import io.github.legion2.tosca_orchestrator.orchestrator.model.finalizers
import io.github.legion2.tosca_orchestrator.orchestrator.model.metadata
import io.github.legion2.tosca_orchestrator.orchestrator.remove
import io.github.legion2.tosca_orchestrator.orchestrator.storage.DeviceStorage
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes
import javax.inject.Inject
import javax.ws.rs.client.ClientBuilder

@ApplicationScoped
class DeviceController {

    private val name = "device-controller"

    @Inject
    protected lateinit var deviceStorage: DeviceStorage

    private val client = ClientBuilder.newBuilder()
        .connectTimeout(1, TimeUnit.SECONDS)
        .build()

    private lateinit var baseController: BaseController

    private val scope = CoroutineScope(Dispatchers.Default)

    fun onStart(@Observes ev: StartupEvent?) {
        baseController = BaseController(this::reconcile, name)
        scope.launchWatch(deviceStorage, baseController.workQueue, enqueueForController())
        scope.launch { baseController.run() }
    }

    fun onStop(@Observes ev: ShutdownEvent?) {
        baseController.shutdown()
        scope.cancel()
    }

    private fun reconcile(request: Request): Result {
        val resource = deviceStorage.getResource(request.name) ?: return Result(false)
        if (resource.metadata.deletion) {
            return handleDelete(resource)
        }

        if (!resource.metadata.finalizers.contains(name)) {
            deviceStorage.putResource(DeviceResource.metadata.finalizers.add(resource, name))
            deviceStorage.updateStatus(resource, "Added Finalizer")
            return Result(true)
        }

        return handleDevice(resource)
    }

    private fun handleDelete(resource: DeviceResource): Result {
        if (!resource.metadata.finalizers.contains(name)) {
            return Result(false)
        }
        deviceStorage.putResource(DeviceResource.metadata.finalizers.remove(resource, name))
        deviceStorage.updateStatus(resource, "Removed Finalizer")
        return Result(false)
    }

    private fun handleDevice(resource: DeviceResource): Result {
        val deviceStatus = kotlin.runCatching {
            val healthCheck = client
                .target(resource.spec.url).path("q/health")
                .request()
                .get(HealthCheck::class.java)
            healthCheck.status
        }.getOrElse { it.message.orEmpty() }

        deviceStorage.updateStatus(resource, deviceStatus)

        return Result(true, Duration.ofSeconds(10))
    }
}

data class HealthCheck(val status: String, val checks: List<String>)

private fun DeviceStorage.updateStatus(resource: DeviceResource, status: String) =
    putResourceStatus(resource.metadata.name, DeviceStatus(status, OffsetDateTime.now()))


