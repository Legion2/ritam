package io.github.legion2.tosca_orchestrator.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import io.github.legion2.tosca_orchestrator.cli.CliContext
import io.github.legion2.tosca_orchestrator.cli.echoString
import io.github.legion2.tosca_orchestrator.cli.resourceNameArgument
import io.github.legion2.tosca_orchestrator.cli.resourceTypeArgument
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class GetResources : CliktCommand(name = "get") {
    private val cliContext by requireObject<CliContext>()
    private val resourceType by resourceTypeArgument()
    private val resourceName by resourceNameArgument().optional()
    private val watch by option("--watch", "-w").flag()

    override fun run() = runBlocking {
        val name = resourceName
        echo("\u001B[s", trailingNewline = false)
        while (true) {
            val content = if (name == null) {
                cliContext.ritamClient.getClient(resourceType).getAllResources().echoString
            } else {
                cliContext.ritamClient.getClient(resourceType).getResource(name).echoString
            }
            echo("\u001B[u\u001B[0J$content")

            if (watch) {
                delay(1000)
            } else {
                break
            }
        }
    }
}