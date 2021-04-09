package io.github.legion2.tosca_orchestrator.tosca.definitions.datatypes

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.util.StdConverter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class TimeInterval(
    @JsonDeserialize(converter = DateTimeConverter::class) val start_time: Instant,
    @JsonDeserialize(converter = DateTimeConverter::class) val end_time: Instant
)


class DateTimeConverter : StdConverter<String, Instant>() {
    override fun convert(value: String): Instant {
        return try {
            ZonedDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault())).toInstant()
        } catch (e: DateTimeParseException) {
            LocalDate.parse(value, DateTimeFormatter.ISO_DATE).atStartOfDay(ZoneId.systemDefault()).toInstant()
        }
    }
}
