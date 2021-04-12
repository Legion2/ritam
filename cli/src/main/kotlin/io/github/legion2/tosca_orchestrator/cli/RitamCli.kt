package io.github.legion2.tosca_orchestrator.cli

import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.versionOption
import java.net.URI

class RitamCli : CliktCommand(name = "ritam", autoCompleteEnvvar = "RITAM_CLI_COMPLETE") {
    private val url: String by option(
        "-e", "--endpoint", help = "Endpoint URL of the RITAM Device and Application Manager",
        metavar = "<url>"
    ).default("http://localhost:8080")

    init {
        versionOption("0.4.0")
        context {
            autoEnvvarPrefix = "RITAM_CLI"
        }
    }

    override fun run() {
        currentContext.findOrSetObject { CliContext.createCliContext(URI.create(url)) }
    }
}

fun CliktCommand.resourceTypeArgument() = argument(
    name = "resource-type",
    help = "The Resource Type",
    completionCandidates = CompletionCandidates.Fixed("application", "application-template", "device", "component")
)

fun CliktCommand.resourceNameArgument() =
    argument(name = "resource-name", help = "The name of the Resource")