package io.github.legion2.tosca_orchestrator.tosca.normative

private val normativeDataTypeDefinitions
    get() = listOf(
        "tosca.datatypes.Credential",
        "tosca.datatypes.Root",
        "tosca.datatypes.TimeInterval",
        "tosca.datatypes.json",
        "tosca.datatypes.network.NetworkInfo",
        "tosca.datatypes.network.PortDef",
        "tosca.datatypes.network.PortInfo",
        "tosca.datatypes.network.PortSpec",
        "tosca.datatypes.xml"
    )

val dataTypeShortNameAliases = mapOf(
    "Credential" to "tosca.datatypes.Credential",
    "TimeInterval" to "tosca.datatypes.TimeInterval",
    "json" to "tosca.datatypes.json",
    "NetworkInfo" to "tosca.datatypes.network.NetworkInfo",
    "PortDef" to "tosca.datatypes.network.PortDef",
    "PortInfo" to "tosca.datatypes.network.PortInfo",
    "PortSpec" to "tosca.datatypes.network.PortSpec",
    "xml" to "tosca.datatypes.xml"
)
