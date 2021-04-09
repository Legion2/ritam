package io.github.legion2.tosca_orchestrator.cli

import java.net.URI

class CliContext(
    val ritamClient: RitamClient
) {
    companion object {
        fun createCliContext(uri: URI): CliContext {
            return CliContext(RitamClient(uri))
        }
    }

}