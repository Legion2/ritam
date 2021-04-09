package io.github.legion2.tosca_orchestrator.tosca.definitions.datatypes

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.legion2.tosca_orchestrator.tosca.definitions.toscaYamlMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.*


internal class TimeIntervalTest {
    @ParameterizedTest
    @MethodSource("timeIntervalYamls")
    fun testDeserialization(timeIntervalYaml: String, expected: TimeInterval) {
        val timeInterval = toscaYamlMapper.readValue<TimeInterval>(timeIntervalYaml)
        Assertions.assertEquals(expected, timeInterval)
    }

    companion object {
        @JvmStatic
        fun timeIntervalYamls() = listOf(
            Arguments.of(
                """
                    start_time: 2001-12-15T02:59:43.1Z
                    end_time: 2001-12-15T03:59:43.1Z
                """.trimIndent(),
                TimeInterval(Instant.parse("2001-12-15T02:59:43.1Z"), Instant.parse("2001-12-15T03:59:43.1Z"))
            ),
            Arguments.of(
                """
                    start_time: 2001-12-14T21:59:43.10-05:00
                    end_time: 2001-12-14T22:59:45.10-05:00
                """.trimIndent(),
                TimeInterval(
                    ZonedDateTime.parse("2001-12-14T21:59:43.10-05:00").toInstant(),
                    ZonedDateTime.parse("2001-12-14T22:59:45.10-05:00").toInstant()
                )
            ),
            Arguments.of(
                """
                    start_time: 2001-12-15T02:59:43.10
                    end_time: 2002-12-14
                """.trimIndent(),
                TimeInterval(
                    LocalDateTime.parse("2001-12-15T02:59:43.10").atZone(ZoneId.systemDefault()).toInstant(),
                    LocalDate.parse("2002-12-14").atStartOfDay(ZoneId.systemDefault()).toInstant()
                )
            )
        )
    }


}