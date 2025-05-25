package com.example.sandbox.domain.valueobject

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

    data class EnumConvertError(val message: String)

    companion object {
        private val map = entries.associateBy { it.toString() }

        fun of(s: String): Result<Position, EnumConvertError> =
            map[s]?.let {
                Ok(it)
            } ?: run {
                Err(EnumConvertError("unknown position: $s"))
            }
    }
}
