package io.github.legion2.tosca_orchestrator.tosca.model.property

import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedDataType

enum class PrimitiveType {
    COMPLEX,
    STRING,
    INTEGER,
    FLOAT,
    BOOLEAN,
    TIMESTAMP,
    NULL,
    VERSION,
    RANGE,
    LIST,
    MAP,
    SCALAR_UNIT_SIZE,
    SCALAR_UNIT_TIME,
    SCALAR_UNIT_FREQUENCY,
    SCALAR_UNIT_BITRATE
}

fun PrimitiveType.toResolvedType(): ResolvedDataType {
    return primitiveTypes.values.find { it.primitiveType == this }!!
}

val sizeUnits = listOf("B", "kB", "KiB", "MB", "MiB", "GB", "GiB", "TB", "TiB")
val timeUnits = listOf("d", "h", "m", "s", "ms", "us", "ns")
val frequencyUnits = listOf("Hz", "kHz", "MHz", "GHz")
val bitrateUnits = listOf("bps", "Kbps", "Kibps", "Mbps", "")//TODO

val primitiveTypes: Map<String, ResolvedDataType>
    get() = listOf(
        ResolvedDataType("tosca.datatypes.Root", PrimitiveType.COMPLEX),
        ResolvedDataType("string", PrimitiveType.STRING),
        ResolvedDataType("integer", PrimitiveType.INTEGER),
        ResolvedDataType("float", PrimitiveType.FLOAT),
        ResolvedDataType("boolean", PrimitiveType.BOOLEAN),
        ResolvedDataType("timestamp", PrimitiveType.TIMESTAMP),
        ResolvedDataType("null", PrimitiveType.NULL),
        ResolvedDataType("version", PrimitiveType.VERSION),
        ResolvedDataType("range", PrimitiveType.RANGE),
        ResolvedDataType("list", PrimitiveType.LIST),
        ResolvedDataType("map", PrimitiveType.MAP),
        ResolvedDataType("scalar-unit.size", PrimitiveType.SCALAR_UNIT_SIZE),
        ResolvedDataType("scalar-unit.time", PrimitiveType.SCALAR_UNIT_TIME),
        ResolvedDataType("scalar-unit.frequency", PrimitiveType.SCALAR_UNIT_FREQUENCY),
        ResolvedDataType("scalar-unit.bitrate", PrimitiveType.SCALAR_UNIT_BITRATE)
    ).associateBy { it.name }
