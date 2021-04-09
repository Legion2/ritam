package io.github.legion2.tosca_orchestrator.tosca.model

//TODO is this really required
data class TypeInfo<T>(val name: TypeReference<T>, val baseType: TypeInfo<T>? = null)

interface Type<T> {
    val typeInfo: TypeInfo<T>
}

fun <T> Type<T>.isSubtypeOf(type: Type<T>) = typeInfo.isSubtypeOf(type.typeInfo)
fun <T> Type<T>.isSubtypeOf(typeReference: TypeReference<T>) = typeInfo.isSubtypeOf(typeReference)
fun <T> TypeInfo<T>.isSubtypeOf(type: TypeInfo<T>) = isSubtypeOf(type.name)

tailrec fun <T> TypeInfo<T>.isSubtypeOf(typeReference: TypeReference<T>): Boolean {
    if (this.name == typeReference) {
        return true
    }
    if (this.baseType == null) {
        return false
    }
    return this.baseType.isSubtypeOf(typeReference)
}
