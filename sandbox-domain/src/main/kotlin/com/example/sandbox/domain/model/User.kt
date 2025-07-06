package com.example.sandbox.domain.model

import com.example.sandbox.domain.valueobject.MailAddress
import com.example.sandbox.domain.valueobject.Position
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.mapError
import de.huxhorn.sulky.ulid.ULID

interface User {
    val id: ULID.Value
    val name: String
    val position: Position
    val mailAddress: MailAddress

    sealed class CreateError {
        data class EnumConvertError(val message: String) : CreateError()
        data object InvalidMailAddressError : CreateError()
    }

    sealed class CopyError {
        data class EnumConvertError(val message: String) : CopyError()
        data object InvalidMailAddressError : CopyError()
    }

    fun copy(
        name: String = this.name,
        position: String = this.position.toString(),
        mailAddress: String = this.mailAddress.value
    ): Result<User, CopyError> = binding {
        val convertedPosition = Position.of(position)
            .mapError { CopyError.EnumConvertError(it.message) }
            .bind()
        val convertedMailAddress = MailAddress.create(mailAddress)
            .mapError { CopyError.InvalidMailAddressError }
            .bind()

        UserData(
            id = id, // keep the original id
            name = name,
            position = convertedPosition,
            mailAddress = convertedMailAddress,
        )
    }

    companion object {
        fun create(
            name: String,
            position: String,
            mailAddress: String
        ): Result<User, CreateError> = binding {
            val convertedPosition = Position.of(position)
                .mapError { CreateError.EnumConvertError(it.message) }
                .bind()
            val convertedMailAddress = MailAddress.create(mailAddress)
                .mapError { CreateError.InvalidMailAddressError }
                .bind()

            UserData(
                id = generateId(),
                name = name,
                position = convertedPosition,
                mailAddress = convertedMailAddress
            )
        }

        private fun generateId(): ULID.Value = ULID().nextValue()
    }
}

private data class UserData(
    override val id: ULID.Value,
    override val name: String,
    override val position: Position,
    override val mailAddress: MailAddress
) : User
