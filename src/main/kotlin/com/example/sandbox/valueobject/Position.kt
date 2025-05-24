package com.example.sandbox.valueobject

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result

enum class Position {
    ENGINEER,
    SENIOR_ENGINEER,
    STAFF_ENGINEER,
    MANAGER,
    SENIOR_MANAGER,
    GENERAL_MANAGER;

    data class ConvertError(val message: String) {
        fun toException(): IllegalArgumentException {
            return IllegalArgumentException(message)
        }
    }

    companion object {
        private val map = entries.associateBy { it.toString() }

        fun of(s: String): Result<Position, ConvertError> {
            val converted = map[s]
            return if (converted != null) {
                Ok(converted)
            } else {
                Err(ConvertError("unknown position: $s"))
            }
        }
    }
}
