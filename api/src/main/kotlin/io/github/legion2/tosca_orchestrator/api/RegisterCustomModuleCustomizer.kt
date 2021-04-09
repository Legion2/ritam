package io.github.legion2.tosca_orchestrator.api

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.quarkus.jackson.ObjectMapperCustomizer
import javax.inject.Singleton

@Singleton
class RegisterCustomModuleCustomizer : ObjectMapperCustomizer {
    override fun customize(mapper: ObjectMapper) {
        mapper.registerKotlinModule()
        mapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }
}