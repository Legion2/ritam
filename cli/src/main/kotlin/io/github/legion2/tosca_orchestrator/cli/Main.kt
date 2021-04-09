package io.github.legion2.tosca_orchestrator.cli

import com.github.ajalt.clikt.core.subcommands
import io.github.legion2.tosca_orchestrator.cli.commands.*

fun createCLI(): RitamCli {
    return RitamCli().subcommands(
        GetResources(),
        CreateResources(),
        UpdateResources(),
        ApplyResources(),
        DeleteResources()
    )
}

fun main(args: Array<String>) = createCLI().main(args)
