package com.example.sandbox.domain.extension

import com.example.sandbox.domain.error.common.InvalidULIDError
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import de.huxhorn.sulky.ulid.ULID

fun String.parseULID(): Result<ULID.Value, InvalidULIDError> = try {
    Ok(ULID.parseULID(this))
} catch (e: IllegalArgumentException) {
    Err(InvalidULIDError(e.message ?: "Invalid ULID format: $this"))
}
