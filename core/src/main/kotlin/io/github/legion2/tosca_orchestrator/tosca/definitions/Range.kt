package io.github.legion2.tosca_orchestrator.tosca.definitions

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.util.StdConverter

@JsonDeserialize(converter = RangeConverter::class)
@JsonSerialize(converter = RangeSerializeConverter::class)
data class Range(val lower_bound: LowerBound, val upper_bound: UpperBound) {
    init {
        when (upper_bound) {
            is UpperBound.Scalar -> {
                if (lower_bound.value > upper_bound.value) {
                    throw IllegalArgumentException("Lower bound must be lower than upper bound")
                }
            }
            is UpperBound.UNBOUNDED -> Unit
        }
    }
}

data class LowerBound(val value: Int) : Comparable<LowerBound> {
    override fun compareTo(other: LowerBound): Int {
        return value.compareTo(other.value)
    }
}

sealed class UpperBound : Comparable<UpperBound> {
    data class Scalar(val value: Int) : UpperBound() {
        override fun compareTo(other: UpperBound): Int {
            return when (other) {
                is Scalar -> value.compareTo(other.value)
                is UNBOUNDED -> -1
            }
        }
    }

    object UNBOUNDED : UpperBound() {
        override fun compareTo(other: UpperBound): Int {
            return when (other) {
                is Scalar -> 1
                is UNBOUNDED -> 0
            }
        }
    }
}

private const val literalUnbounded = "UNBOUNDED"

class RangeConverter : StdConverter<List<Any>, Range>() {
    override fun convert(value: List<Any>): Range {
        val upper = value[1]
        val upperBound = if (upper is String && upper.trim() == literalUnbounded) {
            UpperBound.UNBOUNDED
        } else if (upper is Int) {
            UpperBound.Scalar(upper)
        } else {
            throw IllegalArgumentException("Can not create Range from provided list: $value")
        }

        return Range(LowerBound(value[0] as Int), upperBound)
    }
}

class RangeSerializeConverter : StdConverter<Range, List<Any>>() {
    override fun convert(range: Range): List<Any> {
        val upper: Any = when (range.upper_bound) {
            is UpperBound.Scalar -> range.upper_bound.value
            is UpperBound.UNBOUNDED -> literalUnbounded
        }
        return listOf(range.lower_bound.value, upper)
    }
}

fun Range.refine(other: Range?): Range {
    return if (other == null) {
        this
    } else {
        if (other.lower_bound < lower_bound) {
            throw IllegalArgumentException("The lower bound of the refines can not be lower")
        }
        if (other.upper_bound > upper_bound) {
            throw IllegalArgumentException("The upper bound of the refines can not be higher")
        }
        other
    }
}
