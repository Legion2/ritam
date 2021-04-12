package io.github.legion2.tosca_orchestrator.api.mapper

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import javax.ws.rs.Consumes
import javax.ws.rs.Produces
import javax.ws.rs.ext.ContextResolver
import javax.ws.rs.ext.Provider

@Provider
@Consumes("*/*")
@Produces("*/*")
class YamlMapperProvider : ContextResolver<YAMLMapper> {

    private val yamlMapper: YAMLMapper by lazy {
        YAMLMapper(
            YAMLFactory()
                .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        ).apply {
            findAndRegisterModules()
            setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }

    override fun getContext(type: Class<*>?): YAMLMapper {
        return yamlMapper
    }
}