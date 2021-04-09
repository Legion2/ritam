package io.github.legion2.tosca_orchestrator.orchestrator.controller

import io.github.legion2.tosca_orchestrator.orchestrator.add
import io.github.legion2.tosca_orchestrator.orchestrator.artifact.ExecutionEngine
import io.github.legion2.tosca_orchestrator.orchestrator.controller.common.*
import io.github.legion2.tosca_orchestrator.orchestrator.evaluation.ComponentContext
import io.github.legion2.tosca_orchestrator.orchestrator.evaluation.DynamicEvaluationContext
import io.github.legion2.tosca_orchestrator.orchestrator.model.*
import io.github.legion2.tosca_orchestrator.orchestrator.put
import io.github.legion2.tosca_orchestrator.orchestrator.remove
import io.github.legion2.tosca_orchestrator.orchestrator.removeKey
import io.github.legion2.tosca_orchestrator.orchestrator.storage.ComponentStorage
import io.github.legion2.tosca_orchestrator.tosca.model.property.ResolvedPropertyNameOrIndex
import io.github.legion2.tosca_orchestrator.tosca.model.property.Value
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedAttributeMapping
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedEntity
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import kotlinx.coroutines.*
import org.jboss.logging.Logger
import java.time.Duration
import java.time.OffsetDateTime
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes
import javax.inject.Inject

@ApplicationScoped
class ComponentController {
    private val LOG: Logger = Logger.getLogger(ComponentController::class.java)

    private val name = "component-controller"


    @Inject
    protected lateinit var componentStore: ComponentStorage

    @Inject
    protected lateinit var executionEngine: ExecutionEngine

    private lateinit var baseController: BaseController

    private val scope = CoroutineScope(Dispatchers.Default)

    fun onStart(@Observes ev: StartupEvent?) {
        baseController = BaseController(this::reconcile, name)
        scope.launchWatch(componentStore, baseController.workQueue, enqueueForController())
        scope.launch { baseController.run() }
    }

    fun onStop(@Observes ev: ShutdownEvent?) {
        baseController.shutdown()
        scope.cancel()
    }

    private fun reconcile(request: Request): Result {
        val resource = componentStore.getResource(request.name) ?: return Result(false)

        if (resource.metadata.deletion) {
            val dependencies = resource.spec.dependencies.map {
                componentStore.getResource(it) ?: throw IllegalStateException("Dependency does not exist: $it")
            }
            return if (resource.spec.bindings.containsValue(true)) {
                reconcileOperation(resource, dependencies)
            } else {
                componentDeletion(resource, dependencies)
            }
        }
        if (!resource.metadata.finalizers.contains(name)) {
            componentStore.putResource(ComponentResource.metadata.finalizers.add(resource, name))
            componentStore.updateStatus(resource, "Added Finalizer")
            return Result(true)
        }

        return reconcileComponent(resource)
    }

    private fun reconcileComponent(resource: ComponentResource): Result {
        val componentName = resource.metadata.name
        val dependencies = resource.spec.dependencies.map {
            componentStore.getResource(it) ?: throw IllegalStateException("Dependency does not exist: $it")
        }

        dependencies
            .filter { !it.spec.bindings.containsKey(componentName) }
            .forEach {
                componentStore.putResource(ComponentResource.spec.bindings.put(it, componentName, false))
            }

        if (dependencies.any { it.spec.bindings[componentName] != true }) {
            componentStore.updateStatus(resource, "Waiting for dependencies to accept Bindings")
            return Result(true, Duration.ofMillis(100))
        }

        if (!resource.spec.bindings.values.all { it }) {
            val acceptedBindings = ComponentResource.spec.bindings.modify(resource) {
                it.keys.associateWith { true }
            }
            componentStore.putResource(acceptedBindings)
            componentStore.updateStatus(resource, "Accepted all Bindings")
            return Result(true)
        }


        return reconcileOperation(resource, dependencies)
    }

    private fun reconcileOperation(resource: ComponentResource, dependencies: List<ComponentResource>): Result {
        val modifiedResource = componentStore.putResource(ComponentResource.spec.operationFinalizer.set(resource, true))
        val output = kotlin.runCatching {
            runBlocking {
                executionEngine.executeOperation(
                    modifiedResource.spec.reconciler.reconcile,
                    getDynamicEvaluationContext(dependencies)
                )
            }
        }.getOrElse {
            componentStore.updateStatus(modifiedResource, "Reconcile Operation Failed: ${it.message}")
            throw it
        }

        val attributes = outputToAttributes(modifiedResource.spec.reconciler.reconcile, output)

        componentStore.updateStatus(modifiedResource, "Successfully reconciled", attributes)
        return Result(false)
    }

    private fun componentDeletion(resource: ComponentResource, dependencies: List<ComponentResource>): Result {
        val componentName = resource.metadata.name
        if (resource.spec.operationFinalizer) {
            val output = try {
                runBlocking {
                    executionEngine.executeOperation(
                        resource.spec.reconciler.deletion,
                        getDynamicEvaluationContext(dependencies)
                    )
                }
            } catch (e: Throwable) {
                componentStore.updateStatus(resource, "Finalization Operation Failed: ${e.message}")
                return Result(true)
            }

            val attributes = outputToAttributes(resource.spec.reconciler.deletion, output)

            val deleted = (output.getOrDefault(deletedOutputParameterName, Value.Boolean(true))as Value.Boolean).value

            if (!deleted) {
                componentStore.updateStatus(resource, "Finalization in progress", attributes)
                return Result(true)
            }

            componentStore.putResource(ComponentResource.spec.operationFinalizer.set(resource, false))
            componentStore.updateStatus(resource, "Finalization Completed", attributes)
        }

        val removedBindings = resource.spec.dependencies.mapNotNull {
            componentStore.getResource(it)
        }.filter { it.spec.bindings.containsKey(componentName) }
            .map {
                componentStore.putResource(ComponentResource.spec.bindings.removeKey(it, componentName))
            }.isNotEmpty()

        if (removedBindings) {
            componentStore.updateStatus(resource, "Removed Bindings")
        }

        componentStore.putResource(ComponentResource.metadata.finalizers.remove(resource, name))
        componentStore.updateStatus(resource, "Removed Finalizer")
        return Result(false)
    }
}

private const val deletedOutputParameterName =  "deleted"

private fun getDynamicEvaluationContext(dependencies: List<ComponentResource>): DynamicEvaluationContext {
    return DynamicEvaluationContext(
        dependencies.associate { it.metadata.name to ComponentContext(it.status!!.attributes) },
        emptyMap(),
        nodeSelf = null
    )
}

private fun ComponentStorage.updateStatus(resource: ComponentResource, message: String, attributes: Map<String, Value>? = null) =
    putResourceStatus(resource.metadata.name, ComponentStatus(message, OffsetDateTime.now(), attributes ?: resource.status?.attributes.orEmpty()))


private fun ResolvedAttributeMapping.simple(): String {
    if (entity != ResolvedEntity.SELF || this.optional_capability_name != null) {
        throw IllegalArgumentException("Invalid Attribute Mapping: $this")
    }
    return (this.attribute_name.first() as ResolvedPropertyNameOrIndex.PropertyName).name
}

private fun outputToAttributes(
    reconcileFunction: ReconcileFunction,
    outputs: Map<String, Value>
): Map<String, Value> {
    return reconcileFunction.outputs.map { (key, value) ->
        val output = outputs[key] ?: throw IllegalStateException("Missing output value: $key")
        value.simple() to output
    }.toMap()
}
