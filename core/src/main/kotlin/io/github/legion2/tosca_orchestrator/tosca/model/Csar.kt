package io.github.legion2.tosca_orchestrator.tosca.model

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.legion2.tosca_orchestrator.tosca.definitions.ServiceTemplateDefinition
import java.io.InputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipInputStream


data class ToscaMetaFile(
    val metaFileVersion: String,
    val CsarVersion: String,
    val createdBy: String,
    val entryDefinitions: URI
)

fun loadMetadata(csarBasePath: Path): ToscaMetaFile {
    val toscaMetaFile = csarBasePath.resolve("TOSCA-Metadata").resolve("TOSCA.meta")
    return if (Files.exists(toscaMetaFile)) {
        val lines = Files.readAllLines(toscaMetaFile).associate { it.substringBefore(": ") to it.substringAfter(": ") }
        val entryDefinitions = (lines["Entry-Definitions"]
            ?: throw IllegalArgumentException("Entry-Definitions not defined in TOSCA Meta File."))
        val uri = csarBasePath.resolve(entryDefinitions).toUri()
        ToscaMetaFile(
            lines["TOSCA-Meta-File-Version"]
                ?: throw IllegalArgumentException("TOSCA-Meta-File-Version not defined in TOSCA Meta File."),
            lines["CSAR-Version"]
                ?: throw IllegalArgumentException("CSAR-Version not defined in TOSCA Meta File."),
            lines["Created-By"]
                ?: throw IllegalArgumentException("Created-By not defined in TOSCA Meta File."),
            uri
        )

    } else {
        val yamlFile = Files.newDirectoryStream(csarBasePath, "*.{yaml,yml}").use { it.toList() }.single().toUri()
        val serviceTemplateDefinition: ServiceTemplateDefinition = objectMapper.readValue(yamlFile.toURL())
        val metadata = serviceTemplateDefinition.metadata.orEmpty()
        ToscaMetaFile(
            "1.1",
            metadata["template_version"]
                ?: throw IllegalArgumentException("template_version not defined in TOSCA Meta File."),
            metadata["template_author"]
                ?: throw IllegalArgumentException("template_author not defined in metadata."),
            yamlFile
        )
    }
}

fun storeCsar(inputStream: InputStream): Path {
    val outputDir = Files.createTempDirectory("orchestration-upload-csar-")

    val zipInputStream = ZipInputStream(inputStream)
    zipInputStream.use {
        while (true) {
            val entry = it.nextEntry ?: break
            val outputFile = outputDir.resolve(entry.name).normalize()
            if (!outputFile.startsWith(outputDir)) {
                throw IllegalArgumentException("Entry is outside of the target dir: ${entry.name}")
            }
            if (entry.isDirectory) {
                Files.createDirectories(outputFile)
            } else {
                Files.copy(it, outputFile)
            }
        }
    }
    return outputDir
}
