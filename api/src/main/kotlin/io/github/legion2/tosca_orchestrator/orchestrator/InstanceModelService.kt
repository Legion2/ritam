package io.github.legion2.tosca_orchestrator.orchestrator

import io.github.legion2.tosca_orchestrator.tosca.model.instance.TopologyInstance
import io.github.legion2.tosca_orchestrator.tosca.model.instance.createInstanceModel
import io.quarkus.cache.CacheResult
import java.net.URI
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class InstanceModelService {
    @CacheResult(cacheName = "topology-instances")
    fun getInstanceModel(serviceTemplateFilePath: URI, inputs: Map<String, String>): TopologyInstance {
        return createInstanceModel(serviceTemplateFilePath, inputs)
    }
}