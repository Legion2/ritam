package io.github.legion2.tosca_orchestrator.tosca.model

import io.github.legion2.tosca_orchestrator.tosca.model.Namespace.Companion.tosca
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI

internal class TypeReferenceKtTest {

    @Test
    fun testResolveReference() {
        val myNamespace = Namespace(URI("https://my.org"))
        val otherNamespace = Namespace(URI("https://other.com"))

        val myType = "myType"
        val otherType = "otherType"
        val toscaType = "toscaType"

        val namespacedTypes =
            mapOf(
                myNamespace to mapOf(myType to 1),
                tosca to mapOf(toscaType to 1),
                otherNamespace to mapOf(otherType to 1, myType to 2)
            )

        val resolver = createTypeReferenceResolver(
            namespacedTypes,
            TypeResolverContext(myNamespace, mapOf("my" to myNamespace, "other" to otherNamespace), emptySet()),
            emptyMap(),
            setOf("localType")
        )

        MatcherAssert.assertThat(resolver("myType"), `is`(TypeReference(myNamespace, myType)))
        MatcherAssert.assertThat(resolver("https://my.org/myType"), `is`(TypeReference(myNamespace, myType)))
        MatcherAssert.assertThat(resolver("my:myType"), `is`(TypeReference(myNamespace, myType)))

        assertThrows<IllegalArgumentException> { resolver("otherType") }
        MatcherAssert.assertThat(
            resolver("https://other.com/otherType"),
            `is`(TypeReference(otherNamespace, otherType))
        )
        MatcherAssert.assertThat(resolver("other:otherType"), `is`(TypeReference(otherNamespace, otherType)))
        MatcherAssert.assertThat(resolver("other:myType"), `is`(TypeReference(otherNamespace, myType)))

        MatcherAssert.assertThat(resolver("toscaType"), `is`(TypeReference(tosca, toscaType)))
        MatcherAssert.assertThat(
            resolver("http://docs.oasis-open.org/tosca/ns/simple/yaml/1.3/toscaType"),
            `is`(TypeReference(tosca, toscaType))
        )
        MatcherAssert.assertThat(resolver("tosca:toscaType"), `is`(TypeReference(tosca, toscaType)))

        assertThrows<IllegalArgumentException> { resolver("does.not.exist") }
        assertThrows<IllegalArgumentException> { resolver("my") }
        assertThrows<IllegalArgumentException> { resolver("https://my.orgmyType") }
    }
}