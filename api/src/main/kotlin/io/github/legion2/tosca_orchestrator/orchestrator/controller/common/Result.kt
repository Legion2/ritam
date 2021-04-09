package io.github.legion2.tosca_orchestrator.orchestrator.controller.common

import java.time.Duration

data class Result(val requeue: Boolean, val requeueAfter: Duration = Duration.ZERO)
