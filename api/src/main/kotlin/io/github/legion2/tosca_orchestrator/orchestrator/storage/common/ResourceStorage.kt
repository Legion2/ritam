package io.github.legion2.tosca_orchestrator.orchestrator.storage.common

import io.github.legion2.tosca_orchestrator.orchestrator.model.Resource
import kotlinx.coroutines.channels.ReceiveChannel

interface ResourceStorage<T, S, R : Resource<T, S>> {
    /**
     * @throws IllegalStateException if resource already exists
     * @throws IllegalArgumentException if resource version is set
     */
    fun postResource(resource: R): R

    /**
     * @throws IllegalStateException if the resource version do not match
     * @throws NoSuchElementException if the resource does not exists
     */
    fun putResource(resource: R): R
    /**
     * @throws NoSuchElementException if the resource does not exists
     */
    fun putResourceStatus(name: String, status: S)
    fun getResource(name: String): R?
    fun getResources(labels: Map<String, String>): List<R>
    fun deleteResource(name: String)
    fun subscribe(): ReceiveChannel<R>
    fun subscribeStatus(): ReceiveChannel<R>
    fun garbageCollection()
}


