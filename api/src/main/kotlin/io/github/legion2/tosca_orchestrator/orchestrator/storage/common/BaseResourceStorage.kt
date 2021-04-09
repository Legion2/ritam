package io.github.legion2.tosca_orchestrator.orchestrator.storage.common

import arrow.optics.Lens
import io.github.legion2.tosca_orchestrator.orchestrator.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import org.jboss.logging.Logger
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.NoSuchElementException

@ExperimentalCoroutinesApi
open class BaseResourceStorage<T, S, R : Resource<T, S>>(private val metadataLens: Lens<R, Metadata>, private val statusLens: Lens<R, S?>) :
        ResourceStorage<T, S, R> {
    val LOG: Logger = Logger.getLogger(BaseResourceStorage::class.java)
    private val resources = ConcurrentHashMap<String, R>()

    private val changes = BroadcastChannel<R>(Channel.BUFFERED)
    private val statusChanges = BroadcastChannel<R>(Channel.BUFFERED)

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun postResource(resource: R): R {
        LOG.debug("Post Resource: ${resource.metadata.name}")
        val resourceWithVersion = metadataLens.nullableResourceVersion.modify(resource) {
            if (it != null) {
                throw IllegalArgumentException("Resource version must not be set!")
            }
            UUID.randomUUID().toString()
        }
        resources.compute(resourceWithVersion.metadata.name) { _, oldResource ->
            if (oldResource == null) {
                resourceWithVersion
            } else {
                throw IllegalStateException("Resource ${resourceWithVersion.metadata.name} already exists.")
            }
        }
        scope.launch { changes.send(resourceWithVersion) }
        return resourceWithVersion
    }

    override fun putResource(resource: R): R {
        LOG.debug("Put Resource: ${resource.metadata.name}")
        val quickCheckResource = resources[resource.metadata.name]
        if (quickCheckResource != null && statusLens.set(quickCheckResource, null) == statusLens.set(resource, null)) {
            return resource
        }

        val resourceVersion = resource.metadata.resourceVersion
        val resourceWithNewVersion = metadataLens.resourceVersion.set(resource, UUID.randomUUID().toString())
        resources.computeIfPresent(resourceWithNewVersion.metadata.name) { _, currentResource ->
            if (currentResource.metadata.resourceVersion != resourceVersion) {
                throw IllegalStateException("Resource version conflict, please try again with new version")
            }
            statusLens.set(resourceWithNewVersion, currentResource.status)
        } ?: throw NoSuchElementException("Resource ${resourceWithNewVersion.metadata.name} does not exist.")
        scope.launch { changes.send(resourceWithNewVersion) }
        return resourceWithNewVersion
    }

    override fun putResourceStatus(name: String, status: S) {
        LOG.debug("Put Resource Status: $name")
        if (resources[name]?.status == status) {
            return
        }

        val resource = resources.computeIfPresent(name) { _, currentResource ->
            val resourceWithNewVersion = metadataLens.resourceVersion.set(currentResource, UUID.randomUUID().toString())
            statusLens.set(resourceWithNewVersion, status)
        } ?: throw NoSuchElementException("Resource $name does not exist.")
        scope.launch { statusChanges.send(resource) }
    }

    override fun getResource(name: String): R? {
        return resources[name]
    }

    override fun getResources(labels: Map<String, String>): List<R> {
        return resources.values.filter { resource -> labels.all { resource.metadata.labels.containsKey(it.key) && resource.metadata.labels[it.key] == it.value } }
    }

    override fun deleteResource(name: String) {
        LOG.debug("Delete Resource: $name")
        if (resources[name]?.metadata?.deletion != false) {
            return
        }

        val newResourceVersion = UUID.randomUUID().toString()
        val remove = resources.computeIfPresent(name) { _, resource ->
            metadataLens.resourceVersion.set(metadataLens.deletion.set(resource, true), newResourceVersion)
        } ?: return
        scope.launch { changes.send(remove) }
    }

    override fun subscribe(): ReceiveChannel<R> {
        return changes.openSubscription()
    }

    override fun subscribeStatus(): ReceiveChannel<R> {
        return statusChanges.openSubscription()
    }

    override fun garbageCollection() {
        val garbage = resources.filterValues { it.metadata.deletion && it.metadata.finalizers.isEmpty() }.keys

        garbage.forEach {
            resources.remove(it)
        }
    }
}
