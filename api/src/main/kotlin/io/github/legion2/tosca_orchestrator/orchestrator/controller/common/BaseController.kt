package io.github.legion2.tosca_orchestrator.orchestrator.controller.common

import io.github.legion2.tosca_orchestrator.orchestrator.model.Resource
import io.github.legion2.tosca_orchestrator.orchestrator.storage.common.ResourceStorage
import io.kubernetes.client.extended.workqueue.DefaultRateLimitingQueue
import io.kubernetes.client.extended.workqueue.RateLimitingQueue
import io.kubernetes.client.extended.workqueue.WorkQueue
import kotlinx.coroutines.*
import org.jboss.logging.Logger
import java.time.Duration


typealias Reconcile = (Request) -> Result

class BaseController(
    private val reconcile: Reconcile,
    val name: String,
    val workQueue: RateLimitingQueue<Request> = DefaultRateLimitingQueue()
) {
    private val LOG: Logger = Logger.getLogger(BaseController::class.java)
    suspend fun run() {
        supervisorScope {
            worker()
        }
    }

    private fun worker() {
        while (!workQueue.isShuttingDown) {
            val request = try {
                workQueue.get()
            } catch (e: InterruptedException) {
                // we're reaching here mostly because of forcibly shutting down the controller.
                LOG.error("Controller worker interrupted..", e)
                continue
            } ?: continue

            LOG.debug("Controller $name reconciling $request")
            val result = try {
                reconcile.invoke(request)
            } catch (t: Throwable) {
                LOG.error("Reconcile aborted unexpectedly", t)
                Result(true)
            }
            try {
                // checks whether do a re-queue (on failure)
                if (result.requeue) {
                    if (result.requeueAfter == Duration.ZERO) {
                        val retries = workQueue.numRequeues(request)
                        LOG.debug("Controller $name requeuing $request after $retries retries with rate limit..")
                        workQueue.addRateLimited(request)
                    } else {
                        LOG.debug("Controller $name requeuing $request after ${result.requeueAfter}..")
                        workQueue.addAfter(request, result.requeueAfter)
                    }
                } else {
                    workQueue.forget(request)
                }
            } finally {
                workQueue.done(request)
                LOG.debug("Controller $name finished reconciling $request..")
            }
        }
    }

    fun shutdown() {
        workQueue.shutDown()
    }
}

fun <T, S, R : Resource<T, S>> CoroutineScope.launchWatch(
        resourceStorage: ResourceStorage<T, S, R>,
        queue: WorkQueue<Request>,
        keyFunc: (Resource<T, S>) -> Request,
        filter: (Resource<T, S>) -> Boolean = { true },
        syncPeriod: Duration = Duration.ofMinutes(5),
        watchStatusOnly: Boolean = false
) = launch {
    launch {
        while (isActive) {
            resourceStorage.getResources(emptyMap()).filter(filter).map(keyFunc).forEach(queue::add)
            delay(syncPeriod.toMillis())
        }
    }
    val subscribe = if (watchStatusOnly) resourceStorage::subscribeStatus else resourceStorage::subscribe
    for (resource in subscribe()) {
        if (filter.invoke(resource)) {
            queue.add(keyFunc.invoke(resource))
        }
    }
}

fun <T, S> enqueueForParent(): (Resource<T, S>) -> Request = { Request(it.metadata.parentRef!!) }
fun <T, S> parentRef(resource: Resource<T, S>) = resource.metadata.name

fun <T, S> enqueueForController(): (Resource<T, S>) -> Request = { Request(it.metadata.name) }
const val creator = "creator"
fun <T, S> filterCreator(name: String): (Resource<T, S>) -> Boolean = { it.metadata.labels[creator] == name }

fun creatorLabel(name: String) = creator to name
