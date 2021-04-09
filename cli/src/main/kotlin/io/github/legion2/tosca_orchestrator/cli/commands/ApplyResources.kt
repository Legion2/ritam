package io.github.legion2.tosca_orchestrator.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.inputStream
import io.github.legion2.tosca_orchestrator.cli.*
import kotlinx.coroutines.runBlocking

class ApplyResources : CliktCommand(name = "apply") {
    private val cliContext by requireObject<CliContext>()
    private val resourceType by resourceTypeArgument()
    private val inputStream by argument(
        name = "resource",
        help = "The file containing the resource definition or - to use stdin"
    ).inputStream()

    override fun run() = runBlocking {
        val resource = inputStream.readResource()
        val client = cliContext.ritamClient.getClient(resourceType)
        val currentResource = kotlin.runCatching { client.getResource(resource.name) }.getOrNull()

        if (currentResource == null) {
            client.createResources(resource.name, resource)
        } else {
            val patchedResource = resource.patch(currentResource)
            client.updateResources(resource.name, patchedResource)
        }.let { echo(it.echoString) }
    }
}