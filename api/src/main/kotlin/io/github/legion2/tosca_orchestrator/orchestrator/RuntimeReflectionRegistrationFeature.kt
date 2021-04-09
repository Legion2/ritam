package io.github.legion2.tosca_orchestrator.orchestrator

import org.graalvm.nativeimage.hosted.Feature
import org.graalvm.nativeimage.hosted.RuntimeReflection
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import java.lang.reflect.Array
import java.net.URI

class RuntimeReflectionRegistrationFeature : Feature {
    override fun beforeAnalysis(access: Feature.BeforeAnalysisAccess) {
        registerPackage("io.github.legion2.tosca_orchestrator.tosca.definitions")
        registerPackage("io.github.legion2.tosca_orchestrator.tosca.model")
        registerClass(URI::class.java)
        registerClass(Map::class.java)
        registerClass(List::class.java)
        registerClass(Boolean::class.java)
        registerClass(java.lang.Boolean::class.java)
        registerClass(Int::class.java)
        registerClass(Double::class.java)
        registerClass(Float::class.java)
        registerClass(String::class.java)
    }

    private fun registerPackage(packageName: String) {
        val reflections = Reflections(packageName, SubTypesScanner(false))
        val classes = reflections.getSubTypesOf(Any::class.java)
        println("Registering package, found classes: ${classes.size}.")

        for (clazz in classes) {
            registerClass(clazz)
        }
    }

    private fun registerClass(clazz: Class<out Any>, withArrayType: Boolean = true) {
        println("Registering $clazz.")
        RuntimeReflection.register(clazz)
        clazz.declaredFields.forEach { RuntimeReflection.register(it) }
        clazz.fields.forEach { RuntimeReflection.register(it) }
        clazz.declaredMethods.forEach { RuntimeReflection.register(it) }
        clazz.methods.forEach { RuntimeReflection.register(it) }
        clazz.declaredConstructors.forEach { RuntimeReflection.register(it) }
        clazz.constructors.forEach { RuntimeReflection.register(it) }
        if (withArrayType) {
            registerClass(clazz.arrayTypePoly(), false)
        }
    }

    private fun Class<out Any>.arrayTypePoly() = Array.newInstance(this, 0).javaClass
}