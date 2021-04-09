package io.github.legion2.tosca_orchestrator.tosca.definitions

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass

internal class ExpressionDeserializerTest {
    @ParameterizedTest
    @MethodSource("validExpressions")
    fun testValidExpressions(yaml: String, expressionClass: KClass<*>) {
        val expression = toscaYamlMapper.readValue(yaml, Expression::class.java)
        Assertions.assertEquals(expressionClass, expression::class)
    }

    companion object {
        @JvmStatic
        fun validExpressions() = listOf(
            Arguments.of("test", Expression.Literal.String::class),
            Arguments.of("0", Expression.Literal.Integer::class),
            Arguments.of("true", Expression.Literal.Boolean::class),
            Arguments.of("false", Expression.Literal.Boolean::class),
            Arguments.of("999999", Expression.Literal.Integer::class),
            Arguments.of("{ concat: [test, bla] }", Expression.Function.Concat::class),
            Arguments.of("{ concat: [ concat: [test, foo] , bla ] }", Expression.Function.Concat::class),
            Arguments.of("join: [[test, bla], g]", Expression.Function.Join::class),
            Arguments.of("{ join: [ [ \"t\", \"o\", \"s\", \"c\", \"a\" ] ] }", Expression.Function.Join::class),
            Arguments.of("join: [ [ t, o, s, c, a ] ]", Expression.Function.Join::class),
            Arguments.of("join: [[test, join: [[test, bla], g]], g]", Expression.Function.Join::class),
            Arguments.of("token: [test.ba , \".\", 1 ]", Expression.Function.Token::class),
            Arguments.of("token: [join: [[host, 80], \":\"] , \":\", 1 ]", Expression.Function.Token::class),
            Arguments.of("get_input: test", Expression.Function.GetInput::class),
            Arguments.of("get_input: [test, 1, foo]", Expression.Function.GetInput::class),
            Arguments.of("get_property: [test, bla]", Expression.Function.GetProperty::class),
            Arguments.of("get_property: [SELF, db, url]", Expression.Function.GetProperty::class),
            Arguments.of("get_property: [test, [bla]]", Expression.Function.GetProperty::class),
            Arguments.of("get_property: [TARGET, db, [url]]", Expression.Function.GetProperty::class),
            Arguments.of("get_attribute: [test, foo]", Expression.Function.GetAttribute::class),
            Arguments.of("get_attribute: [SELF, db, url]", Expression.Function.GetAttribute::class),
            Arguments.of("get_attribute: [HOST, [url, port]]", Expression.Function.GetAttribute::class),
            Arguments.of("get_attribute: [SOURCE, db, [url]]", Expression.Function.GetAttribute::class),
            Arguments.of("get_attribute: [SELF, db, [elements, 1]]", Expression.Function.GetAttribute::class),
            Arguments.of(
                "get_operation_output: [SELF, default, start, status]",
                Expression.Function.GetOperationOutput::class
            ),
            Arguments.of(
                "get_operation_output: [db, data, migration, status]",
                Expression.Function.GetOperationOutput::class
            ),
            Arguments.of("get_node_of_type: Compute", Expression.Function.GetNodesOfType::class),
            Arguments.of("get_artifact: [SELF, start.sh]", Expression.Function.GetArtifact::class),
            Arguments.of("get_artifact: [HOST, start.sh, LOCAL_FILE]", Expression.Function.GetArtifact::class),
            Arguments.of(
                "get_artifact: [db, connect, /var/scripts/connect.sh]",
                Expression.Function.GetArtifact::class
            ),
            Arguments.of("get_artifact: [db, data, /var/data, false]", Expression.Function.GetArtifact::class),
            Arguments.of(
                """
                    endpoint:
                     host: localhost
                     port: 80
                    header:
                     host: example.org
                     """.trimIndent(), Expression.Literal.Map::class
            ),
            Arguments.of("list: [db, data, /var/data, false]", Expression.Literal.Map::class),
            Arguments.of("[db, data, /var/data, false]", Expression.Literal.List::class),
            Arguments.of(
                """
                            - db
                            - data
                            - /var/data
                            - false
                            """.trimIndent(), Expression.Literal.List::class
            )
        )
    }
}