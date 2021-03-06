package io.github.legion2.tosca_orchestrator.tosca.model

/**
 * @property namespace is the own namespace of this context
 * @property aliases are all the alias names defined in this context referencing other namespaces
 * @property unnamedImports all generated namespaces for imported service templates without namespace and alias
 */
data class TypeResolverContext(
    val namespace: Namespace,
    val aliases: Map<String, Namespace>,
    val unnamedImports: Set<Namespace>
)
