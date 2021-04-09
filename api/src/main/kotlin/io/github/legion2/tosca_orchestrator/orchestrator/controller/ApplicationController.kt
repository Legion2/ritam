package io.github.legion2.tosca_orchestrator.orchestrator.controller

import io.github.legion2.tosca_orchestrator.orchestrator.InstanceModelService
import io.github.legion2.tosca_orchestrator.orchestrator.add
import io.github.legion2.tosca_orchestrator.orchestrator.artifact.ReconcilerLifecycleInterface
import io.github.legion2.tosca_orchestrator.orchestrator.controller.common.*
import io.github.legion2.tosca_orchestrator.orchestrator.evaluation.StaticEvaluationContext
import io.github.legion2.tosca_orchestrator.orchestrator.filterNullValues
import io.github.legion2.tosca_orchestrator.orchestrator.model.*
import io.github.legion2.tosca_orchestrator.orchestrator.remove
import io.github.legion2.tosca_orchestrator.orchestrator.storage.ApplicationStorage
import io.github.legion2.tosca_orchestrator.orchestrator.storage.ComponentStorage
import io.github.legion2.tosca_orchestrator.tosca.model.addExceptionContextInfo
import io.github.legion2.tosca_orchestrator.tosca.model.instance.InterfaceInstance
import io.github.legion2.tosca_orchestrator.tosca.model.instance.NodeInstance
import io.github.legion2.tosca_orchestrator.tosca.model.instance.OperationInstance
import io.github.legion2.tosca_orchestrator.tosca.model.instance.TopologyInstance
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.Duration
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes
import javax.inject.Inject

@ApplicationScoped
class ApplicationController {

    private val name = "application-controller"

    @Inject
    protected lateinit var componentStore: ComponentStorage

    @Inject
    protected lateinit var applicationStore: ApplicationStorage

    @Inject
    protected lateinit var instanceModelService: InstanceModelService

    private lateinit var baseController: BaseController

    private val scope = CoroutineScope(Dispatchers.Default)

    fun onStart(@Observes ev: StartupEvent?) {
        baseController = BaseController(this::reconcile, name)
        scope.launchWatch(applicationStore, baseController.workQueue, enqueueForController())
        scope.launchWatch(componentStore, baseController.workQueue, enqueueForParent(), filterCreator(name), watchStatusOnly = true)
        scope.launch { baseController.run() }
    }

    fun onStop(@Observes ev: ShutdownEvent?) {
        baseController.shutdown()
        scope.cancel()
    }

    private fun reconcile(request: Request): Result {
        val resource = applicationStore.getResource(request.name) ?: return Result(false)
        if (resource.metadata.deletion) {
            return handleDelete(resource)
        }

        if (!resource.metadata.finalizers.contains(name)) {
            applicationStore.putResource(ApplicationResource.metadata.finalizers.add(resource, name))
            applicationStore.updateStatus(resource, ApplicationStatus("Added Finalizer"))
            return Result(true)
        }

        return handleApplication(resource)
    }

    private fun handleDelete(resource: ApplicationResource): Result {
        if (!resource.metadata.finalizers.contains(name)) {
            return Result(false)
        }
        val componentResources = componentStore.getResources(
                mapOf(
                        creatorLabel(name),
                        applicationLabel(resource)
                )
        )
        for (componentResource in componentResources) {
            componentStore.deleteResource(componentResource.metadata.name)
        }

        if (componentResources.isNotEmpty()) {
            applicationStore.updateStatus(
                    resource,
                    ApplicationStatus("Waiting for child resource deletion: ${componentResources.map { it.metadata.name }}")
            )
            return Result(true, Duration.ofMillis(100))
        }
        applicationStore.putResource(ApplicationResource.metadata.finalizers.remove(resource, name))
        applicationStore.updateStatus(resource, ApplicationStatus("Removed Finalizer"))
        return Result(false)
    }


    private fun handleApplication(resource: ApplicationResource): Result {
        return kotlin.runCatching {
            val topologyInstance = instanceModelService.getInstanceModel(resource)

            val staticEvaluationContext = StaticEvaluationContext(
                    topologyInstance.inputs,
                    topologyInstance.nodes.associateBy { it.name },
                    topologyInstance.relationships.associateBy { it.name })
            val nodes = topologyInstance.nodes.associate { node ->
                node.name to handleComponent(resource, node, topologyInstance, staticEvaluationContext)
            }.mapValues { it.value.status?.message.orEmpty() }
            applicationStore.updateStatus(resource, ApplicationStatus(
                    "Successfully Reconciled",
                    nodes = nodes,
                    //topologyInstance.outputs.mapValues { staticEvaluationContext.evaluate(it.value) }
            ))
            Result(false)
        }.getOrElse {
            applicationStore.updateStatus(resource, ApplicationStatus(it.message.orEmpty()))
            throw it
        }
    }

