package io.github.legion2.tosca_orchestrator.tosca.normative

private val normativeInterfaceTypeDefinitions
    get() = listOf(
        "tosca.interfaces.node.lifecycle.Standard",
        "tosca.interfaces.relationship.Configure"
    )

val interfaceTypeShortNameAliases = mapOf(
    "Standard" to "tosca.interfaces.node.lifecycle.Standard",
    "Configure" to "tosca.interfaces.relationship.Configure"
)

object NormativeLifecycleInterface {
    const val name = "Standard"
    const val createOperation = "create"
    const val configureOperation = "configure"
    const val startOperation = "start"
    const val stopOperation = "stop"
    const val deleteOperation = "delete"
}

object NormativeRelationshipInterface {
    const val name = "Configure"
    const val preConfigureSourceOperation = "pre_configure_source"
    const val postConfigureSourceOperation = "post_configure_source"
    const val preConfigureTargetOperation = "pre_configure_target"
    const val postConfigureTargetOperation = "post_configure_target"
    const val addTargetOperation = "add_target"
    const val addSourceOperation = "add_source"
    const val targetChangedOperation = "target_changed"
    const val removeTargetOperation = "remove_target"
}
