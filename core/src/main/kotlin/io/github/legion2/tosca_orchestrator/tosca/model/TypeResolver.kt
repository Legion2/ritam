package io.github.legion2.tosca_orchestrator.tosca.model

import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.*
import io.github.legion2.tosca_orchestrator.tosca.normative.*

typealias TypeResolver<T> = (String) -> T

data class ReusableTypesWithResolverContext(
    val reusableTypes: ReusableTypes,
    val typeResolverContext: TypeResolverContext
)

fun ReusableTypesWithResolverContext.createDataTypeResolver(localDataTypes: Map<String, ResolvedDataType> = emptyMap()): TypeResolver<ResolvedDataType> {
    return createTypeResolver(reusableTypes.dataTypes, typeResolverContext, dataTypeShortNameAliases, localDataTypes)
}

fun ReusableTypesWithResolverContext.createNodeTypeResolver(localTypes: Map<String, ResolvedNodeType> = emptyMap()): TypeResolver<ResolvedNodeType> {
    return createTypeResolver(reusableTypes.nodeTypes, typeResolverContext, nodeTypeShortNameAliases, localTypes)
}

fun ReusableTypesWithResolverContext.createRelationshipTypeResolver(localTypes: Map<String, ResolvedRelationshipType> = emptyMap()): TypeResolver<ResolvedRelationshipType> {
    return createTypeResolver(
        reusableTypes.relationshipTypes,
        typeResolverContext,
        relationshipTypeShortNameAliases,
        localTypes
    )
}

fun ReusableTypesWithResolverContext.createInterfaceTypeResolver(localTypes: Map<String, ResolvedInterfaceType> = emptyMap()): TypeResolver<ResolvedInterfaceType> {
    return createTypeResolver(
        reusableTypes.interfaceTypes,
        typeResolverContext,
        interfaceTypeShortNameAliases,
        localTypes
    )
}

fun ReusableTypesWithResolverContext.createCapabilityTypeResolver(localTypes: Map<String, ResolvedCapabilityType> = emptyMap()): TypeResolver<ResolvedCapabilityType> {
    return createTypeResolver(
        reusableTypes.capabilityTypes,
        typeResolverContext,
        capabilityTypeShortNameAliases,
        localTypes
    )
}

fun ReusableTypesWithResolverContext.createArtifactTypeResolver(localTypes: Map<String, ResolvedArtifactType> = emptyMap()): TypeResolver<ResolvedArtifactType> {
    return createTypeResolver(
        reusableTypes.artifactTypes,
        typeResolverContext,
        artifactTypeShortNameAliases,
        localTypes
    )
}

fun ReusableTypesWithResolverContext.createArtifactFileExtensionResolver(localTypes: Map<String, ResolvedArtifactType> = emptyMap()): (String) -> ResolvedArtifactType {
    val artifactTypes = reusableTypes.artifactTypes.flatMap { it.value.values } + localTypes.values
    return fun(fileName: String): ResolvedArtifactType {
        return artifactTypes.find { artifactType -> artifactType.file_ext.any { fileName.endsWith(it) } }
            ?: throw IllegalArgumentException("Artifact Type not known for: $fileName")
    }
}

fun ReusableTypesWithResolverContext.createNodeTypeReferenceResolver(localTypes: Set<String> = emptySet()): TypeResolver<TypeReference<ResolvedNodeType>> {
    return createTypeReferenceResolver(
        reusableTypes.nodeTypes,
        typeResolverContext,
        nodeTypeShortNameAliases,
        localTypes
    )
}

/**
 * Create a Type Resolver which resolves type names according to their namespace, aliases and context.
 */
private fun <T> createTypeResolver(
    namespacedTypes: Map<Namespace, Map<String, T>>,
    typeResolverContext: TypeResolverContext,
    shortNameAliases: Map<String, String>,
    localTypes: Map<String, T> = emptyMap()
): TypeResolver<T> {
    val getTypeReference =
        createTypeReferenceResolver(namespacedTypes, typeResolverContext, shortNameAliases, localTypes.keys)
    return fun(typeName: String): T {
        val (namespace, localName) = getTypeReference(typeName)

        if (namespace == typeResolverContext.namespace) {
            localTypes[localName]?.let { return it }
        }

        val types = namespacedTypes[namespace]
            ?: throw IllegalArgumentException("Namespace not defined: $namespace")
        return types[localName]
            ?: throw IllegalArgumentException("Type not defined '$localName' in namespace: $namespace $namespacedTypes")
    }
}


