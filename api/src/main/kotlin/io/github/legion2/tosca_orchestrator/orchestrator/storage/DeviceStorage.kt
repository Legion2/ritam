package io.github.legion2.tosca_orchestrator.orchestrator.storage

import io.github.legion2.tosca_orchestrator.orchestrator.model.*
import io.github.legion2.tosca_orchestrator.orchestrator.storage.common.BaseResourceStorage
import io.github.legion2.tosca_orchestrator.orchestrator.storage.common.ResourceStorage
import io.quarkus.scheduler.Scheduled
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class DeviceStorage :
    ResourceStorage<Device, DeviceStatus, DeviceResource> by BaseResourceStorage(DeviceResource.metadata, DeviceResource.nullableStatus) {
    @Scheduled(every = "1s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    protected fun doGarbageCollection() {
        garbageCollection()
    }
}
