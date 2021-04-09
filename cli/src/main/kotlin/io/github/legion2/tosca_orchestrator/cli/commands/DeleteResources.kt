package io.github.legion2.tosca_orchestrator.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import io.github.legion2.tosca_orchestrator.cli.CliContext
import io.github.legion2.tosca_orchestrator.cli.resourceNameArgument
import io.github.legion2.tosca_orchestrator.cli.resourceTypeArgument
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class DeleteResources : CliktCommand(name = "delete") {
    private val cliContext by requireObject<CliContext>()
    private val resourceType by resourceTypeArgument()
    private val resourceName by resourceNameArgument()
    private val wait by option("--wait", "-w").flag()

    override fun run() = runBlocking {
        val client = cliContext.ritamClient.getClient(resourceType)
        client.deleteResources(resourceName)
        if (wait) {
            do {
                delay(100)
                val resource = kotlin.runCatching { client.getResource(resourceName) }.getOrNull()
            } while (resource != null)
        }
        echo("$resourceType $resourceName deleted")
    }
}