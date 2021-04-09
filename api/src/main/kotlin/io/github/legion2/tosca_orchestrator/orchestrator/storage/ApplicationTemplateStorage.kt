package io.github.legion2.tosca_orchestrator.orchestrator.storage

import io.github.legion2.tosca_orchestrator.orchestrator.model.*
import io.github.legion2.tosca_orchestrator.orchestrator.storage.common.BaseResourceStorage
import io.github.legion2.tosca_orchestrator.orchestrator.storage.common.ResourceStorage
import io.github.legion2.tosca_orchestrator.tosca.model.instance.TopologyInstance
import io.quarkus.scheduler.Scheduled
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class ApplicationTemplateStorage :
    ResourceStorage<ApplicationTemplate, ApplicationTemplateStatus, ApplicationTemplateResource> by BaseResourceStorage(ApplicationTemplateResource.metadata, ApplicationTemplateResource.nullableStatus) {
    @Scheduled(every = "1s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    protected fun doGarbageCollection() {
        garbageCollection()
    }
}
