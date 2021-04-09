package io.github.legion2.tosca_orchestrator.tosca.model

import java.net.URI

data class Namespace(val uri: URI) {
    companion object {
        val tosca = Namespace(URI("http://docs.oasis-open.org/tosca/ns/simple/yaml/1.3"))
    }
}
