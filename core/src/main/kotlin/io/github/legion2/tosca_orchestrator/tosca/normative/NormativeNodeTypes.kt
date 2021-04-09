package io.github.legion2.tosca_orchestrator.tosca.normative

import io.github.legion2.tosca_orchestrator.tosca.model.Namespace
import io.github.legion2.tosca_orchestrator.tosca.model.TypeReference
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedNodeType

private val normativeNodeTypeDefinitions
    get() = listOf(
        "tosca.nodes.Abstract.Compute",
        "tosca.nodes.Abstract.Storage",
        "tosca.nodes.Compute",
        "tosca.nodes.Container.Application",
        "tosca.nodes.Container.Runtime",
        "tosca.nodes.DBMS",
        "tosca.nodes.Database",
        "tosca.nodes.LoadBalancer",
        "tosca.nodes.Root",
        "tosca.nodes.SoftwareComponent",
        "tosca.nodes.Storage.BlockStorage",
        "tosca.nodes.Storage.ObjectStorage",
        "tosca.nodes.WebApplication",
        "tosca.nodes.WebServer"
    )

val nodesRootType = TypeReference<ResolvedNodeType>(Namespace.tosca, "tosca.nodes.Root")

val nodeTypeShortNameAliases = normativeNodeTypeDefinitions.associateBy { it.removePrefix("tosca.nodes.") }

enum class NormativeNodeStates(val value: String) {
    INITIAL("initial"),
    CREATING("creating"),
    CREATED("created"),
    CONFIGURING("configuring"),
    CONFIGURED("configured"),
    STARTING("starting"),
    STARTED("started"),
    STOPPING("stopping"),
    DELETING("deleting"),
    ERROR("error")
}
