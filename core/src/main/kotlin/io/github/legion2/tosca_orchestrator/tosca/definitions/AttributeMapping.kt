package io.github.legion2.tosca_orchestrator.tosca.definitions

import com.fasterxml.jackson.annotation.JsonCreator

data class AttributeMapping(
    val entity: Entity,
    val optional_capability_name: String? = null,
    val attribute_name: List<PropertyNameOrIndex>
) {
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    constructor(mapping: List<PropertyNameOrIndex>) : this(
        Entity.valueOf((mapping.first() as PropertyNameOrIndex.PropertyName).name),
        null,
        mapping.drop(1)
    )
}

enum class Entity {
    SELF,
    SOURCE,
    TARGET
}
