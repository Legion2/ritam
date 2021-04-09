package io.github.legion2.tosca_orchestrator.orchestrator.artifact

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.legion2.tosca_orchestrator.orchestrator.artifact.ExecutionEnvironment.Companion.getExecutionEnvironment
import io.github.legion2.tosca_orchestrator.orchestrator.evaluation.DynamicEvaluationContext
import io.github.legion2.tosca_orchestrator.orchestrator.model.ReconcileFunction
import io.github.legion2.tosca_orchestrator.tosca.model.property.Value
import io.github.legion2.tosca_orchestrator.tosca.model.property.fromRawValue
import io.github.legion2.tosca_orchestrator.tosca.model.property.rawValue
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedArtifact
import kotlinx.coroutines.withTimeout
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.time.Duration
import java.util.*
import javax.annotation.PostConstruct
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
class ExecutionEngine {
    private val LOG: Logger = Logger.getLogger(this.javaClass)

    @ConfigProperty(name = "orchestrator.execution.defaultTimeout", defaultValue = "PT5M")
    protected lateinit var defaultTimeoutConfig: String

    private val defaultTimeout: Duration get() = Duration.parse(defaultTimeoutConfig)
    private lateinit var artifactProcessorManagers: List<ArtifactProcessorManager>

    @Inject
    lateinit var objectMapper: ObjectMapper

    suspend fun executeOperation(
        interfaceInstance: ReconcileFunction,
        evaluationContext: DynamicEvaluationContext
    ): Map<String, Value> {
        val timeout = interfaceInstance.timeout ?: defaultTimeout
        val primary = interfaceInstance.artifact
        val dependencies = interfaceInstance.dependencies
        val inputs = interfaceInstance.inputs.mapValues { evaluationContext.evaluate(it.value) }
            .mapValues { objectMapper.writeValueAsString(it.value.rawValue) }
        val outputs = interfaceInstance.outputs.keys
        return if (primary != null) {
            val executionEnvironment = getExecutionEnvironment()
            val output = try {
                val artifactProcessor = getArtifactProcessor(primary)
                LOG.debug("Start processing of reconciler artifact")
                withTimeout(timeout.toMillis()) {
                    artifactProcessor.process(primary, executionEnvironment, inputs, outputs, dependencies)
                }
            } finally {
                LOG.debug("Finished processing of reconciler artifact")
                executionEnvironment.cleanup()
            }
            output.mapValues { (key, value) ->
                kotlin.runCatching {
                    objectMapper.readValue<Any?>(value)
                }.getOrElse {
                    LOG.error("Exception thrown while deserialize output values for key '$key'", it)
                    null
                }
            }.mapValues { fromRawValue(it.value) }
        } else {
            LOG.debug("No artifact for reconciler operation")
            emptyMap()
        }
    }

    @PostConstruct
    protected fun loadArtifactProcessorManager() {
        artifactProcessorManagers =
            ServiceLoader.load(ArtifactProcessorManager::class.java).iterator().asSequence().toList()
    }

    /**
     * 13.2.1 Identify Artifact Processor
     */
    fun getArtifactProcessor(resolvedArtifact: ResolvedArtifact): ArtifactProcessorManager {
        return artifactProcessorManagers.find {
            kotlin.runCatching { it.canProcess(resolvedArtifact.type) }.getOrDefault(false)
        } ?: throw IllegalArgumentException("artifact type '${resolvedArtifact.type}' is not supported by any Artifact Processor")
    }

}

