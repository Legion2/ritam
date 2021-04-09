package io.github.legion2.tosca_orchestrator.orchestrator.artifact

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path

data class ExecutionEnvironment(val directory: Path) {

    suspend fun cleanup() {
        withContext(Dispatchers.IO) {
            directory.toFile().deleteRecursively()
        }
    }

    companion object {
        suspend fun getExecutionEnvironment(): ExecutionEnvironment {
            val tempDirectory = withContext(Dispatchers.IO) {
                Files.createTempDirectory("tosca-execution-environment-")
            }
            return ExecutionEnvironment(tempDirectory)
        }
    }
}