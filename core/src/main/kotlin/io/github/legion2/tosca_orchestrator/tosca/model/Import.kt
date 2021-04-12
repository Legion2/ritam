package io.github.legion2.tosca_orchestrator.tosca.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.legion2.tosca_orchestrator.tosca.definitions.ImportDefinition
import io.github.legion2.tosca_orchestrator.tosca.definitions.ServiceTemplateDefinition
import io.github.legion2.tosca_orchestrator.tosca.definitions.toscaYamlMapper
import java.net.URI
import java.util.*

/**
 * Information about an Service Template loaded from an URI
 */
private data class ImportInfo(
    val namespace: Namespace,
    val serviceTemplateDefinition: ServiceTemplateDefinition,
    val uri: URI,
    val importWithoutNamespace: Boolean
) {
    val contextPath: URI get() = uri.parent
}

data class ImportedServiceTemplate(
    val namespace: Namespace,
    val serviceTemplateDefinition: ServiceTemplateDefinition,
    val importedNamespaces: Set<Namespace>,
    val namespaceAliases: Map<String, Namespace>,
    val unnamedImports: Set<Namespace>,
    val uri: URI
) {
    val contextPath: URI get() = uri.parent
}

fun importAll(uri: URI): Pair<ImportedServiceTemplate, Map<Namespace, ImportedServiceTemplate>> {
    val importInfo = import(ImportDefinition(uri), uri.parent)
    return importAll(importInfo)
}

private fun importAll(rootImportInfo: ImportInfo): Pair<ImportedServiceTemplate, Map<Namespace, ImportedServiceTemplate>> {
    val serviceTemplateDefinitions = mutableMapOf<Namespace, ImportedServiceTemplate>()
    val queue: Queue<ImportInfo> = LinkedList()

    val rootNamespace = rootImportInfo.namespace
    queue.add(rootImportInfo)
    queue.add(loadNormativeTypes())

    while (queue.isNotEmpty()) {
        val importInfo = queue.remove()
        if (serviceTemplateDefinitions.containsKey(importInfo.namespace)) {
            continue
        }
        if (importInfo.serviceTemplateDefinition.tosca_definitions_version != "tosca_simple_yaml_1_3") {
            throw IllegalArgumentException("TOSCA definition version '${importInfo.serviceTemplateDefinition.tosca_definitions_version}' not supported.")
        }

        val imports = importInfo.serviceTemplateDefinition.imports.orEmpty().associateWith { importDefinition ->
            import(importDefinition, importInfo.contextPath)
        }
        queue.addAll(imports.values)

        val importedNamespaces = imports.values.map { it.namespace }.toSet().let {
            if (importInfo.namespace == Namespace.tosca) it else it + Namespace.tosca
        }

        val aliases =
            imports.filterKeys { it.namespace_prefix != null }.map { it.key.namespace_prefix to it.value.namespace }
                .toMap() as Map<String, Namespace>

        val unnamedImports = imports.filter { it.value.importWithoutNamespace && it.key.namespace_prefix == null }
            .map { it.value.namespace }.toSet()

        serviceTemplateDefinitions[importInfo.namespace] = ImportedServiceTemplate(
            importInfo.namespace,
            importInfo.serviceTemplateDefinition,
            importedNamespaces,
            aliases,
            unnamedImports,
            importInfo.uri
        )
    }

    return serviceTemplateDefinitions.getValue(rootNamespace) to serviceTemplateDefinitions
}

/**
 * @param contextPath is the uri from which to resolve relative URIs
 */
private fun import(importDefinition: ImportDefinition, contextPath: URI): ImportInfo {
    if (importDefinition.namespace_uri != null) {
        throw IllegalArgumentException("namespace_uri is deprecated in TOSCA 1.2 and not supported, declare the namespace in the target ServiceTemplate instead.")
    }

    val uri = contextPath.resolve(importDefinition.file)
    //TODO repository

    val serviceTemplateDefinition =
        runCatching { toscaYamlMapper.readValue<ServiceTemplateDefinition>(uri.toURL()) }.addExceptionContextInfo { "Can not load Service Template: $uri" }
    val namespace = namespaceFor(serviceTemplateDefinition.namespace, uri)
    return ImportInfo(namespace, serviceTemplateDefinition, uri, serviceTemplateDefinition.namespace == null)
}

private fun namespaceFor(namespace: URI?, serviceTemplateUri: URI): Namespace {
    if (namespace != null) {
        return Namespace(namespace)
    }

    return Namespace(serviceTemplateUri)
}

val URI.parent: URI
    get() =
        when {
            isOpaque -> URI(scheme, schemeSpecificPart.removeSuffix("/").substringBeforeLast("/") + "/", fragment) //TODO this is a workaround
            else -> if (path.endsWith("/")) resolve("..") else resolve(".")
        }


private fun loadNormativeTypes(): ImportInfo {
    val classLoader = Thread.currentThread().contextClassLoader

    val uri = classLoader.getResource("io/github/legion2/tosca_orchestrator/tosca/normative/normative-types-1.3.yaml")!!
        .toURI()
    return import(ImportDefinition(uri), uri)
}