    private fun handleComponent(
            resource: ApplicationResource,
            nodeInstance: NodeInstance,
            topologyInstance: TopologyInstance,
            staticEvaluationContext: StaticEvaluationContext
    ): ComponentResource = kotlin.runCatching {
        val applicationName = resource.metadata.name

        val dependencies = topologyInstance.relationships.filter { it.source == nodeInstance.name }
                .map { componentResourceName(applicationName, it.target) }

        val reconciler = reconcilerFrom(
                nodeInstance.getReconcilerInterface(),
                staticEvaluationContext.copy(nodeSelf = nodeInstance)
        )
        val component = Component(reconciler, dependencies)

        val componentResource = componentStore.getResource(componentResourceName(applicationName, nodeInstance.name))
        if (componentResource == null) {
            componentStore.postResource(ComponentResource(
                    Metadata(
                            componentResourceName(applicationName, nodeInstance.name),
                            mapOf(applicationLabel(resource), "component" to nodeInstance.name, creatorLabel(name)),
                            parentRef = parentRef(resource)
                    ),
                    component
            ))
        } else {
            componentStore.putResource(ComponentResource.spec.modify(componentResource) {
                Component.reconciler.set(Component.dependencies.set(it, dependencies), reconciler)
            })
        }
    }.addExceptionContextInfo { "Exception during Component reconciliation: ${nodeInstance.name}" }

    private fun applicationLabel(resource: ApplicationResource) = "application" to resource.metadata.name
    private fun componentResourceName(serviceName: String, nodeName: String) = "$serviceName-$nodeName"
    private fun InstanceModelService.getInstanceModel(resource: ApplicationResource) =
            getInstanceModel(resource.spec.serviceTemplate, resource.spec.inputs)
}

private fun NodeInstance.getReconcilerInterface(): InterfaceInstance? {
    return interfaces[ReconcilerLifecycleInterface.name]
}

private fun ApplicationStorage.updateStatus(resource: ApplicationResource, status: ApplicationStatus) =
        putResourceStatus(resource.metadata.name, status)

fun reconcilerFrom(reconcilerInterface: InterfaceInstance?, evaluationContext: StaticEvaluationContext): Reconciler {
    reconcilerInterface ?: return Reconciler(
            ReconcileFunction(),
            ReconcileFunction()
    )

    val inputs =
            reconcilerInterface.inputs.mapValues { it.value.expression?.let { it1 -> evaluationContext.evaluate(it1) } }
                    .filterNullValues()

    val reconcileOperation = reconcilerInterface.operations[ReconcilerLifecycleInterface.reconcileOperation]
            ?: throw IllegalArgumentException("Reconcile Operation not defined in Reconciler Interface")
    val deletionOperation = reconcilerInterface.operations[ReconcilerLifecycleInterface.deletionOperation]
            ?: throw IllegalArgumentException("Deletion Operation not defined in Reconciler Interface")
    return Reconciler(
            reconcileFunctionFrom(reconcileOperation, inputs, evaluationContext),
            reconcileFunctionFrom(deletionOperation, inputs, evaluationContext)
    )
}

fun reconcileFunctionFrom(
        reconcileOperation: OperationInstance?,
        inputs: Map<String, DynamicExpression>,
        evaluationContext: StaticEvaluationContext
): ReconcileFunction {
    reconcileOperation ?: return ReconcileFunction()

    val inputOverrides =
            reconcileOperation.inputs.mapValues { it.value.expression?.let { it1 -> evaluationContext.evaluate(it1) } }
                    .filterNullValues()

    val allInputs = inputs + inputOverrides
    val artifact = reconcileOperation.implementation.primary
    val dependencies = reconcileOperation.implementation.dependencies
    val timeout = reconcileOperation.implementation.timeout?.toLong()?.let { Duration.ofSeconds(it) }
    val outputs = reconcileOperation.outputs
    return ReconcileFunction(artifact, allInputs, outputs, timeout, dependencies)
}
