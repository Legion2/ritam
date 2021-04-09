package io.github.legion2.tosca_orchestrator.tosca.model.resolved

import io.github.legion2.tosca_orchestrator.tosca.definitions.OperationImplementationDefinition

/**
 * @property timeout timeout in seconds
 */
data class ResolvedOperationImplementation(
    val primary: ResolvedArtifact? = null,
    val dependencies: List<ResolvedArtifact> = emptyList(),
    val timeout: Int? = null,
    val operation_host: String? = null
) {
    companion object {
        /**
         * 5.8.1 Additional Requirements
         * on-op
         */
        val noOperation = ResolvedOperationImplementation()

        fun from(
            operationImplementation: OperationImplementationDefinition?,
            operationResolverContext: OperationResolverContext
        ): ResolvedOperationImplementation {
            if (operationImplementation == null) {
                return noOperation
            }
            val artifact = operationImplementation.primary?.let {
                ResolvedArtifact.from(
                    it,
                    operationResolverContext.artifactResolverContext,
                    operationResolverContext.artifactResolver
                )
            }
            val dependencies = operationImplementation.dependencies.orEmpty().map {
                ResolvedArtifact.from(
                    it,
                    operationResolverContext.artifactResolverContext,
                    operationResolverContext.artifactResolver
                )
            }

            return ResolvedOperationImplementation(
                artifact,
                dependencies,
                operationImplementation.timeout,
                operationImplementation.operation_host
            )
        }

        fun ResolvedOperationImplementation.refine(
            operationImplementation: OperationImplementationDefinition?,
            operationResolverContext: OperationResolverContext
        ): ResolvedOperationImplementation {
            if (operationImplementation == null) {
                return this
            }
            if (this != noOperation) {
                println("Existing Operation Implementation completely replaced by new implementation: $operationImplementation")
            }

            return from(operationImplementation, operationResolverContext)
        }
    }
}

data class OperationResolverContext(
    val artifactResolverContext: ArtifactResolverContext,
    val artifactResolver: (String) -> ResolvedArtifact
)
