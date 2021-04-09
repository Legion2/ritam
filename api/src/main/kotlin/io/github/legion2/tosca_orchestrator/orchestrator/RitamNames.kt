package io.github.legion2.tosca_orchestrator.orchestrator

import io.github.legion2.tosca_orchestrator.tosca.model.Namespace
import io.github.legion2.tosca_orchestrator.tosca.model.TypeReference
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedArtifactType
import java.net.URI

val ritam = Namespace(URI("https://legion2.github.io/ritam"))

val javascript = TypeReference<ResolvedArtifactType>(ritam, "ritam.artifacts.Implementation.JavaScript")
