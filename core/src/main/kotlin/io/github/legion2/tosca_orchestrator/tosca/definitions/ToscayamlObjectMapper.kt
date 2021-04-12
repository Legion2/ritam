package io.github.legion2.tosca_orchestrator.tosca.definitions

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.util.StdConverter
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

val toscaYamlMapper by lazy {
    ObjectMapper(YAMLFactory().configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true).disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)).apply {
        findAndRegisterModules()
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
        enable(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES)
        enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
    }
}

abstract class OrderedMappingConverter<K, T> : StdConverter<List<Map<K, T>>, Map<K, T>>() {
    override fun convert(list: List<Map<K, T>>): Map<K, T> {
        return list.associate { wrapper ->
            if (wrapper.size != 1) {
                throw IllegalArgumentException("Only one mapping per list entry")
            }
            val entry = wrapper.entries.toList()[0]
            entry.toPair()
        }
    }
}

abstract class OrderedMappingSerializerConverter<K, T> : StdConverter<Map<K, T>, List<Map<K, T>>>() {
    override fun convert(orderedMap: Map<K, T>): List<Map<K, T>> {
        return orderedMap.map {
            mapOf(it.key to it.value)
        }
    }
}