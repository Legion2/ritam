package io.github.legion2.tosca_orchestrator.tosca.model

import java.net.URI

/**
 * The fully qualified and resolved Type name with namespace
 */
data class TypeReference<T>(val namespace: Namespace, val name: String)

internal fun <T> createTypeReferenceResolver(
    namespacedTypes: Map<Namespace, Map<String, T>>,
    typeResolverContext: TypeResolverContext,
    shortNameAliases: Map<String, String>,
    localTypes: Set<String> = emptySet()
): TypeResolver<TypeReference<T>> {
    return fun(typeName: String): TypeReference<T> {
        try {
            val typeNameResolvedAliases = shortNameAliases[typeName] ?: typeName
            val (namespace, localName) = runCatching {
                val asUri = URI.create(typeNameResolvedAliases)
                val namespace = namespacedTypes.keys.find { it.uri.relativize(asUri) != asUri }
                if (namespace != null) {
                    namespace to namespace.uri.relativize(asUri).toString()
                } else null
            }.getOrNull() ?: if (typeNameResolvedAliases.contains(":")) {
                val split = typeNameResolvedAliases.split(":", limit = 2)
                typeResolverContext.aliases[split[0]] to split[1]
            } else {
                null to typeNameResolvedAliases
            }

            if (namespace == null) {
                // if namespace not given first check if type is in tosca namespace
                if (namespacedTypes.getValue(Namespace.tosca).containsKey(localName)) {
                    return TypeReference(Namespace.tosca, localName)
                }

                // if namespace not given check if it is imported without namespace
                for (unnamedImport in typeResolverContext.unnamedImports) {
                    if (namespacedTypes.getValue(unnamedImport).containsKey(localName)) {
                        return TypeReference(unnamedImport, localName)
                    }
                }
            }

            val namespaceOrLocal = namespace ?: typeResolverContext.namespace

            // check if type is a local type
            if (namespaceOrLocal == typeResolverContext.namespace) {
                if (localTypes.contains(localName)) {
                    return TypeReference(namespaceOrLocal, localName)
                }
            }

            val types = namespacedTypes[namespaceOrLocal]
                ?: throw IllegalArgumentException("Namespace not defined: $namespaceOrLocal")
            if (types.containsKey(localName)) {
                return TypeReference(namespaceOrLocal, localName)
            } else {
                throw IllegalArgumentException("Type '$localName' not defined in namespace: $namespaceOrLocal $namespacedTypes")
            }
        } catch (e: RuntimeException) {
            throw IllegalArgumentException("Can not find type for name '$typeName' with $typeResolverContext in $namespacedTypes or $localTypes", e)
        }
    }
}

/**
 * Get Type from TypeReference
 */
fun <T> Map<Namespace, Map<String, T>>.get(typeReference: TypeReference<T>): T {
    return this.getValue(typeReference.namespace).getValue(typeReference.name)
}