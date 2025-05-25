package com.example.sandbox.domain.valueobject

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import org.apache.commons.validator.routines.EmailValidator

interface MailAddress {
    val value: String

    data object InvalidMailAddressError

    companion object {
        fun create(value: String): Result<MailAddress, InvalidMailAddressError> = binding {
            validate(value).bind()
            MailAddressData(value)
        }

        private fun validate(value: String): Result<Unit, InvalidMailAddressError> {
            val isValid = EmailValidator.getInstance()
                .isValid(value)

            return if (isValid) {
                Ok(Unit)
            } else {
                Err(InvalidMailAddressError)
            }
        }
    }
}

private data class MailAddressData(
    override val value: String
) : MailAddress {
    override fun toString(): String = value
}
