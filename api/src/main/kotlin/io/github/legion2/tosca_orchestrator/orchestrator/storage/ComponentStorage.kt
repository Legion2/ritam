package io.github.legion2.tosca_orchestrator.orchestrator.storage

import io.github.legion2.tosca_orchestrator.orchestrator.model.*
import io.github.legion2.tosca_orchestrator.orchestrator.storage.common.BaseResourceStorage
import io.github.legion2.tosca_orchestrator.orchestrator.storage.common.ResourceStorage
import io.quarkus.scheduler.Scheduled
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class ComponentStorage :
    ResourceStorage<Component, ComponentStatus, ComponentResource> by BaseResourceStorage(ComponentResource.metadata, ComponentResource.nullableStatus) {
    @Scheduled(every = "1s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    protected fun doGarbageCollection() {
        garbageCollection()
    }
}
